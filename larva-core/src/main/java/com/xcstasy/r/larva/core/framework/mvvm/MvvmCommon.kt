package com.xcstasy.r.larva.core.framework.mvvm

import com.xcstasy.r.larva.core.framework.exception.ObtainException
import kotlinx.coroutines.experimental.Job

/**
 * @author Drc_ZeaRot
 * @since 2018/1/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
interface ErrorUnifiedDispatcher {
    fun onObtainFail(e: ObtainException)
}

interface LoadingDispatcher {
    fun showLoading()
    fun hideLoading()
}

interface JobHolder {
    val mainJob: Job
}