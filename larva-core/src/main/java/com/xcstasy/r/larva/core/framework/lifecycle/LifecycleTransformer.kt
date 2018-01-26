package com.xcstasy.r.larva.core.framework.lifecycle

import com.xcstasy.r.larva.core.koroutine.DeferredTransformer
import com.xcstasy.r.larva.core.koroutine.JobTransformer
import io.reactivex.*

/**
 * @author Drc_ZeaRot
 * @since 2018/1/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
interface LifecycleTransformer<T> :
        DeferredTransformer<T, T>,
        JobTransformer,
        ObservableTransformer<T, T>,
        SingleTransformer<T, T>,
        MaybeTransformer<T, T>,
        FlowableTransformer<T, T>,
        CompletableTransformer