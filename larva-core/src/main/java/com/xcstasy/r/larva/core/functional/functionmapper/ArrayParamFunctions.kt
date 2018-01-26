package com.xcstasy.r.larva.core.functional.functionmapper

/**
 * @author Drc_ZeaRot
 * @since 2017/12/20
 * @lastModified by Drc_ZeaRot on 2017/12/20
 */
class Array2Function<T1, T2, out R>(private val function: Function2<T1, T2, R>) : (Array<Any>) -> R {
    override fun invoke(a: Array<Any>): R {
        if (a.size != 2) {
            throw IllegalArgumentException("Array of size 2 expected but got " + a.size)
        }
        @Suppress("UNCHECKED_CAST")
        return function.invoke(a[0] as T1, a[1] as T2)
    }
}

class Array3Function<T1, T2, T3, out R>(private val function: Function3<T1, T2, T3, R>) : (Array<Any>) -> R {
    override fun invoke(a: Array<Any>): R {
        if (a.size != 3) {
            throw IllegalArgumentException("Array of size 3 expected but got " + a.size)
        }
        @Suppress("UNCHECKED_CAST")
        return function.invoke(a[0] as T1, a[1] as T2, a[2] as T3)
    }
}

class Array4Function<T1, T2, T3, T4, out R>(private val function: Function4<T1, T2, T3, T4, R>) : (Array<Any>) -> R {
    override fun invoke(a: Array<Any>): R {
        if (a.size != 4) {
            throw IllegalArgumentException("Array of size 4 expected but got " + a.size)
        }
        @Suppress("UNCHECKED_CAST")
        return function.invoke(a[0] as T1, a[1] as T2, a[2] as T3, a[3] as T4)
    }
}

class Array5Function<T1, T2, T3, T4, T5, out R>(private val function: Function5<T1, T2, T3, T4, T5, R>) : (Array<Any>) -> R {
    override fun invoke(a: Array<Any>): R {
        if (a.size != 5) {
            throw IllegalArgumentException("Array of size 5 expected but got " + a.size)
        }
        @Suppress("UNCHECKED_CAST")
        return function.invoke(a[0] as T1, a[1] as T2, a[2] as T3, a[3] as T4, a[5] as T5)
    }
}