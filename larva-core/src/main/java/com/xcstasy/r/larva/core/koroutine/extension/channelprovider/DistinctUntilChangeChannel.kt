package com.xcstasy.r.larva.core.koroutine.extension.channelprovider

import io.reactivex.internal.functions.ObjectHelper
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlin.coroutines.experimental.CoroutineContext

/**
 * @author Drc_ZeaRot
 * @since 2017/12/20
 * @lastModified by Drc_ZeaRot on 2017/12/20
 */
fun <E, K> DistinctUntilChangeChannel(source: ReceiveChannel<E>, keySelector: (E) -> K, context: CoroutineContext = Unconfined): ReceiveChannel<E> =
        produce(context) {
            var last: K? = null
            var hasValue = false
            source.consumeEach {
                val key = keySelector(it)
                if (hasValue) {
                    val equals = ObjectHelper.equals(last, key)
                    last = key
                    if (!equals) send(it)
                } else {
                    hasValue = true
                    last = key
                    send(it)
                }
            }
        }