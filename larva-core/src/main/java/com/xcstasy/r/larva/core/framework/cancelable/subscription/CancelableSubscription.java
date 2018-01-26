package com.xcstasy.r.larva.core.framework.cancelable.subscription;

/**
 * @author Drc_ZeaRot
 * @lastModified by Drc_ZeaRot on 2018/1/23
 * @since 2017/11/17
 */

public interface CancelableSubscription {
    /**
     * Cancel the resource, the operation should be idempotent.
     */
    void cancel();
    /**
     * Returns true if this resource has been canceled.
     * @return true if this resource has been canceled
     */
    boolean isCanceled();


}
