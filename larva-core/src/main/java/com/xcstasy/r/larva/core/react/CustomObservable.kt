package com.xcstasy.r.larva.core.react

import com.xcstasy.r.larva.core.react.operator.ObservableZipLatest
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.*
import io.reactivex.functions.Function
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins


/**
 * @author Drc_ZeaRot
 * @since 2018/1/12
 * @lastModified by Drc_ZeaRot on 2018/1/12
 */
object CustomObservable {

    fun <T1, T2, R> zipLatest(
            source1: ObservableSource<out T1>, source2: ObservableSource<out T2>,
            zipper: BiFunction<in T1, in T2, out R>, delayError: Boolean = false): Observable<R> {
        return zipLatestArray<Any, R>(Functions.toFunction(zipper), delayError, source1, source2)
    }

    fun <T1, T2, T3, R> zipLatest(
            source1: ObservableSource<out T1>, source2: ObservableSource<out T2>, source3: ObservableSource<out T3>,
            zipper: Function3<in T1, in T2, in T3, out R>, delayError: Boolean = false): Observable<R> {
        return zipLatestArray<Any, R>(Functions.toFunction(zipper), delayError, source1, source2, source3)
    }

    fun <T1, T2, T3, T4, R> zipLatest(
            source1: ObservableSource<out T1>, source2: ObservableSource<out T2>, source3: ObservableSource<out T3>,
            source4: ObservableSource<out T4>, zipper: Function4<in T1, in T2, in T3, in T4, out R>, delayError: Boolean = false): Observable<R> {
        return zipLatestArray<Any, R>(Functions.toFunction(zipper), delayError, source1, source2, source3, source4)
    }

    fun <T1, T2, T3, T4, T5, R> zipLatest(
            source1: ObservableSource<out T1>, source2: ObservableSource<out T2>,
            source3: ObservableSource<out T3>, source4: ObservableSource<out T4>, source5: ObservableSource<out T5>,
            zipper: Function5<in T1, in T2, in T3, in T4, in T5, out R>, delayError: Boolean = false): Observable<R> {
        return zipLatestArray<Any, R>(Functions.toFunction(zipper), delayError, source1, source2, source3, source4, source5)
    }

    fun <T1, T2, T3, T4, T5, T6, R> zipLatest(
            source1: ObservableSource<out T1>, source2: ObservableSource<out T2>, source3: ObservableSource<out T3>,
            source4: ObservableSource<out T4>, source5: ObservableSource<out T5>, source6: ObservableSource<out T6>,
            zipper: Function6<in T1, in T2, in T3, in T4, in T5, in T6, out R>, delayError: Boolean = false): Observable<R> {
        return zipLatestArray<Any, R>(Functions.toFunction(zipper), delayError, source1, source2, source3, source4, source5, source6)
    }

    fun <T1, T2, T3, T4, T5, T6, T7, R> zipLatest(
            source1: ObservableSource<out T1>, source2: ObservableSource<out T2>, source3: ObservableSource<out T3>,
            source4: ObservableSource<out T4>, source5: ObservableSource<out T5>, source6: ObservableSource<out T6>,
            source7: ObservableSource<out T7>, zipper: Function7<in T1, in T2, in T3, in T4, in T5, in T6, in T7, out R>, delayError: Boolean = false): Observable<R> {
        return zipLatestArray<Any, R>(Functions.toFunction(zipper), delayError, source1, source2, source3, source4, source5, source6, source7)
    }

    fun <T1, T2, T3, T4, T5, T6, T7, T8, R> zipLatest(
            source1: ObservableSource<out T1>, source2: ObservableSource<out T2>, source3: ObservableSource<out T3>,
            source4: ObservableSource<out T4>, source5: ObservableSource<out T5>, source6: ObservableSource<out T6>,
            source7: ObservableSource<out T7>, source8: ObservableSource<out T8>,
            zipper: Function8<in T1, in T2, in T3, in T4, in T5, in T6, in T7, in T8, out R>, delayError: Boolean = false): Observable<R> {
        return zipLatestArray<Any, R>(Functions.toFunction(zipper), delayError, source1, source2, source3, source4, source5, source6, source7, source8)
    }

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> zipLatest(
            source1: ObservableSource<out T1>, source2: ObservableSource<out T2>, source3: ObservableSource<out T3>,
            source4: ObservableSource<out T4>, source5: ObservableSource<out T5>, source6: ObservableSource<out T6>,
            source7: ObservableSource<out T7>, source8: ObservableSource<out T8>, source9: ObservableSource<out T9>,
            zipper: Function9<in T1, in T2, in T3, in T4, in T5, in T6, in T7, in T8, in T9, out R>, delayError: Boolean = false): Observable<R> {
        return zipLatestArray<Any, R>(Functions.toFunction(zipper), delayError, source1, source2, source3, source4, source5, source6, source7, source8, source9)
    }

    fun <T, R> zipLatestArray(zipper: Function<in Array<Any>, out R>,
                              delayError: Boolean, vararg sources: ObservableSource<out T>): Observable<R> {
        if (sources.isEmpty()) {
            return Observable.empty()
        }
        return RxJavaPlugins.onAssembly(ObservableZipLatest(sources, null, zipper, delayError))
    }

    fun <T, R> zipLatestIterable(sources: Iterable<ObservableSource<out T>>,
                                 zipper: Function<in Array<Any>, out R>, delayError: Boolean): Observable<R> {
        return RxJavaPlugins.onAssembly(ObservableZipLatest(null, sources, zipper, delayError))
    }

}