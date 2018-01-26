@file:Suppress("UNCHECKED_CAST")

package com.xcstasy.r.larva.core.framework.mapping.transformer

import com.xcstasy.r.larva.core.framework.cleanstructure.data.ApiResult
import com.xcstasy.r.larva.core.framework.network.const.OBTAIN_FAIL_MESSAGE
import com.xcstasy.r.larva.core.framework.exception.ObtainException
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher

/**
 * @author Drc_ZeaRot
 * @since 2017/11/7
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */

interface ReactComposeTransformer<T> :
        ObservableTransformer<T, T>,
        SingleTransformer<T, T>,
        MaybeTransformer<T, T>,
        FlowableTransformer<T, T>,
        CompletableTransformer

interface ReactComposeTransformer4Data<T, R> :
        ObservableTransformer<T, R>,
        SingleTransformer<T, R>,
        FlowableTransformer<T, R>,
        MaybeTransformer<T, R>

class Api2EntityObservable<T, R> : Function<T, Observable<R>> {
    override fun apply(result: T): Observable<R> =
            ((result as? ApiResult<*>)?.data as? R)?.let {
                Observable.just(it)
            } ?: Observable.error(ObtainException("500", OBTAIN_FAIL_MESSAGE))
}

class Api2EntitySingle<T, R> : Function<T, Single<R>> {
    override fun apply(result: T): Single<R> =
            ((result as? ApiResult<*>)?.data as? R)?.let {
                Single.just(it)
            } ?: Single.error(ObtainException("500", OBTAIN_FAIL_MESSAGE))
}

class Api2EntityMaybe<T, R> : Function<T, Maybe<R>> {
    override fun apply(result: T): Maybe<R> =
            ((result as? ApiResult<*>)?.data as? R)?.let {
                Maybe.just(it)
            } ?: Maybe.error(ObtainException("500", OBTAIN_FAIL_MESSAGE))

}

class Api2EntityFlowable<T, R> : Function<T, Flowable<R>> {
    override fun apply(result: T): Flowable<R> =
            ((result as? ApiResult<*>)?.data as? R)?.let {
                Flowable.just(it)
            } ?: Flowable.error(ObtainException("500", OBTAIN_FAIL_MESSAGE))

}

class ScheduleTransformer<T> : ReactComposeTransformer<T> {
    override fun apply(upstream: Flowable<T>): Publisher<T> =
            upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun apply(upstream: Observable<T>): ObservableSource<T> =
            upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun apply(upstream: Single<T>): SingleSource<T> =
            upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun apply(upstream: Maybe<T>): MaybeSource<T> =
            upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun apply(upstream: Completable): CompletableSource =
            upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
}