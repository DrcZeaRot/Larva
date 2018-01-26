package com.xcstasy.r.larva.core.react.operator;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.disposables.EmptyDisposable;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.util.AtomicThrowable;
import io.reactivex.plugins.RxJavaPlugins;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Similar as Zip,Use this only when you know the explicit frequency about all sources.
 * <p>this emits the same number of events as the source which emits least events.
 * <p>when zipping, the latest event of all sources joins
 * <p>eg:
 * <p>SourceA: - 1 - - 2 - - - 3
 * <p>SourceB: a - b c - d e f g
 * <p>result:  - 1a - - 2c - - - 3g
 */
public final class ObservableZipLatest<T, R> extends Observable<R> {

    final ObservableSource<? extends T>[] sources;
    final Iterable<? extends ObservableSource<? extends T>> sourcesIterable;
    final Function<? super Object[], ? extends R> zipper;
    final boolean delayError;

    public ObservableZipLatest(ObservableSource<? extends T>[] sources,
                               Iterable<? extends ObservableSource<? extends T>> sourcesIterable,
                               Function<? super Object[], ? extends R> zipper,
                               boolean delayError) {
        this.sources = sources;
        this.sourcesIterable = sourcesIterable;
        this.zipper = zipper;
        this.delayError = delayError;
    }

    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        ObservableSource<? extends T>[] sources = this.sources;
        int count = 0;
        if (sources == null) {
            sources = new Observable[8];
            for (ObservableSource<? extends T> p : sourcesIterable) {
                if (count == sources.length) {
                    ObservableSource<? extends T>[] b = new ObservableSource[count + (count >> 2)];
                    System.arraycopy(sources, 0, b, 0, count);
                    sources = b;
                }
                sources[count++] = p;
            }
        } else {
            count = sources.length;
        }

        if (count == 0) {
            EmptyDisposable.complete(observer);
            return;
        }

        ZipLatestCoordinator<T, R> zc = new ZipLatestCoordinator<T, R>(observer, zipper, count, delayError);
        zc.subscribe(sources);

    }

    static final class ZipLatestCoordinator<T, R> extends AtomicInteger implements Disposable {

        private static final long serialVersionUID = 8567835234654448817L;
        final Observer<? super R> actual;
        final Function<? super Object[], ? extends R> zipper;
        final ZipLatestObserver<T, R>[] observers;
        final T[] latest;
        final AtomicReference<Object> latestRef = new AtomicReference<>();
        final boolean delayError;

        final T[] row;

        volatile boolean cancelled;

        volatile boolean done;

        final AtomicThrowable errors = new AtomicThrowable();

        int active;
        int complete;

        public ZipLatestCoordinator(Observer<? super R> actual,
                                    Function<? super Object[], ? extends R> zipper,
                                    int count, boolean delayError) {
            this.actual = actual;
            this.zipper = zipper;
            this.delayError = delayError;
            this.latest = (T[]) new Object[count];
            this.observers = new ZipLatestObserver[count];
            this.row = (T[]) new Object[count];
        }

        public void subscribe(ObservableSource<? extends T>[] sources) {
            ZipLatestObserver<T, R>[] s = observers;
            int len = s.length;
            for (int i = 0; i < len; i++) {
                s[i] = new ZipLatestObserver<T, R>(this, i);
            }
            // this makes sure the contents of the observers array is visible
            this.lazySet(0);
            actual.onSubscribe(this);
            for (int i = 0; i < len; i++) {
                if (cancelled) {
                    return;
                }
                sources[i].subscribe(s[i]);
            }
        }


        @Override
        public void dispose() {
            if (!cancelled) {
                cancelled = true;
                cancelSources();
                if (getAndIncrement() == 0) {
                    clear(latestRef);
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        void cancel(AtomicReference<?> ref) {
            clear(ref);
            cancelSources();
        }

        void cancelSources() {
            for (ZipLatestObserver<T, R> s : observers) {
                s.dispose();
            }
        }

        void clear(AtomicReference<?> ref) {
            synchronized (this) {
                Arrays.fill(latest, null);
            }
            for (ZipLatestObserver<?, ?> zs : observers) {
                zs.latest.set(null);
            }
            ref.set(null);
        }

        void combine(T value, int index) {
            int a;
            int c;
            int len;
            boolean empty;
            boolean f;
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                len = latest.length;
                T o = latest[index];
                a = active;
                if (o == null) {
                    active = ++a;
                }
                c = complete;
                if (value == null) {
                    complete = ++c;
                } else {
                    latest[index] = value;
                }
                f = a == len;
                // see if either all sources completed
                empty = c == len
                        || (value == null && o == null); // or this source completed without any value
                if (!empty) {
                    if (value != null && f) {
                        latestRef.set(latest.clone());
                    } else if (value == null && errors.get() != null) {
                        done = true; // if this source completed without a value
                    }
                } else {
                    done = true;
                }
            }
            if (!f && value != null) {
                return;
            }
            drain();
        }

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missing = 1;

            final AtomicReference<Object> ref = latestRef;
            final Observer<? super R> a = actual;
            final boolean delayError = this.delayError;
            final ZipLatestObserver<T, R>[] zs = observers;

            final T[] os = row;

            for (; ; ) {

                if (checkCombineTerminated(done, ref.get() == null, a, ref, delayError)) {
                    return;
                }

                for (; ; ) {
                    int i = 0;
                    int emptyCount = 0;

                    for (ZipLatestObserver<T, R> z : zs) {
                        if (os[i] == null) {
                            boolean d = z.done;
                            T v = z.latest.getAndSet(null);
                            boolean empty = v == null;

                            if (checkZipTerminated(d, empty, a, ref, delayError, z)) {
                                return;
                            }
                            if (!empty) {
                                os[i] = v;
                            } else {
                                emptyCount++;
                            }
                        } else {
                            if (z.done && !delayError) {
                                Throwable ex = z.error;
                                if (ex != null) {
                                    cancel(ref);
                                    a.onError(ex);
                                    return;
                                }
                            }
                        }
                        i++;
                    }

                    if (emptyCount != 0) {
                        break;


                    }

                    @SuppressWarnings("unchecked")
                    T[] array = (T[]) ref.getAndSet(null);

                    R v;
                    try {
                        v = ObjectHelper.requireNonNull(zipper.apply(array.clone()), "The zipper returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        cancelled = true;
                        cancel(ref);
                        a.onError(ex);
                        return;
                    }

                    a.onNext(v);

                    Arrays.fill(os, null);
                }

                missing = addAndGet(-missing);
                if (missing == 0) {
                    return;
                }
            }

        }

        boolean checkCombineTerminated(boolean d, boolean empty, Observer<?> a, AtomicReference<?> ref, boolean delayError) {
            if (cancelled) {
                cancel(ref);
                return true;
            }
            if (d) {
                if (delayError) {
                    if (empty) {
                        cancel(ref);
                        Throwable e = errors.terminate();
                        if (e != null) {
                            a.onError(e);
                        } else {
                            a.onComplete();
                        }
                        return true;
                    }
                } else {
                    Throwable e = errors.get();
                    if (e != null) {
                        cancel(ref);
                        a.onError(errors.terminate());
                        return true;
                    } else if (empty) {
                        clear(latestRef);
                        a.onComplete();
                        return true;
                    }
                }
            }
            return false;
        }

        boolean checkZipTerminated(boolean d, boolean empty, Observer<? super R> a, AtomicReference<?> ref, boolean delayError, ZipLatestObserver<?, ?> source) {
            if (cancelled) {
                cancel(ref);
                return true;
            }

            if (d) {
                if (delayError) {
                    if (empty) {
                        Throwable e = source.error;
                        cancel(ref);
                        if (e != null) {
                            a.onError(e);
                        } else {
                            a.onComplete();
                        }
                        return true;
                    }
                } else {
                    Throwable e = source.error;
                    if (e != null) {
                        cancel(ref);
                        a.onError(e);
                        return true;
                    } else if (empty) {
                        cancel(latestRef);
                        a.onComplete();
                        return true;
                    }
                }
            }

            return false;
        }

        void onError(Throwable e) {
            if (!errors.addThrowable(e)) {
                RxJavaPlugins.onError(e);
            }
        }

    }

    static final class ZipLatestObserver<T, R> implements Observer<T> {

        final ZipLatestCoordinator<T, R> parent;

        final int index;

        final AtomicReference<T> latest = new AtomicReference<>();

        final AtomicReference<Disposable> s = new AtomicReference<Disposable>();

        Throwable error;

        boolean done;

        public ZipLatestObserver(ZipLatestCoordinator<T, R> parent, int index) {
            this.parent = parent;
            this.index = index;
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this.s, d);
        }

        @Override
        public void onNext(T t) {
            latest.set(t);
            parent.combine(t, index);
        }

        @Override
        public void onError(Throwable t) {
            error = t;
            done = true;
            parent.onError(t);
            parent.combine(null, index);
        }

        @Override
        public void onComplete() {
            done = true;
            parent.combine(null, index);
        }

        public void dispose() {
            DisposableHelper.dispose(s);
        }
    }
}
