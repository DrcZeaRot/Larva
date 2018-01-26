package com.xcstasy.r.larva.core.framework.cleanstructure.data

import com.xcstasy.r.larva.core.framework.cleanstructure.domain.IRepository

/**
 * @author Drc_ZeaRot
 * @since 2017/11/9
 * @lastModified by Drc_ZeaRot on 2017/11/9
 */
abstract class BaseRepository<out S : IServiceProvider<*>>(val serviceProvider: S) : IRepository