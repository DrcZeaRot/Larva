package com.xcstasy.r.larva.core.framework.lifecycle.react

import com.xcstasy.r.larva.core.framework.lifecycle.*
import com.xcstasy.r.larva.core.framework.mapping.transformer.ReactComposeTransformer
import io.reactivex.*
import io.reactivex.functions.Function
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import org.reactivestreams.Publisher
import java.util.concurrent.CancellationException

/**
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
interface ReactLifecycleTransformer<T> : ReactComposeTransformer<T>

class ReactLifecycleTransformerImpl<T>(private val observable: Observable<*>) : LifecycleTransformer<T> {
    init {
        checkNotNull(observable, "observable == null")
    }

    override fun transformJob(job: Job): Job = job

    override fun transform(deferred: Deferred<T>): Deferred<T> = deferred

    override fun apply(upstream: Observable<T>): ObservableSource<T> = upstream.takeUntil(observable)

    override fun apply(upstream: Flowable<T>): Publisher<T> = upstream.takeUntil(observable.toFlowable(BackpressureStrategy.LATEST))

    override fun apply(upstream: Single<T>): SingleSource<T> = upstream.takeUntil(observable.firstOrError())

    override fun apply(upstream: Maybe<T>): MaybeSource<T> = upstream.takeUntil(observable.firstElement())

    override fun apply(upstream: Completable): CompletableSource =
            Completable.ambArray(upstream,
                    observable.flatMapCompletable({ Completable.error(CancellationException()) }))

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as ReactLifecycleTransformerImpl<*>

        return observable == that.observable
    }

    override fun hashCode(): Int = observable.hashCode()

    override fun toString(): String {
        return "ReactLifecycleTransformerImpl{" +
                "observable=" + observable +
                '}'
    }
}

val ACTIVITY_LIFECYCLE_REACT: Function<LifecycleEvent, LifecycleEvent> = Function {
    when (it) {
        ActivityEvent.CREATE, ActivityEvent.STOP -> ActivityEvent.DESTROY
        ActivityEvent.START, ActivityEvent.PAUSE -> ActivityEvent.STOP
        ActivityEvent.RESUME -> ActivityEvent.PAUSE
        CommonEventMvvm.DIALOG_SHOW -> CommonEventMvvm.DIALOG_DISMISS
        ActivityEvent.DESTROY -> throw OutsideLifecycleException("Cannot bind to Fragment lifecycle when outside of it.")
        else -> throw UnsupportedOperationException("Binding to $it not yet implemented")
    }
}
val FRAGMENT_LIFECYCLE_REACT: Function<LifecycleEvent, LifecycleEvent> = Function {
    when (it) {
        FragmentEvent.ATTACH, FragmentEvent.DESTROY -> FragmentEvent.DETACH
        FragmentEvent.CREATE, FragmentEvent.DESTROY_VIEW -> FragmentEvent.DESTROY
        FragmentEvent.CREATE_VIEW, FragmentEvent.STOP -> FragmentEvent.DESTROY_VIEW
        FragmentEvent.START, FragmentEvent.PAUSE -> FragmentEvent.STOP
        FragmentEvent.RESUME -> FragmentEvent.PAUSE
        CommonEventMvvm.DIALOG_SHOW -> CommonEventMvvm.DIALOG_DISMISS
        FragmentEvent.DETACH -> throw OutsideLifecycleException("Cannot bind to Fragment lifecycle when outside of it.")
        else -> throw UnsupportedOperationException("Binding to $it not yet implemented")
    }
}