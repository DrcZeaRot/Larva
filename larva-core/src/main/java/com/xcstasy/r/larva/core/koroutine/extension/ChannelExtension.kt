package com.xcstasy.r.larva.core.koroutine.extension

import com.xcstasy.r.larva.core.functional.functionmapper.arrayParam
import com.xcstasy.r.larva.core.koroutine.extension.channelprovider.CombineLatestChannel
import com.xcstasy.r.larva.core.koroutine.extension.channelprovider.DistinctUntilChangeChannel
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlin.coroutines.experimental.CoroutineContext

/**
 * @author Drc_ZeaRot
 * @since 2017/12/20
 * @lastModified by Drc_ZeaRot on 2017/12/20
 */
fun <E, R> Array<ReceiveChannel<E>>.combineLatest(context: CoroutineContext = Unconfined,
                                                  combiner: (Array<Any>) -> R): ReceiveChannel<R> =
        CombineLatestChannel(context, this, combiner)

fun <E, R> List<ReceiveChannel<E>>.combineLatest(context: CoroutineContext = Unconfined,
                                                 combiner: (Array<Any>) -> R): ReceiveChannel<R> =
        this.toTypedArray().combineLatest(context, combiner)

fun <E1, E2, R> ReceiveChannel<E1>.combineLatest(
        source2: ReceiveChannel<E2>,
        context: CoroutineContext = Unconfined,
        combiner: (E1, E2) -> R): ReceiveChannel<R> =
        arrayOf(this, source2).combineLatest(context, combiner = combiner.arrayParam())

fun <E1, E2, E3, R> ReceiveChannel<E1>.combineLatest(
        source2: ReceiveChannel<E2>,
        source3: ReceiveChannel<E3>,
        context: CoroutineContext = Unconfined,
        combiner: (E1, E2, E3) -> R): ReceiveChannel<R> =
        arrayOf(this, source2, source3).combineLatest(context, combiner = combiner.arrayParam())

fun <E1, E2, E3, E4, R> ReceiveChannel<E1>.combineLatest(
        source2: ReceiveChannel<E2>,
        source3: ReceiveChannel<E3>,
        source4: ReceiveChannel<E4>,
        context: CoroutineContext = Unconfined,
        combiner: (E1, E2, E3, E4) -> R): ReceiveChannel<R> =
        arrayOf(this, source2, source3, source4).combineLatest(context, combiner = combiner.arrayParam())

fun <E1, E2, E3, E4, E5, R> ReceiveChannel<E1>.combineLatest(
        source2: ReceiveChannel<E2>,
        source3: ReceiveChannel<E3>,
        source4: ReceiveChannel<E4>,
        source5: ReceiveChannel<E5>,
        context: CoroutineContext = Unconfined,
        combiner: (E1, E2, E3, E4, E5) -> R): ReceiveChannel<R> =
        arrayOf(this, source2, source3, source4, source5).combineLatest(context, combiner = combiner.arrayParam())

fun <E> ReceiveChannel<E>.distinctUntilChanged(context: CoroutineContext = Unconfined): ReceiveChannel<E> = distinctUntilChanged(context) { it }

fun <E, K> ReceiveChannel<E>.distinctUntilChanged(context: CoroutineContext = Unconfined, keySelector: (E) -> K): ReceiveChannel<E> =
        DistinctUntilChangeChannel(this, keySelector, context)