package com.xcstasy.r.larva.core.framework.cleanstructure.domain.usecase

import com.xcstasy.r.larva.core.framework.cleanstructure.domain.IRepository
import io.reactivex.CompletableSource
import io.reactivex.MaybeSource
import io.reactivex.ObservableSource
import io.reactivex.SingleSource

/**
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2017/11/23
 */
/*SingleUseCase*/

interface ISingleUseCase<E> : IResultUseCase<SingleSource<E>>

interface ISingleParamUseCase<in P, E> : IParamResultUseCase<P, SingleSource<E>>

/*MaybeUseCase*/

interface IMaybeUseCase<E> : IResultUseCase<MaybeSource<E>>

interface IMaybeParamUseCase<in P, E> : IParamResultUseCase<P, MaybeSource<E>>

/*CompletableUseCase*/

interface ICompletableUseCase : IResultUseCase<CompletableSource>

interface ICompletableParamUseCase<in P> : IParamResultUseCase<P, CompletableSource>

/*ObservableUseCase*/

interface IObservableUseCase<E> : IResultUseCase<ObservableSource<E>>

interface IObservableParamUseCase<in P, E> : IParamResultUseCase<P, ObservableSource<E>>


//=======================================   AbsUseCase  ==========================================

/*SingleUseCase*/

abstract class AbsSingleUseCase<out R : IRepository, E>(repository: R)
    : AbsUseCase<R>(repository), ISingleUseCase<E>

abstract class AbsSingleParamUseCase<out R : IRepository, in P, E>(repository: R)
    : AbsUseCase<R>(repository), ISingleParamUseCase<P, E>

/*MaybeUseCase*/

abstract class AbsMaybeUseCase<out R : IRepository, E>(repository: R)
    : AbsUseCase<R>(repository), IMaybeUseCase<E>

abstract class AbsMaybeParamUseCase<out R : IRepository, in P, E>(repository: R)
    : AbsUseCase<R>(repository), IMaybeParamUseCase<P, E>

/*CompletableUseCase*/

abstract class AbsCompletableUseCase<out R : IRepository>(repository: R)
    : AbsUseCase<R>(repository), ICompletableUseCase

abstract class AbsCompletableParamUseCase<out R : IRepository, in P>(repository: R)
    : AbsUseCase<R>(repository), ICompletableParamUseCase<P>

/*ObservableUseCase*/

abstract class AbsObservableUseCase<out R : IRepository, E>(repository: R)
    : AbsUseCase<R>(repository), IObservableUseCase<E>

abstract class AbsObservableParamUseCase<out R : IRepository, in P, E>(repository: R)
    : AbsUseCase<R>(repository), IObservableParamUseCase<P, E>