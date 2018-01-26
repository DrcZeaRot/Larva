package com.xcstasy.r.larva.core.framework.lifecycle.koroutine

import com.xcstasy.r.larva.core.koroutine.extension.default
import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleEvent
import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleTransformer
import io.reactivex.annotations.CheckReturnValue
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlin.coroutines.experimental.CoroutineContext

/**
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2017/11/23
 */
object KoroutineLifecycle {

    /**
     * Binds the given source to a lifecycle.
     *
     * This helper automatically determines (based on the lifecycle sequence itself) when the source
     * should stop emitting items. Note that for this method, it assumes *any* event
     * emitted by the given lifecycle indicates that the lifecycle is over.
     *
     * @param receiver the ReceiveChannel
     * @return a reusable [LifecycleTransformer] that dispose the source whenever the lifecycle emits
     */
    @CheckReturnValue
    private fun <T> bind(receiver: ReceiveChannel<Boolean>): LifecycleTransformer<T> =
            KoroutineLifecycleTransformer(receiver)

    /**
     * Binds the given source to a lifecycle.
     *
     *
     * This method determines (based on the lifecycle sequence itself) when the source
     * should stop emitting items. It uses the provided correspondingEvents function to determine
     * when to dispose.
     *
     * Note that this is an advanced usage of the library and should generally be used only if you
     * really know what you're doing with a given lifecycle.
     *
     * @param lifecycle the lifecycle sequence
     * @param correspondingEvents a function which tells the source when to dispose
     * @return a reusable [LifecycleTransformer] that dispose the source during the Fragment lifecycle
     */
    @CheckReturnValue
    fun <T, R : LifecycleEvent> bind(lifecycle: BroadcastChannel<R>,
                                     correspondingEvents: (R) -> R,
                                     context: CoroutineContext = Unconfined): LifecycleTransformer<T> =
            bind(takeUntilCorrespondingEvent(lifecycle, correspondingEvents, context))

    private fun <R : LifecycleEvent> takeUntilCorrespondingEvent(
            lifecycle: BroadcastChannel<R>,
            correspondingEvents: (R) -> R, context: CoroutineContext = Unconfined): ReceiveChannel<Boolean> = produce(context.default) {
        val correspondingEvent = lifecycle.openSubscription().use { sub ->
            sub.receive().let(correspondingEvents)
        }
        val subscription = lifecycle.openSubscription()
        subscription.consumeEach {
            if (it === correspondingEvent) {
                send(true)
                subscription.close()
            }
        }
    }

    /**
     * Binds the given source to a lifecycle.
     *
     *
     * When the lifecycle event occurs, the source will cease to emit any notifications.
     *
     * @param lifecycle the lifecycle sequence
     * @param event the event which should conclude notifications from the source
     * @return a reusable [LifecycleTransformer] that dispose the source at the specified event
     */
    @CheckReturnValue
    fun <T, R : LifecycleEvent> bindUntilEvent(
            lifecycle: BroadcastChannel<R>,
            event: R, context: CoroutineContext = Unconfined): LifecycleTransformer<T> =
            bind(takeUntilEvent(lifecycle, event, context))

    private fun <R : LifecycleEvent> takeUntilEvent(
            lifecycle: BroadcastChannel<R>,
            event: R, context: CoroutineContext = Unconfined): ReceiveChannel<Boolean> = produce(context.default) {
        val subscription = lifecycle.openSubscription()
        subscription.consumeEach {
            if (it === event) {
                send(true)
                subscription.close()
            }
        }
    }
}