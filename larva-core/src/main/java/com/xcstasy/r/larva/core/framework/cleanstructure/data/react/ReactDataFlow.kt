package com.xcstasy.r.larva.core.framework.cleanstructure.data.react

import com.xcstasy.r.larva.core.framework.cleanstructure.data.IReactDataFlow
import com.xcstasy.r.larva.core.framework.mapping.ReactMappingFactory
import com.xcstasy.r.larva.core.framework.mvvm.MView
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * @author Drc_ZeaRot
 * @since 2017/11/7
 * @lastModified by Drc_ZeaRot on 2017/11/7
 */
class ReactDataFlow<T>
private constructor(private val verifier: ((T) -> Boolean)?,
                    private val messageBlock: (() -> Unit)?) : IReactDataFlow<T> {

    private val behaviorSubject: BehaviorSubject<T> = BehaviorSubject.create()

    override var value: T
        set(value) {
            latestValue = value
            behaviorSubject.onNext(value)
        }
        get() = latestValue ?: throw NoSuchElementException()

    override var latestValue: T? = null
        set(value) {
            value?.let { field = it }
        }
        get() = latestValue?.let { value ->
            verifier?.invoke(value)?.let { valid -> if (valid) value else messageBlock?.invoke()?.let { null } }
        }

    override fun validator(baseView: MView<*>): Observable<Boolean> =
            (behaviorSubject.hide().map { verifier?.invoke(it) ?: true }).distinctUntilChanged()


    override fun open(): Observable<T> = behaviorSubject.hide()

    override fun observe(baseView: MView<*>, onChange: (T) -> Unit) {
        behaviorSubject
                .compose(ReactMappingFactory.disposer(baseView))
                .compose(ReactMappingFactory.scheduler())
                .subscribe(onChange)
    }

    companion object : IReactDataFlow.Creator {
        override fun <T> empty(messageBlock: (() -> Unit)?, verifier: ((T) -> Boolean)?) = ReactDataFlow(verifier, messageBlock)

        override fun <T> withDefault(default: T, messageBlock: (() -> Unit)?, verifier: ((T) -> Boolean)?) = ReactDataFlow(verifier, messageBlock).apply { value = default }
    }
}