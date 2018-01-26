package com.xcstasy.r.larva.core.react

import android.os.Looper
import io.reactivex.Observer

/**
 * @author Drc_ZeaRot
 * @since 2018/1/10
 * @lastModified by Drc_ZeaRot on 2018/1/10
 */
fun checkMainThread(observer: Observer<*>): Boolean {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        observer.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
        return false
    }
    return true
}