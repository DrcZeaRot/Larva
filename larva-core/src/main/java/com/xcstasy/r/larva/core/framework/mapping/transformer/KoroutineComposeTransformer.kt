package com.xcstasy.r.larva.core.framework.mapping.transformer

import android.text.TextUtils
import com.xcstasy.r.larva.core.framework.cleanstructure.data.ApiResult
import com.xcstasy.r.larva.core.framework.cleanstructure.data.UrlSignature
import com.xcstasy.r.larva.core.framework.exception.ObtainException
import com.xcstasy.r.larva.core.framework.network.const.HTTP_STATUS_ERROR
import com.xcstasy.r.larva.core.framework.network.const.OBTAIN_FAIL_MESSAGE
import com.xcstasy.r.larva.core.framework.network.const.RESTFUL_SUCCESS_CODE

/**
 * @author Drc_ZeaRot
 * @since 2017/11/24
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
fun <T : ApiResult<*>, R> T.apiTransform(): R =
        this.apiFilter().apiMap()

fun <T : ApiResult<*>> T.apiFilter(): T =
        if (dispatchFilter<T>()(this)) {
            this
        } else throw ObtainException("500", OBTAIN_FAIL_MESSAGE)

fun <T : ApiResult<*>, R> T.apiMap(): R =
        flatMapping<T, R>()(this)


private fun <T, R> flatMapping(): (T) -> R = {
    @Suppress("UNCHECKED_CAST")
    ((it as? ApiResult<*>)?.data as? R)?.let {
        it
    } ?: throw ObtainException("500", OBTAIN_FAIL_MESSAGE)
}

/**
 * 统一筛选
 */
private fun <T> dispatchFilter(): (T) -> Boolean =
        { result ->
            when (result) {
                is UrlSignature -> {
                    val tagReceiver = result as UrlSignature
                    (HTTP_STATUS_ERROR != tagReceiver.code).also {
                        if (!it) throw ObtainException(tagReceiver.code, tagReceiver.message, tagReceiver.tag)
                    }
                }
                is ApiResult<*> -> {
                    val apiResult = result as ApiResult<*>
                    (RESTFUL_SUCCESS_CODE.equals(apiResult.code, ignoreCase = true)).also {
                        if (!it) {
                            val showMsg = apiResult.showMsg
                            val message = if (TextUtils.isEmpty(showMsg)) {
                                if (TextUtils.isEmpty(apiResult.message)) {
                                    OBTAIN_FAIL_MESSAGE
                                } else apiResult.message
                            } else showMsg
                            throw ObtainException(apiResult.code, message, apiResult.tag)
                        }
                    }
                }
                is ObtainException -> {
                    throw result
                }
                else -> true
            }
        }

