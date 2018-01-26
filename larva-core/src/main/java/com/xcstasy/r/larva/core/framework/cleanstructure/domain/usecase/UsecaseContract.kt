package com.xcstasy.r.larva.core.framework.cleanstructure.domain.usecase

import com.xcstasy.r.larva.core.framework.cleanstructure.domain.IRepository
import com.xcstasy.r.larva.core.framework.mvvm.MViewModel

/**
 * @author Drc_ZeaRot
 * @since 2018/1/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */

interface IUseCase {
    fun destroy() = Unit
}

interface IResultUseCase<out R> : IUseCase, (MViewModel) -> R

interface IParamResultUseCase<in P, out R> : IUseCase, (MViewModel, P) -> R

abstract class AbsUseCase<out R : IRepository>(val repository: R) : IUseCase