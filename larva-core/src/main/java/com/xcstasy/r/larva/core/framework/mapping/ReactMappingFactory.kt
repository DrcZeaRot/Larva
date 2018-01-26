package com.xcstasy.r.larva.core.framework.mapping

import android.app.Activity
import android.text.TextUtils
import com.xcstasy.r.larva.core.framework.cancelable.ReactDisposable
import com.xcstasy.r.larva.core.framework.cancelable.ReactSubscription
import com.xcstasy.r.larva.core.framework.cleanstructure.data.ApiResult
import com.xcstasy.r.larva.core.framework.cleanstructure.data.UrlSignature
import com.xcstasy.r.larva.core.framework.exception.ObtainException
import com.xcstasy.r.larva.core.framework.lifecycle.ActivityEvent
import com.xcstasy.r.larva.core.framework.lifecycle.FragmentEvent
import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleEvent
import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleTransformer
import com.xcstasy.r.larva.core.framework.mapping.transformer.*
import com.xcstasy.r.larva.core.framework.mvvm.ErrorUnifiedDispatcher
import com.xcstasy.r.larva.core.framework.mvvm.LoadingDispatcher
import com.xcstasy.r.larva.core.framework.mvvm.MView
import com.xcstasy.r.larva.core.framework.network.const.HTTP_STATUS_ERROR
import com.xcstasy.r.larva.core.framework.network.const.OBTAIN_FAIL_MESSAGE
import com.xcstasy.r.larva.core.framework.network.const.RESTFUL_SUCCESS_CODE
import io.reactivex.*
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import org.reactivestreams.Publisher

/**
 * @author Drc_ZeaRot
 * @since 2017/11/24
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
object ReactMappingFactory {

    /**
     * dispatch your react with Schedulers.io and AndroidSchedulers.main
     */
    fun <T> scheduler(): ReactComposeTransformer<T> = ScheduleTransformer()

    /**
     * control your react lifecycle with specified [LifecycleEvent]
     */
    fun <T> lifecycle(baseView: MView<*>, event: LifecycleEvent): LifecycleTransformer<T> =
            (if (event === ActivityEvent.DESTROY && baseView !is Activity) FragmentEvent.DESTROY_VIEW
            else event)
                    .let { baseView.bindUntilEvent(it) }

    /**
     * control your react lifecycle
     */
    fun <T> lifecycleNoEvent(baseView: MView<*>): LifecycleTransformer<T> =
            baseView.bindToLifecycle()

    /**
     * manage your react disposable
     */
    fun <T> disposer(baseView: MView<*>): ReactComposeTransformer<T> =
            object : ReactComposeTransformer<T> {
                override fun apply(upstream: Flowable<T>): Publisher<T> =
                        upstream.doOnSubscribe { baseView.addCancelable(ReactSubscription(it)) }

                override fun apply(upstream: Observable<T>): ObservableSource<T> =
                        upstream.doOnSubscribe { baseView.addCancelable(ReactDisposable(it)) }

                override fun apply(upstream: Single<T>): SingleSource<T> =
                        upstream.doOnSubscribe { baseView.addCancelable(ReactDisposable(it)) }

                override fun apply(upstream: Maybe<T>): MaybeSource<T> =
                        upstream.doOnSubscribe { baseView.addCancelable(ReactDisposable(it)) }

                override fun apply(upstream: Completable): CompletableSource =
                        upstream.doOnSubscribe { baseView.addCancelable(ReactDisposable(it)) }

            }

    /**
     * mainly used for show/hide progress
     */
    fun <T> startFinish(dispatcher: LoadingDispatcher): ReactComposeTransformer<T> =
            object : ReactComposeTransformer<T> {
                override fun apply(upstream: Flowable<T>): Publisher<T> =
                        upstream.doOnSubscribe(subscribeConsumer(dispatcher))
                                .doOnTerminate(disposeAction(dispatcher))
                                .doFinally(disposeAction(dispatcher))

                override fun apply(upstream: Single<T>): SingleSource<T> =
                        upstream.doOnSubscribe(subscribeConsumer(dispatcher))
                                .doOnSuccess(disposeConsumer(dispatcher))
                                .doFinally(disposeAction(dispatcher))

                override fun apply(upstream: Observable<T>): ObservableSource<T> =
                        upstream.doOnSubscribe(subscribeConsumer(dispatcher))
                                .doOnTerminate(disposeAction(dispatcher))
                                .doFinally(disposeAction(dispatcher))

                override fun apply(upstream: Maybe<T>): MaybeSource<T> =
                        upstream.doOnSubscribe(subscribeConsumer(dispatcher))
                                .doOnSuccess(disposeConsumer(dispatcher))
                                .doFinally(disposeAction(dispatcher))

                override fun apply(upstream: Completable): CompletableSource =
                        upstream.doOnSubscribe(subscribeConsumer(dispatcher))
                                .doOnTerminate(disposeAction(dispatcher))
                                .doFinally(disposeAction(dispatcher))

            }

    /**
     * filter your com.xcstasy.r.larva.core.framework.cleanstructure.data.ApiResult
     */
    fun <T> api(dispatcher: ErrorUnifiedDispatcher): ReactComposeTransformer4Data<T, T> =
            object : ReactComposeTransformer4Data<T, T> {
                override fun apply(upstream: Flowable<T>): Publisher<T> =
                        upstream.filter(obtainErrorPredicate<T>(dispatcher))
                                .doOnError(obtainErrorConsumer(dispatcher))
                                .onErrorResumeNext(Flowable.empty())

                override fun apply(upstream: Maybe<T>): MaybeSource<T> =
                        upstream.filter(obtainErrorPredicate<T>(dispatcher))
                                .doOnError(obtainErrorConsumer(dispatcher))
                                .onErrorResumeNext(Maybe.empty())

                override fun apply(upstream: Single<T>): SingleSource<T> =
                        upstream.filter(obtainErrorPredicate<T>(dispatcher))
                                .toSingle()
                                .doOnError(obtainErrorConsumer(dispatcher))

                override fun apply(upstream: Observable<T>): ObservableSource<T> =
                        upstream.filter(obtainErrorPredicate<T>(dispatcher))
                                .doOnError(obtainErrorConsumer(dispatcher))
                                .onErrorResumeNext(Observable.empty())
            }

    /**
     * filter your com.xcstasy.r.larva.core.framework.cleanstructure.data.ApiResult and map it to data
     */
    fun <T, R> api2Entity(dispatcher: ErrorUnifiedDispatcher): ReactComposeTransformer4Data<T, R> =
            object : ReactComposeTransformer4Data<T, R> {
                override fun apply(upstream: Flowable<T>): Publisher<R> =
                        upstream.filter(obtainErrorPredicate<T>(dispatcher))
                                .flatMap(Api2EntityFlowable<T, R>())
                                .doOnError(obtainErrorConsumer(dispatcher))

                override fun apply(upstream: Single<T>): SingleSource<R> =
                        upstream.filter(obtainErrorPredicate<T>(dispatcher))
                                .toSingle()
                                .flatMap(Api2EntitySingle<T, R>())
                                .doOnError(obtainErrorConsumer(dispatcher))

                override fun apply(upstream: Observable<T>): ObservableSource<R> =
                        upstream.filter(obtainErrorPredicate<T>(dispatcher))
                                .flatMap(Api2EntityObservable<T, R>())
                                .doOnError(obtainErrorConsumer(dispatcher))
                                .onErrorResumeNext(Observable.empty())

                override fun apply(upstream: Maybe<T>): MaybeSource<R> =
                        upstream.filter(obtainErrorPredicate<T>(dispatcher))
                                .flatMap(Api2EntityMaybe<T, R>())
                                .doOnError(obtainErrorConsumer(dispatcher))
                                .onErrorResumeNext(Maybe.empty())
            }

    /**
     * composition for lifecycle、disposer、scheduler, with specified [LifecycleEvent]
     */
    fun <T> default(baseView: MView<*>, event: LifecycleEvent): ReactComposeTransformer<T> =
            object : ReactComposeTransformer<T> {
                override fun apply(upstream: Flowable<T>): Publisher<T> =
                        upstream.compose(lifecycle(baseView, event))
                                .compose(disposer(baseView))
                                .compose(scheduler())

                override fun apply(upstream: Single<T>): SingleSource<T> =
                        upstream.compose(lifecycle(baseView, event))
                                .compose(disposer(baseView))
                                .compose(scheduler())

                override fun apply(upstream: Observable<T>): ObservableSource<T> =
                        upstream.compose(lifecycle(baseView, event))
                                .compose(disposer(baseView))
                                .compose(scheduler())

                override fun apply(upstream: Maybe<T>): MaybeSource<T> =
                        upstream.compose(lifecycle(baseView, event))
                                .compose(disposer(baseView))
                                .compose(scheduler())

                override fun apply(upstream: Completable): CompletableSource =
                        upstream.compose(lifecycle<T>(baseView, event))
                                .compose(disposer<T>(baseView))
                                .compose(scheduler<T>())
            }

    /**
     * composition for lifecycle、disposer、scheduler
     */
    fun <T> defaultNoEvent(baseView: MView<*>): ReactComposeTransformer<T> =
            object : ReactComposeTransformer<T> {
                override fun apply(upstream: Flowable<T>): Publisher<T> =
                        upstream.compose(lifecycleNoEvent(baseView))
                                .compose(disposer(baseView))
                                .compose(scheduler())

                override fun apply(upstream: Single<T>): SingleSource<T> =
                        upstream.compose(lifecycleNoEvent(baseView))
                                .compose(disposer(baseView))
                                .compose(scheduler())

                override fun apply(upstream: Observable<T>): ObservableSource<T> =
                        upstream.compose(lifecycleNoEvent(baseView))
                                .compose(disposer(baseView))
                                .compose(scheduler())

                override fun apply(upstream: Maybe<T>): MaybeSource<T> =
                        upstream.compose(lifecycleNoEvent(baseView))
                                .compose(disposer(baseView))
                                .compose(scheduler())

                override fun apply(upstream: Completable): CompletableSource =
                        upstream.compose(lifecycleNoEvent<T>(baseView))
                                .compose(disposer<T>(baseView))
                                .compose(scheduler<T>())
            }


    private fun <T> subscribeConsumer(dispatcher: LoadingDispatcher): Consumer<T> =
            Consumer {
                dispatcher.showLoading()
            }

    private fun <T> disposeConsumer(dispatcher: LoadingDispatcher): Consumer<T> =
            Consumer {
                dispatcher.hideLoading()
            }

    private fun disposeAction(dispatcher: LoadingDispatcher): Action =
            Action {
                dispatcher.hideLoading()
            }

    /**
     * 统一筛选
     */
    private fun <T> obtainErrorPredicate(dispatcher: ErrorUnifiedDispatcher): Predicate<T> =
            Predicate { result ->
                when (result) {
                    is UrlSignature -> {
                        val tagReceiver = result as UrlSignature
                        (HTTP_STATUS_ERROR != tagReceiver.code).also {
                            if (!it) dispatcher.onObtainFail(ObtainException(tagReceiver.code, tagReceiver.message, tagReceiver.tag))
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
                                dispatcher.onObtainFail(ObtainException(apiResult.code, message, apiResult.tag))
                            }
                        }
                    }
                    is ObtainException -> {
                        dispatcher.onObtainFail(result as ObtainException)
                        false
                    }
                    else -> true
                }
            }

    /**
     * 统一 错误处理
     */
    private fun obtainErrorConsumer(dispatcher: ErrorUnifiedDispatcher): Consumer<Throwable> =
            Consumer { throwable ->
                if (throwable is ObtainException) {
                    dispatcher.onObtainFail(throwable)
                } else {
                    throwable.printStackTrace()
                    dispatcher.onObtainFail(ObtainException("404", OBTAIN_FAIL_MESSAGE))
                }
            }
}