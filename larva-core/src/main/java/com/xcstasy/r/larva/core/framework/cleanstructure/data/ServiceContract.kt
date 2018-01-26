package com.xcstasy.r.larva.core.framework.cleanstructure.data

/**
 * @author Drc_ZeaRot
 * @since 2017/11/9
 * @lastModified by Drc_ZeaRot on 2017/11/9
 */

interface IServiceProvider<out S> : () -> S

interface UrlSignature {
    val code: String
    val message: String
    var tag: String
}

interface IApiResult<out E> : UrlSignature {
    val showMsg: String
    val data: E?
}

class ApiResult<out E>(
        override val showMsg: String = "",
        override val data: E? = null,
        override val message: String = "",
        override val code: String = "",
        override var tag: String = ""
) : IApiResult<E>