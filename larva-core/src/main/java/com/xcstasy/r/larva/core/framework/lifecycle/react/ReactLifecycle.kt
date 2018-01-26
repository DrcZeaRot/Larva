package com.xcstasy.r.larva.core.framework.lifecycle.react

import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleTransformer
import com.xcstasy.r.larva.core.framework.lifecycle.OutsideLifecycleException
import io.reactivex.Observable
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.exceptions.Exceptions
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function

/**
 * @author Drc_ZeaRot
 * @since 2018/1/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
fun <T> checkNotNull(value: T?, message: String): T {
    if (value == null) {
        throw NullPointerException(message)
    }
    return value
}

object ReactLifecycle {
    /**
     * Binds the given source to a lifecycle.
     *
     *
     * This helper automatically determines (based on the lifecycle sequence itself) when the source
     * should stop emitting items. Note that for this method, it assumes *any* event
     * emitted by the given lifecycle indicates that the lifecycle is over.
     *
     * @param lifecycle the lifecycle sequence
     * @return a reusable [ReactLifecycleTransformer] that unsubscribes the source whenever the lifecycle emits
     */
    @CheckReturnValue
    private fun <T, R> bind(lifecycle: Observable<R>): LifecycleTransformer<T> =
            ReactLifecycleTransformerImpl(lifecycle)

    /**
     * Binds the given source to a lifecycle.
     *
     *
     * This method determines (based on the lifecycle sequence itself) when the source
     * should stop emitting items. It uses the provided correspondingEvents function to determine
     * when to unsubscribe.
     *
     *
     * Note that this is an advanced usage of the library and should generally be used only if you
     * really know what you're doing with a given lifecycle.
     *
     * @param lifecycle the lifecycle sequence
     * @param correspondingEvents a function which tells the source when to unsubscribe
     * @return a reusable [ReactLifecycleTransformer] that unsubscribes the source during the Fragment lifecycle
     */
    @CheckReturnValue
    fun <T, R> bind(lifecycle: Observable<R>,
                    correspondingEvents: Function<R, R>): LifecycleTransformer<T> {
        checkNotNull(lifecycle, "lifecycle == null")
        checkNotNull(correspondingEvents, "correspondingEvents == null")
        return bind(takeUntilCorrespondingEvent(lifecycle.share(), correspondingEvents))
    }

    private fun <R> takeUntilCorrespondingEvent(lifecycle: Observable<R>,
                                                correspondingEvents: Function<R, R>): Observable<Boolean> {
        return Observable.combineLatest(
                lifecycle.take(1).map(correspondingEvents),
                lifecycle.skip(1),
                BiFunction<R, R, Boolean> { bindUntilEvent, lifecycleEvent -> lifecycleEvent == bindUntilEvent })
                .onErrorReturn { throwable ->
                    if (throwable is OutsideLifecycleException) {
                        return@onErrorReturn true
                    }
                    Exceptions.propagate(throwable)
                    false
                }
                .filter { shouldComplete -> shouldComplete }
    }

    /**
     * Binds the given source to a lifecycle.
     *
     *
     * When the lifecycle event occurs, the source will cease to emit any notifications.
     *
     * @param lifecycle the lifecycle sequence
     * @param event the event which should conclude notifications from the source
     * @return a reusable [ReactLifecycleTransformer] that unsubscribes the source at the specified event
     */
    @CheckReturnValue
    fun <T, R> bindUntilEvent(lifecycle: Observable<R>,
                              event: R): LifecycleTransformer<T> {
        checkNotNull(lifecycle, "lifecycle == null")
        checkNotNull(event, "event == null")
        return bind(takeUntilEvent(lifecycle, event))
    }

    private fun <R> takeUntilEvent(lifecycle: Observable<R>, event: R): Observable<R> =
            lifecycle.filter { lifecycleEvent -> lifecycleEvent == event }
}