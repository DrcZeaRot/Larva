package com.xcstasy.r.larva.core.framework.databinding.viewbinding

import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * @author Drc_ZeaRot
 * @since 2017/12/18
 * @lastModified by Drc_ZeaRot on 2017/12/18
 */
interface ViewEventChannel<out T> {
    val receiveChannel: ReceiveChannel<T>
}