@file:Suppress("NOTHING_TO_INLINE")

package com.xcstasy.r.larva.core.koroutine.extension

import com.xcstasy.r.larva.core.framework.lifecycle.ActivityEvent
import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleEvent
import com.xcstasy.r.larva.core.framework.mvvm.MView
import com.xcstasy.r.larva.core.koroutine.DeferredTransformer
import kotlinx.coroutines.experimental.Deferred
/**
 * @author Drc_ZeaRot
 * @since 2017/11/24
 * @lastModified by Drc_ZeaRot on 2017/11/24
 */

inline fun <T, R> Deferred<T>.compose(transformer: DeferredTransformer<T, R>): Deferred<R> =
        transformer.transform(this)

fun <T> Deferred<T>.lifecycle(baseView: MView<*>, event: LifecycleEvent = ActivityEvent.DESTROY): Deferred<T> =
        koroutineLifecycleTransformer<T>(baseView, event).let { compose(it) }

fun <T> Deferred<T>.lifecycleNoEvent(baseView: MView<*>): Deferred<T> =
        koroutineLifecycleNoEventTransformer<T>(baseView).let { compose(it) }