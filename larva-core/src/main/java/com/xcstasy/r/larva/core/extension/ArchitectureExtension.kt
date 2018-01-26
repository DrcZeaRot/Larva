@file:Suppress("NOTHING_TO_INLINE")

package com.xcstasy.r.larva.core.extension

import com.xcstasy.r.larva.core.framework.mvvm.MViewModel

/**
 * @author Drc_ZeaRot
 * @since 2017/11/30
 * @lastModified by Drc_ZeaRot on 2017/11/30
 */

inline val MViewModel.dispatcher: MViewModel
    get() = this