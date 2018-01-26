package com.xcstasy.r.larva.core.framework.cleanstructure.data.koroutine

import com.xcstasy.r.larva.core.koroutine.extension.distinctUntilChanged
import com.xcstasy.r.larva.core.framework.cleanstructure.data.IKoroutineDataFlow
import com.xcstasy.r.larva.core.framework.mvvm.MView
import com.xcstasy.r.larva.core.koroutine.extension.default
import com.xcstasy.r.larva.core.koroutine.extension.job
import com.xcstasy.r.larva.core.koroutine.extension.parent
import com.xcstasy.r.larva.core.koroutine.extension.ui
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2017/11/23
 */
class KoroutineDataFlow<T>
private constructor(private val verifier: ((T) -> Boolean)?,
                    private val messageBlock: (() -> Unit)?) : IKoroutineDataFlow<T> {

    private val sendChannel = BroadcastChannel<T>(Channel.CONFLATED)

    override var value: T
        set(value) {
            latestValue = value
            sendChannel.offer(value)
        }
        get() = latestValue ?: throw NoSuchElementException()

    override var latestValue: T? = null
        set(value) {
            value?.let { field = it }
        }
        get() = latestValue?.let { value ->
            verifier?.invoke(value)?.let { valid -> if (valid) value else messageBlock?.invoke()?.let { null } }
        }

    override fun validator(baseView: MView<*>): ReceiveChannel<Boolean> =
            sendChannel.openSubscription().map { verifier?.invoke(it) ?: true }.distinctUntilChanged()

    override fun open(): ReceiveChannel<T> = sendChannel.openSubscription()

    override fun observe(baseView: MView<*>, onChange: (T) -> Unit) {
        launch(baseView.job.default) {
            sendChannel.openSubscription()
                    .consumeEach { withContext(parent.ui) { onChange(it) } }
        }.invokeOnCompletion { sendChannel.close() }
    }

    companion object : IKoroutineDataFlow.Creator {
        override fun <T> empty(messageBlock: (() -> Unit)?, verifier: ((T) -> Boolean)?) = KoroutineDataFlow(verifier, messageBlock)

        override fun <T> withDefault(default: T, messageBlock: (() -> Unit)?, verifier: ((T) -> Boolean)?) = KoroutineDataFlow(verifier, messageBlock).apply { value = default }
    }
}