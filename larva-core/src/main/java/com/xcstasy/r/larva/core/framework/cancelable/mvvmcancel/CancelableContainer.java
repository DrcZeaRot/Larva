package com.xcstasy.r.larva.core.framework.cancelable.mvvmcancel;

import com.xcstasy.r.larva.core.framework.cancelable.MvvmCancelable;

/**
 * @author Drc_ZeaRot
 * @lastModified by Drc_ZeaRot on 2018/1/23
 * @since 2017/12/9
 */

public interface CancelableContainer {
    /**
     * Adds a MvvmCancelable to this container or cancels it if the
     * container has been canceled.
     *
     * @param c the MvvmCancelable to add, not null
     * @return true if successful, false if this container has been canceled
     */
    boolean add(MvvmCancelable c);

    /**
     * Removes and cancels the given MvvmCancelable if it is part of this
     * container.
     *
     * @param c the MvvmCancelable to remove and cancel, not null
     * @return true if the operation was successful
     */
    boolean remove(MvvmCancelable c);

    /**
     * Removes (but does not cancel) the given MvvmCancelable if it is part of this
     * container.
     *
     * @param c the MvvmCancelable to remove, not null
     * @return true if the operation was successful
     */
    boolean delete(MvvmCancelable c);
}
