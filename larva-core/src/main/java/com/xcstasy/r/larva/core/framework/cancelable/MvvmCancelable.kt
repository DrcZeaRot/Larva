package com.xcstasy.r.larva.core.framework.cancelable

import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription

/**
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */

interface MvvmCancelable {
    fun isCanceled(): Boolean
    fun cancel()
}

class ReactDisposable(private val value: Disposable) : MvvmCancelable {
    override fun isCanceled(): Boolean = value.isDisposed
    override fun cancel() = value.dispose()
}

class ReactSubscription(private val value: Subscription) : MvvmCancelable {
    override fun isCanceled(): Boolean = false
    override fun cancel() = value.cancel()
}