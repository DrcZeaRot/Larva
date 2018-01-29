package com.xcstasy.r.larva.core.framework.network.calladapter

import com.xcstasy.r.larva.core.framework.cleanstructure.data.ApiResult
import com.xcstasy.r.larva.core.framework.cleanstructure.data.UrlSignature
import com.xcstasy.r.larva.core.framework.network.const.HTTP_STATUS_ERROR
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author Drc_ZeaRot
 * @since 2017/11/22
 * @lastModified by Drc_ZeaRot on 2017/11/22
 */
class KoroutineCallAdapterFactory : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<out Annotation>?, retrofit: Retrofit?): CallAdapter<*, *>? {
        if (getRawType(returnType) !== ApiResult::class.java) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException("ApiResult return type must be parameterized as ApiResult<Foo> or ApiResult<in Foo>")
        }
        return BodyCallAdapter<ApiResult<*>>(returnType)
    }


    private class BodyCallAdapter<R>(
            private val responseType: Type
    ) : CallAdapter<R, R> {

        override fun responseType(): Type = responseType

        override fun adapt(call: Call<R>): R = runBlocking {
            suspendBody(call)
        }

        @Suppress("UNCHECKED_CAST")
        private suspend fun suspendBody(call: Call<R>): R =
                suspendCancellableCoroutine { cont ->
                    call.enqueue(object : Callback<R> {
                        override fun onResponse(innerCall: Call<R>, response: Response<R>?) {
                            bothNotNull(response, response?.body()) { resp, body ->
                                if (cont.isActive) {
                                    if (resp.isSuccessful) {
                                        if (body is UrlSignature) {
                                            body.tag = innerCall.request().url().toString()
                                        }
                                        cont.resume(body)
                                    } else {
                                        cont.resume(FailureReceiver(innerCall) as R)
                                    }
                                }
                            }
                        }

                        override fun onFailure(innerCall: Call<R>, t: Throwable) {
                            if (cont.isActive) {
                                cont.resume(FailureReceiver(innerCall) as R)
                            }
                        }
                    })
                    cont.invokeOnCompletion {
                        call.cancel()
                    }
                }

    }

}

class FailureReceiver(call: Call<*>) : UrlSignature {
    override val code: String = HTTP_STATUS_ERROR
    override val message: String = "网络链接失败，请稍后重试"
    override var tag: String = call.request().url().toString()
}

private inline fun <L, R> bothNotNull(left: L?, right: R?, block: (L, R) -> Unit) {
    if (left != null && right != null) {
        block(left, right)
    }
}