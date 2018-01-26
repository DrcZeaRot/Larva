package com.xcstasy.r.larva.core.framework.databinding.viewbinding.koroutinebinding

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import com.xcstasy.r.larva.core.framework.databinding.viewbinding.ViewEventChannel
import kotlinx.coroutines.experimental.CompletionHandler
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * @author Drc_ZeaRot
 * @since 2017/12/18
 * @lastModified by Drc_ZeaRot on 2017/12/18
 */
class TextViewAfterTextChangeHandler(private val view: TextView) : TextWatcher, CompletionHandler, ViewEventChannel<Pair<Editable, TextView>> {

    init {
        view.addTextChangedListener(this)
    }

    private val sendChannel = BroadcastChannel<Pair<Editable, TextView>>(Channel.CONFLATED)

    override val receiveChannel: ReceiveChannel<Pair<Editable, TextView>>
        get() = sendChannel.openSubscription()

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        if (!sendChannel.isClosedForSend) {
            s?.also { sendChannel.offer(it to view) }
        }
    }

    override fun invoke(cause: Throwable?) {
        view.removeTextChangedListener(this)
        sendChannel.close()
    }
}