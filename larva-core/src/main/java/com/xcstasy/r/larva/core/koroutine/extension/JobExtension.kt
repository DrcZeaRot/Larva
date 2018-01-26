@file:Suppress("NOTHING_TO_INLINE")

package com.xcstasy.r.larva.core.koroutine.extension

import android.app.Activity
import com.xcstasy.r.larva.core.framework.lifecycle.*
import com.xcstasy.r.larva.core.framework.mvvm.MView
import com.xcstasy.r.larva.core.koroutine.JobTransformer
import kotlinx.coroutines.experimental.Job

/**
 * @author Drc_ZeaRot
 * @since 2017/11/27
 * @lastModified by Drc_ZeaRot on 2017/11/27
 */
inline fun Job.composeJob(transformer: JobTransformer): Job =
        transformer.transformJob(this)

fun Job.lifecycle(baseView: MView<*>, event: LifecycleEvent = ActivityEvent.DESTROY): Job =
        koroutineLifecycleTransformer<Unit>(baseView, event).let { composeJob(it) }

fun Job.lifecycleNoEvent(baseView: MView<*>): Job =
        koroutineLifecycleNoEventTransformer<Unit>(baseView).let { composeJob(it) }

inline fun <T> koroutineLifecycleTransformer(
        baseView: MView<*>,
        event: LifecycleEvent = ActivityEvent.DESTROY): LifecycleTransformer<T> =
        (if (event === ActivityEvent.DESTROY && baseView !is Activity) FragmentEvent.DESTROY_VIEW
        else event)
                .let { baseView.bindUntilEvent(it, ProviderType.KOROUTINE) }

inline fun <T> koroutineLifecycleNoEventTransformer(baseView: MView<*>): LifecycleTransformer<T> =
        baseView.bindToLifecycle(ProviderType.KOROUTINE)
