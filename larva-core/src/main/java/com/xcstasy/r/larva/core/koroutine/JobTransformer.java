package com.xcstasy.r.larva.core.koroutine;

import kotlinx.coroutines.experimental.Job;

/**
 * @author Drc_ZeaRot
 * @lastModified by Drc_ZeaRot on 2018/1/23
 * @since 2017/11/24
 */

public interface JobTransformer {
    Job transformJob(Job job);
}
