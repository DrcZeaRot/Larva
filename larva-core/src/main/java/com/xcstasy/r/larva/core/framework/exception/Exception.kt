package com.xcstasy.r.larva.core.framework.exception

import java.lang.Exception

/**
 * @author Drc_ZeaRot
 * @since 2018/1/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
class ObtainException(message: String,
                      val code: String = "500",
                      val tag: String = "") : Exception(message)