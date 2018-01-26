package com.xcstasy.r.larva.core.functional.functionmapper


fun <T1, T2, R> ((T1, T2) -> R).arrayParam(): (Array<Any>) -> R = Array2Function(this)
fun <T1, T2, T3, R> ((T1, T2, T3) -> R).arrayParam(): (Array<Any>) -> R = Array3Function(this)
fun <T1, T2, T3, T4, R> ((T1, T2, T3, T4) -> R).arrayParam(): (Array<Any>) -> R = Array4Function(this)
fun <T1, T2, T3, T4, T5, R> ((T1, T2, T3, T4, T5) -> R).arrayParam(): (Array<Any>) -> R = Array5Function(this)

