@file:Suppress("NOTHING_TO_INLINE")

package com.xcstasy.r.larva.core.koroutine.extension

import com.xcstasy.r.larva.core.framework.mvvm.MView
import com.xcstasy.r.larva.core.framework.mvvm.MViewModel
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.android.UI
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.intrinsics.coroutineContext

/**
 * @author Drc_ZeaRot
 * @since 2017/11/24
 * @lastModified by Drc_ZeaRot on 2017/11/24
 */
inline val CoroutineContext.default: CoroutineContext
    get() = this + DefaultDispatcher

inline val CoroutineContext.ui: CoroutineContext
    get() = this + UI

inline val CoroutineScope.default: CoroutineContext
    get() = this.coroutineContext + DefaultDispatcher

inline val CoroutineScope.ui: CoroutineContext
    get() = this.coroutineContext + UI

inline val CoroutineScope.parent: CoroutineContext
    get() = this.coroutineContext

inline val MView<*>.job: CoroutineContext
    get() = this.mainJob

inline val MViewModel.job: CoroutineContext
    get() = this.baseView.mainJob

@Suppress("RedundantSuspendModifier")
suspend inline fun parent(): CoroutineContext = coroutineContext
