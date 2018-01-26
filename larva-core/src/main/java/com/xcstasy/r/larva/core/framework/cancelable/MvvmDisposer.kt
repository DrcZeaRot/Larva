package com.xcstasy.r.larva.core.framework.cancelable

/**
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
interface MvvmDisposer {
    fun addCancelable(cancelable: MvvmCancelable)
}