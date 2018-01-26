package com.xcstasy.r.larva.core.framework.cancelable.mvvmcancel;

import android.support.annotation.NonNull;

import com.xcstasy.r.larva.core.framework.cancelable.MvvmCancelable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.util.ExceptionHelper;
import io.reactivex.internal.util.OpenHashSet;

/**
 * @author Drc_ZeaRot
 * @lastModified by Drc_ZeaRot on 2018/1/23
 * @since 2017/12/9
 */

public class CompositeCancelable implements CancelableContainer, CancelableCanceler {

    private OpenHashSet<MvvmCancelable> resources;

    private volatile boolean canceled;

    public CompositeCancelable() {
    }

    /**
     * Creates a CompositeCancelable with the given array of initial elements.
     *
     * @param resources the array of MvvmCancelables to start with
     */
    public CompositeCancelable(@NonNull MvvmCancelable... resources) {
        ObjectHelper.requireNonNull(resources, "resources is null");
        this.resources = new OpenHashSet<>(resources.length + 1);
        for (MvvmCancelable resource : resources) {
            ObjectHelper.requireNonNull(resource, "MvvmCancelable item is null");
            this.resources.add(resource);
        }

    }

    public CompositeCancelable(@NonNull Iterable<? extends MvvmCancelable> resources) {
        ObjectHelper.requireNonNull(resources, "resources is null");
        this.resources = new OpenHashSet<>();
        for (MvvmCancelable resource : resources) {
            ObjectHelper.requireNonNull(resource, "MvvmCancelable item is null");
            this.resources.add(resource);
        }
    }

    @Override
    public void cancel() {
        if (canceled) {
            return;
        }

        OpenHashSet<MvvmCancelable> set;
        synchronized (this) {
            if (canceled) {
                return;
            }
            canceled = true;
            set = resources;
            resources = null;
        }

        cancel(set);
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public boolean add(MvvmCancelable c) {
        ObjectHelper.requireNonNull(c, "d is null");
        if (!canceled) {
            synchronized (this) {
                if (!canceled) {
                    OpenHashSet<MvvmCancelable> set = resources;
                    if (set == null) {
                        set = new OpenHashSet<>();
                        resources = set;
                    }
                    set.add(c);
                    return true;
                }
            }
        }
        c.cancel();
        return false;
    }

    /**
     * Atomically adds the given array of Disposables to the container or
     * disposes them all if the container has been disposed.
     *
     * @param ds the array of Disposables
     * @return true if the operation was successful, false if the container has been disposed
     */
    public boolean addAll(@NonNull MvvmCancelable... ds) {
        ObjectHelper.requireNonNull(ds, "ds is null");
        if (!canceled) {
            synchronized (this) {
                if (!canceled) {
                    OpenHashSet<MvvmCancelable> set = resources;
                    if (set == null) {
                        set = new OpenHashSet<>(ds.length + 1);
                        resources = set;
                    }
                    for (MvvmCancelable d : ds) {
                        ObjectHelper.requireNonNull(d, "d is null");
                        set.add(d);
                    }
                    return true;
                }
            }
        }
        for (MvvmCancelable d : ds) {
            d.cancel();
        }
        return false;
    }

    @Override
    public boolean remove(MvvmCancelable c) {
        if (delete(c)) {
            c.cancel();
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(MvvmCancelable c) {
        ObjectHelper.requireNonNull(c, "Disposable item is null");
        if (canceled) {
            return false;
        }
        synchronized (this) {
            if (canceled) {
                return false;
            }

            OpenHashSet<MvvmCancelable> set = resources;
            if (set == null || !set.remove(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Atomically clears the container, then cancels all the previously contained Subscriptions.
     */
    public void clear() {
        if (canceled) {
            return;
        }
        OpenHashSet<MvvmCancelable> set;
        synchronized (this) {
            if (canceled) {
                return;
            }

            set = resources;
            resources = null;
        }

        cancel(set);
    }

    /**
     * Returns the number of currently held Disposables.
     *
     * @return the number of currently held Disposables
     */
    public int size() {
        if (canceled) {
            return 0;
        }
        synchronized (this) {
            if (canceled) {
                return 0;
            }
            OpenHashSet<MvvmCancelable> set = resources;
            return set != null ? set.size() : 0;
        }
    }

    /**
     * Cancel the contents of the OpenHashSet by suppressing non-fatal Throwables till the end.
     *
     * @param set the OpenHashSet to cancel elements of
     */
    void cancel(OpenHashSet<MvvmCancelable> set) {
        if (set == null) {
            return;
        }
        List<Throwable> errors = null;
        Object[] array = set.keys();

        for (Object o : array) {
            if (o instanceof MvvmCancelable) {
                try {
                    ((MvvmCancelable) o).cancel();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    if (errors == null) {
                        errors = new ArrayList<>();
                    }
                    errors.add(ex);
                }
            }
        }
        if (errors != null) {
            if (errors.size() == 1) {
                throw ExceptionHelper.wrapOrThrow(errors.get(0));
            }
            throw new CompositeException(errors);
        }

    }
}
