package com.xcstasy.r.larva.core.framework.cleanstructure.domain.usecase

import com.xcstasy.r.larva.core.framework.cleanstructure.domain.IRepository
import kotlinx.coroutines.experimental.Deferred
import kotlin.coroutines.experimental.CoroutineContext

/**
 * @author Drc_ZeaRot
 * @since 2017/11/24
 * @lastModified by Drc_ZeaRot on 2017/11/24
 */

interface IDeferredResultUseCase<out R> : IUseCase, (CoroutineContext) -> R

interface IDeferredParamResultUseCase<in P, out R> : IUseCase, (CoroutineContext, P) -> R

/*DeferredUseCase*/

interface IDeferredUseCase<out E> : IDeferredResultUseCase<Deferred<E>>

interface IDeferredParamUseCase<in P, out E> : IDeferredParamResultUseCase<P, Deferred<E>>


/*DeferredUseCase*/

abstract class AbsDeferredUseCase<out R : IRepository, out E>(repository: R)
    : AbsUseCase<R>(repository), IDeferredUseCase<E>

abstract class AbsDeferredParamUseCase<out R : IRepository, in P, out E>(repository: R)
    : AbsUseCase<R>(repository), IDeferredParamUseCase<P, E>