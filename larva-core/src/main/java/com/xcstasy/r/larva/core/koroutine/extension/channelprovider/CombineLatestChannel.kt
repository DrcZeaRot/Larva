package com.xcstasy.r.larva.core.koroutine.extension.channelprovider

import com.xcstasy.r.larva.core.koroutine.extension.default
import com.xcstasy.r.larva.core.kotlin.AtomicIntegerImpl
import io.reactivex.Flowable
import io.reactivex.exceptions.Exceptions
import io.reactivex.functions.Cancellable
import io.reactivex.internal.functions.ObjectHelper
import io.reactivex.internal.queue.SpscLinkedArrayQueue
import io.reactivex.internal.util.AtomicThrowable
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext


/**
 * @author Drc_ZeaRot
 * @since 2017/12/20
 * @lastModified by Drc_ZeaRot on 2017/12/20
 */
@Suppress("FunctionName")
fun <E, R> CombineLatestChannel(context: CoroutineContext = Unconfined,
                                sources: Array<ReceiveChannel<E>>,
                                combiner: (Array<Any>) -> R): ReceiveChannel<R> {
    if (sources.isEmpty()) return Channel<R>().apply { cancel() }
    return ChannelCombineLatest(context, sources, combiner)
}

class ChannelCombineLatest<E, R>(context: CoroutineContext = Unconfined,
                                 private val sources: Array<ReceiveChannel<E>>,
                                 combiner: (Array<Any>) -> R) : LinkedListChannel<R>() {

    private val oneTimeFlag: AtomicBoolean = AtomicBoolean(false)

    private val coordinator  = LatestCoordinator<E, R>(this, context, combiner, sources.size, 2 * Flowable.bufferSize())

    override fun onReceiveEnqueued() {
        if (!oneTimeFlag.get()) {
            coordinator.consume(sources)
            oneTimeFlag.compareAndSet(false, true)
        }
    }

    override fun onClosed(closed: Closed<R>) {
        coordinator.cancel()
    }
}

@Suppress("UNCHECKED_CAST")
private class LatestCoordinator<E, out R>(private val actual: Channel<R>,
                                          val context: CoroutineContext,
                                          private val combiner: (Array<Any>) -> R,
                                          count: Int, bufferSize: Int) : AtomicIntegerImpl(), Cancellable {

    @Volatile
    private var cancelled: Boolean = false

    @Volatile
    private var done: Boolean = false
    private val latest: Array<E?> = arrayOfNulls<Any>(count) as Array<E?>
    private val observers: Array<CombinerConsumer<E, R>?> = arrayOfNulls(count)

    private val queue: SpscLinkedArrayQueue<Any> = SpscLinkedArrayQueue(bufferSize)
    private val errors = AtomicThrowable()
    private var active: Int = 0

    private var complete: Int = 0

    fun consume(sources: Array<ReceiveChannel<E>>) {
        val cc: Array<CombinerConsumer<E, R>?> = observers
        for (index in cc.indices) {
            cc[index] = CombinerConsumer(this, index)
        }
        lazySet(0)
        for (index in cc.indices) {
            if (done || cancelled) return
            cc[index]?.consume(sources[index])
        }
    }

    override fun cancel() {
        if (!cancelled) {
            cancelled = true
            cancelSources()
            if (andIncrement == 0) {
                clear(queue)
            }
            actual.close()
        }
    }

    fun cancel(q: SpscLinkedArrayQueue<*>) {
        clear(q)
        cancelSources()
    }

    fun cancelSources() {
        for (s in observers) {
            s?.cancel()
        }
    }

    fun clear(q: SpscLinkedArrayQueue<*>) {
        synchronized(this) {
            Arrays.fill(latest, null)
        }
        q.clear()
    }

    fun combine(value: E?, index: Int) {
        val consumer: CombinerConsumer<E, R> = observers[index]!!
        var a: Int
        var c: Int
        var len: Int
        var empty: Boolean
        var f = false

        synchronized(this) {
            if (cancelled) {
                return
            }
            len = latest.size
            val o = latest[index]
            a = active
            if (o == null) {
                active = ++a
            }
            c = complete
            if (value == null) {
                complete = ++c
            } else {
                latest[index] = value
            }
            f = a == len
            // see if either all sources completed
            empty = c == len ||
                    value == null && o == null // or this source completed without any value
            if (!empty) {
                if (value != null && f) {
                    queue.offer(consumer, latest.clone())
                } else if (value == null && errors.get() != null) {
                    done = true // if this source completed without a value
                } else {

                }
            } else {
                done = true
            }
        }
        if (!f && value != null) {
            return
        }
        drain()
    }

    fun drain() {
        if (andIncrement != 0) {
            return
        }
        val q = queue
        val a = actual

        var missed = 1

        while (true) {

            if (checkTerminated(done, q.isEmpty, a, q)) {
                return
            }

            while (true) {
                val d = done
                val cs = q.poll() as? CombinerConsumer<E, R>
                val empty = cs == null

                if (checkTerminated(d, empty, a, q)) {
                    return
                }

                if (empty) {
                    break
                }

                val array = q.poll() as Array<Any>

                val v: R
                try {
                    v = ObjectHelper.requireNonNull<R>(combiner.invoke(array), "The combiner returned a null")
                } catch (ex: Throwable) {
                    Exceptions.throwIfFatal(ex)
                    cancelled = true
                    cancel(q)
                    a.close()
                    return
                }

                a.offer(v)
            }

            missed = addAndGet(-missed)
            if (missed == 0) {
                break
            }
        }

    }

    fun checkTerminated(d: Boolean, empty: Boolean, a: SendChannel<R>, q: SpscLinkedArrayQueue<*>): Boolean {
        if (cancelled) {
            cancel(q)
            return true
        }
        if (d) {
            val e = errors.get()
            if (e != null) {
                cancel(q)
                a.close(errors.terminate())
                return true
            } else if (empty) {
                clear(queue)
                a.close()
                return true
            }
        }
        return false
    }

    fun onError(e: Throwable) {
        if (!(errors.addThrowable(e))) {
            actual.cancel(e)
        }
    }
}

private class CombinerConsumer<in E, R>(private val parent: LatestCoordinator<E, R>, private val index: Int) : Cancellable {

    private var job: Job? = null

    override fun cancel() {
        job?.apply { if (!isCancelled) cancel() }
    }

    fun consume(source: ReceiveChannel<E>) {
        try {
            job = launch(parent.context.default) {
                source.consumeEach {
                    if (isActive) {
                        parent.combine(it, index)
                    }
                }
            }.apply { invokeOnCompletion { parent.combine(null, index) } }
        } catch (e: Exception) {
            parent.onError(e)
            parent.combine(null, index)
        }
    }
}