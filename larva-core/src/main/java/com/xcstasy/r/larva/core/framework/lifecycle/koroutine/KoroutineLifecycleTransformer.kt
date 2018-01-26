package com.xcstasy.r.larva.core.framework.lifecycle.koroutine

import com.xcstasy.r.larva.core.koroutine.extension.default
import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleTransformer
import io.reactivex.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.reactivestreams.Publisher

/**
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2017/11/23
 */
class KoroutineLifecycleTransformer<T>(private val receiver: ReceiveChannel<Boolean>) : LifecycleTransformer<T> {

    override fun apply(upstream: Observable<T>): ObservableSource<T> = upstream

    override fun apply(upstream: Flowable<T>): Publisher<T> = upstream

    override fun apply(upstream: Maybe<T>): MaybeSource<T> = upstream

    override fun apply(upstream: Single<T>): SingleSource<T> = upstream

    override fun apply(upstream: Completable): CompletableSource = upstream

    override fun transform(deferred: Deferred<T>): Deferred<T> {
        launch(deferred.default) {
            receiver.consumeEach {
                if (isActive && it) {
                    deferred.cancel()
                    receiver.cancel()
                }
            }
        }
        return deferred
    }

    override fun transformJob(job: Job): Job {
        launch(job.default) {
            receiver.consumeEach {
                if (isActive && it) {
                    job.cancel()
                    receiver.cancel()
                }
            }
        }
        return job
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KoroutineLifecycleTransformer<*>) return false

        if (receiver != other.receiver) return false

        return true
    }

    override fun hashCode(): Int {
        return receiver.hashCode()
    }

    override fun toString(): String {
        return "KoroutineLifecycleTransformer(receiver=$receiver)"
    }


}