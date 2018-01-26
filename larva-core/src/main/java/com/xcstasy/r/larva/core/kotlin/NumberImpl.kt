package com.xcstasy.r.larva.core.kotlin

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Drc_ZeaRot
 * @since 2018/1/19
 * @lastModified by Drc_ZeaRot on 2018/1/19
 */

open class AtomicIntegerImpl : AtomicInteger() {
    override fun toByte(): Byte = 0
    override fun toChar(): Char = '0'
    override fun toShort(): Short = 0
}