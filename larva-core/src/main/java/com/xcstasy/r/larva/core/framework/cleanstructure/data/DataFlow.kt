package com.xcstasy.r.larva.core.framework.cleanstructure.data

import com.xcstasy.r.larva.core.framework.mvvm.MView
import io.reactivex.Observable
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Observable data flow.
 * write [value] to emit a value
 * call [observe] to subscribe to this
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2017/11/23
 */

interface IReactDataFlow<T> : IMutableDataFlow<T> {

    /**
     * call this to get a data flow about every element's validation
     */
    fun validator(baseView: MView<*>): Observable<Boolean>

    /**
     * call this to get a data flow copy from main data flow of this
     */
    fun open(): Observable<T>

    interface Creator {
        /**
         * Create an empty [IReactDataFlow]
         * @param verifier verify if the given value is valid , used when you read [latestValue]
         */
        fun <T> empty(messageBlock: (() -> Unit)? = null, verifier: ((T) -> Boolean)? = null): IReactDataFlow<T>

        /**
         * Create a [IReactDataFlow],when subscribed ,emit [default] immediately
         * @param verifier verify if the given value is valid , used when you read [latestValue]
         */
        fun <T> withDefault(default: T, messageBlock: (() -> Unit)? = null, verifier: ((T) -> Boolean)? = null): IReactDataFlow<T>
    }
}

/**
 * Observable data flow.
 * write [value] to emit a value
 * call [observe] to subscribe to this
 * @author Drc_ZeaRot
 * @since 2017/11/23
 * @lastModified by Drc_ZeaRot on 2017/11/23
 */

interface IKoroutineDataFlow<T> : IMutableDataFlow<T> {

    /**
     * call this to get a data flow about every element's validation
     */
    fun validator(baseView: MView<*>): ReceiveChannel<Boolean>

    /**
     * call this to get a data flow copy from main data flow of this
     */
    fun open(): ReceiveChannel<T>

    interface Creator {
        /**
         * Create an empty [IKoroutineDataFlow]
         * @param verifier verify if the given value is valid , used when you read [latestValue]
         */
        fun <T> empty(messageBlock: (() -> Unit)? = null, verifier: ((T) -> Boolean)? = null): IKoroutineDataFlow<T>

        /**
         * Create a [IKoroutineDataFlow],when subscribed ,emit [default] immediately
         * @param verifier verify if the given value is valid , used when you read [latestValue]
         */
        fun <T> withDefault(default: T, messageBlock: (() -> Unit)? = null, verifier: ((T) -> Boolean)? = null): IKoroutineDataFlow<T>
    }
}

interface IMutableDataFlow<T> : IDataFlow<T> {
    /**
     * write value to this,observer get the value,
     * read value from this only get the last value,
     * before read make sure you already set at least one value,otherwise you may get an [NoSuchElementException]
     */
    var value: T
    /**
     * the very latest value this DataFlow holds , mainly used for two way binding , do not write this in viewModel
     */
    var latestValue: T?

}

interface IDataFlow<out T> {
    /**
     * call this to observe the data flow.
     * scheduler、disposer already involved
     * @param baseView need this for disposer and lifecycle
     */
    fun observe(baseView: MView<*>, onChange: (T) -> Unit)
}