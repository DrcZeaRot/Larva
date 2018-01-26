package com.xcstasy.r.larva.core.framework.cancelable.subscription;

import org.reactivestreams.Subscription;

/**
 * @author Drc_ZeaRot
 * @lastModified by Drc_ZeaRot on 2018/1/23
 * @since 2017/11/17
 */

public interface SubscriptionContainer {

    /**
     * Adds a subscription to this container or cancels it if the
     * container has been canceled.
     *
     * @param s the subscription to add, not null
     * @return true if successful, false if this container has been canceled
     */
    boolean add(Subscription s);

    /**
     * Removes and cancels the given subscription if it is part of this
     * container.
     *
     * @param s the subscription to remove and cancel, not null
     * @return true if the operation was successful
     */
    boolean remove(Subscription s);

    /**
     * Removes (but does not cancel) the given subscription if it is part of this
     * container.
     *
     * @param s the subscription to remove, not null
     * @return true if the operation was successful
     */
    boolean delete(Subscription s);
}
