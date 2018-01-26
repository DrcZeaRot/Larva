package com.xcstasy.r.larva.core.framework.cancelable.subscription;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.util.ExceptionHelper;
import io.reactivex.internal.util.OpenHashSet;

/**
 * @author Drc_ZeaRot
 * @lastModified by Drc_ZeaRot on 2018/1/23
 * @since 2017/11/17
 */

public class CompositeSubscription implements CancelableSubscription, SubscriptionContainer {

    private OpenHashSet<Subscription> resources;

    private volatile boolean canceled;

    public CompositeSubscription() {
    }

    /**
     * Creates a CompositeSubscription with the given array of initial elements.
     *
     * @param resources the array of Subscriptions to start with
     */
    public CompositeSubscription(@NonNull Subscription... resources) {
        ObjectHelper.requireNonNull(resources, "resources is null");
        this.resources = new OpenHashSet<>(resources.length + 1);
        for (Subscription resource : resources) {
            ObjectHelper.requireNonNull(resource, "Subscription item is null");
            this.resources.add(resource);
        }

    }

    public CompositeSubscription(@NonNull Iterable<? extends Subscription> resources) {
        ObjectHelper.requireNonNull(resources, "resources is null");
        this.resources = new OpenHashSet<>();
        for (Subscription resource : resources) {
            ObjectHelper.requireNonNull(resource, "Subscription item is null");
            this.resources.add(resource);
        }
    }

    @Override
    public void cancel() {
        if (canceled) {
            return;
        }

        OpenHashSet<Subscription> set;
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
    public boolean add(Subscription s) {
        ObjectHelper.requireNonNull(s, "d is null");
        if (!canceled) {
            synchronized (this) {
                if (!canceled) {
                    OpenHashSet<Subscription> set = resources;
                    if (set == null) {
                        set = new OpenHashSet<>();
                        resources = set;
                    }
                    set.add(s);
                    return true;
                }
            }
        }
        s.cancel();
        return false;
    }

    /**
     * Atomically adds the given array of Disposables to the container or
     * disposes them all if the container has been disposed.
     *
     * @param ds the array of Disposables
     * @return true if the operation was successful, false if the container has been disposed
     */
    public boolean addAll(@NonNull Subscription... ds) {
        ObjectHelper.requireNonNull(ds, "ds is null");
        if (!canceled) {
            synchronized (this) {
                if (!canceled) {
                    OpenHashSet<Subscription> set = resources;
                    if (set == null) {
                        set = new OpenHashSet<>(ds.length + 1);
                        resources = set;
                    }
                    for (Subscription d : ds) {
                        ObjectHelper.requireNonNull(d, "d is null");
                        set.add(d);
                    }
                    return true;
                }
            }
        }
        for (Subscription d : ds) {
            d.cancel();
        }
        return false;
    }

    @Override
    public boolean remove(Subscription s) {
        if (delete(s)) {
            s.cancel();
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(Subscription s) {
        ObjectHelper.requireNonNull(s, "Disposable item is null");
        if (canceled) {
            return false;
        }
        synchronized (this) {
            if (canceled) {
                return false;
            }

            OpenHashSet<Subscription> set = resources;
            if (set == null || !set.remove(s)) {
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
        OpenHashSet<Subscription> set;
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
            OpenHashSet<Subscription> set = resources;
            return set != null ? set.size() : 0;
        }
    }


    /**
     * Cancel the contents of the OpenHashSet by suppressing non-fatal Throwables till the end.
     *
     * @param set the OpenHashSet to cancel elements of
     */
    void cancel(OpenHashSet<Subscription> set) {
        if (set == null) {
            return;
        }
        List<Throwable> errors = null;
        Object[] array = set.keys();

        for (Object o : array) {
            if (o instanceof Subscription) {
                try {
                    ((Subscription) o).cancel();
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
