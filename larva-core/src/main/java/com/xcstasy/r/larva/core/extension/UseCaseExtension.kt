@file:Suppress("NOTHING_TO_INLINE")

package com.xcstasy.r.larva.core.extension

import com.xcstasy.r.larva.core.framework.cleanstructure.domain.usecase.IDeferredParamUseCase
import com.xcstasy.r.larva.core.framework.cleanstructure.domain.usecase.IDeferredUseCase
import com.xcstasy.r.larva.core.framework.exception.ObtainException
import com.xcstasy.r.larva.core.framework.mvvm.ErrorUnifiedDispatcher
import com.xcstasy.r.larva.core.framework.mvvm.LoadingDispatcher
import com.xcstasy.r.larva.core.framework.mvvm.MViewModel
import com.xcstasy.r.larva.core.framework.network.const.OBTAIN_FAIL_MESSAGE
import com.xcstasy.r.larva.core.functional.type.*
import com.xcstasy.r.larva.core.koroutine.extension.default
import com.xcstasy.r.larva.core.koroutine.extension.parent
import com.xcstasy.r.larva.core.koroutine.extension.ui
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * 先显示Loading，然后对mergeBlock进行并发执行，并挂起当前协程。全部执行完毕后，隐藏Loading
 * @param dispatcher Loading分发器
 */
suspend fun <T> Array<suspend () -> T>.mergeWithLoading(
        dispatcher: LoadingDispatcher) {
    withContext(parent().ui) { dispatcher.showLoading() }
    val mergeJobList: Array<Job> = Array(this.size) {
        launch(parent().default) { this@mergeWithLoading[it]() }
    }
    mergeJobList.forEach { it.join() }
    withContext(parent().ui) { dispatcher.hideLoading() }
}

/**
 * 对mergeBlock进行并发执行，并挂起当前协程。
 */
suspend fun <T> Array<suspend () -> T>.merge() =
        Array(this.size) {
            launch(parent().default) { this@merge[it]() }
        }.forEach { it.join() }

/**
 * 先显示Loading，然后对mergeBlock进行并发执行，并挂起当前协程。全部执行完毕后，隐藏Loading
 * @param dispatcher Loading分发器
 */
suspend fun <T> List<suspend () -> T>.mergeWithLoading(
        dispatcher: LoadingDispatcher) {
    withContext(parent().ui) { dispatcher.showLoading() }
    val mergeJobList: Array<Job> = Array(this.size) {
        launch(parent().default) { this@mergeWithLoading[it]() }
    }
    mergeJobList.forEach { it.join() }
    withContext(parent().ui) { dispatcher.hideLoading() }
}

/**
 * 对mergeBlock进行并发执行，并挂起当前协程。
 */
suspend fun <T> List<suspend () -> T>.merge() =
        Array(this.size) {
            launch(parent().default) { this@merge[it]() }
        }.forEach { it.join() }

/**
 * 创建suspend block
 */
inline fun <T> suspending(noinline block: suspend () -> T): suspend () -> T = block

/**
 * 先显示Loading，然后执行UseCase，执行完毕后隐藏Loading，返回一个Either
 * @param vm        KoroutineViewModel
 * @param param     UseCase参数
 * @param isUnifyError 是否在onObtainFail中统一处理Error
 */
suspend fun <P, E> IDeferredParamUseCase<P, E>.executeWithLoading(
        vm: MViewModel,
        param: P,
        isUnifyError: Boolean = true): Either<ObtainException, E> {
    withContext(parent().ui) { vm.showLoading() }
    val entity = execute(vm, param, isUnifyError)
    withContext(parent().ui) { vm.hideLoading() }
    return entity
}

/**
 * 执行UseCase，返回一个Either
 * @param dispatcher 错误分发器
 * @param param     UseCase参数
 * @param isUnifyError 是否在onObtainFail中统一处理Error
 */
suspend fun <P, E> IDeferredParamUseCase<P, E>.execute(
        dispatcher: ErrorUnifiedDispatcher,
        param: P,
        isUnifyError: Boolean = true): Either<ObtainException, E> = either(dispatcher, param, isUnifyError).eitherWhenBlock()

/**
 * 先显示Loading，然后执行UseCase，执行完毕后隐藏Loading，返回一个Either
 * @param vm        KoroutineViewModel
 * @param isUnifyError 是否在onObtainFail中统一处理Error
 */
suspend fun <E> IDeferredUseCase<E>.executeWithLoading(
        vm: MViewModel,
        isUnifyError: Boolean = true): Either<ObtainException, E> {
    withContext(parent().ui) { vm.showLoading() }
    val entity = execute(vm, isUnifyError)
    withContext(parent().ui) { vm.hideLoading() }
    return entity
}

/**
 * 执行UseCase，返回一个Either
 * @param dispatcher 错误分发器
 * @param isUnifyError 是否在onObtainFail中统一处理Error
 */
suspend fun <E> IDeferredUseCase<E>.execute(
        dispatcher: ErrorUnifiedDispatcher,
        isUnifyError: Boolean = true): Either<ObtainException, E> = either(dispatcher, isUnifyError).eitherWhenBlock()

/**
 * 通过UseCase创建一个Either
 */
private suspend fun <E, P> IDeferredParamUseCase<P, E>.either(
        dispatcher: ErrorUnifiedDispatcher,
        param: P,
        isUnifyError: Boolean = true): Either<ObtainException, Deferred<E>> =
        try {
            this(parent(), param).right()
        } catch (t: Throwable) {
            (t as? ObtainException ?: t.let {
                it.printStackTrace()
                ObtainException("404", OBTAIN_FAIL_MESSAGE)
            }).also { if (isUnifyError) withContext(parent().ui) { dispatcher.onObtainFail(it) } }.left()
        }

/**
 * 通过UseCase创建一个Either
 */
private suspend fun <E> IDeferredUseCase<E>.either(
        dispatcher: ErrorUnifiedDispatcher,
        isUnifyError: Boolean = true): Either<ObtainException, Deferred<E>> =
        try {
            this(parent()).right()
        } catch (t: Throwable) {
            (t as? ObtainException ?: t.let {
                it.printStackTrace()
                ObtainException("404", OBTAIN_FAIL_MESSAGE)
            }).also { if (isUnifyError) withContext(parent().ui) { dispatcher.onObtainFail(it) } }.left()
        }

private suspend inline fun <T> Either<ObtainException, Deferred<T>>.eitherWhenBlock(): Either<ObtainException, T> =
        when (this) {
            is Right -> b.await().right()
            is Left -> a.left()
        }


