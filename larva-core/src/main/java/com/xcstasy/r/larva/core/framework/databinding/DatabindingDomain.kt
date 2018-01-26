package com.xcstasy.r.larva.core.framework.databinding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.xcstasy.r.larva.core.koroutine.extension.combineLatest
import com.xcstasy.r.larva.core.extension.visible
import com.xcstasy.r.larva.core.framework.cleanstructure.data.IDataFlow
import com.xcstasy.r.larva.core.framework.cleanstructure.data.IKoroutineDataFlow
import com.xcstasy.r.larva.core.framework.cleanstructure.data.IMutableDataFlow
import com.xcstasy.r.larva.core.framework.cleanstructure.data.IReactDataFlow
import com.xcstasy.r.larva.core.framework.databinding.viewbinding.koroutinebinding.TextViewAfterTextChangeHandler
import com.xcstasy.r.larva.core.framework.mvvm.MView
import com.xcstasy.r.larva.core.koroutine.extension.default
import io.reactivex.Observable
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

/**
 * Databinding领域
 * @author Drc_ZeaRot
 * @since 2017/12/12
 * @lastModified by Drc_ZeaRot on 2017/12/12
 */

interface BindingDomain {
    val baseView: MView<*>
}

@Suppress("NOTHING_TO_INLINE")
open class DefaultDomain(override val baseView: MView<*>) : BindingDomain {

    inline fun <T, E> T.bind(dataFlow: IDataFlow<E>, crossinline onChange: T.(E) -> Unit) {
        dataFlow.observe(baseView) { this.onChange(it) }
    }

    inline fun <T> T.bindString(dataFlow: IDataFlow<String>, crossinline onChange: T.(String) -> Unit) {
        bind(dataFlow, onChange)
    }

    inline fun <T> T.bindBoolean(dataFlow: IDataFlow<Boolean>, crossinline onChange: T.(Boolean) -> Unit) {
        bind(dataFlow, onChange)
    }

    inline fun <T> T.bindInt(dataFlow: IDataFlow<Int>, crossinline onChange: T.(Int) -> Unit) {
        bind(dataFlow, onChange)
    }

    inline fun View.bindVisible(dataFlow: IDataFlow<Boolean>) {
        bindBoolean(dataFlow) { visible = it }
    }

    fun View.bindEnable(vararg dataFlow: IReactDataFlow<Any>) {
        dataFlow.map { it.validator(baseView) }
                .let {
                    Observable.combineLatest(it) { array -> array.all { it == true } }
                }.subscribe({ this@bindEnable.isEnabled = it }, {})
    }

    fun View.bindEnable(vararg dataFlow: IKoroutineDataFlow<Any>) {
        val channel = dataFlow
                .map { it.validator(baseView) }
                .combineLatest { array -> array.all { it == true } }
        launch(baseView.mainJob.default) {
            channel.consumeEach {
                if (isActive) {
                    this@bindEnable.isEnabled = it
                }
            }
        }.invokeOnCompletion { channel.cancel() }
    }

    inline fun View.bindBg(dataFlow: IDataFlow<Int>) {
        bindInt(dataFlow) { setBackgroundResource(it) }
    }

    inline fun TextView.bindText(dataFlow: IDataFlow<String>) {
        bindString(dataFlow) { text = it }
    }

    fun TextView.bindTextTwoWay(dataFlow: IDataFlow<String>) {
        (dataFlow as? IMutableDataFlow<String>)?.also { flow ->
            val handler = TextViewAfterTextChangeHandler(this)
            launch(baseView.mainJob.default) {
                handler.receiveChannel
                        .consumeEach { flow.latestValue = it.first.toString().trim() }
            }.invokeOnCompletion(handler = handler)
        } ?: throw IllegalArgumentException("Only IMutableDataFlow can be binded two way")
        dataFlow.observe(baseView) { text = it }
    }

    inline fun TextView.bindHint(dataFlow: IDataFlow<String>) {
        bindString(dataFlow) { hint = it }
    }

    inline fun TextView.bindColorRes(dataFlow: IDataFlow<Int>) {
        bindInt(dataFlow) { setTextColor(it) }
    }

    inline fun ImageView.bindImgRes(dataFlow: IDataFlow<Int>) {
        bindInt(dataFlow) { setImageResource(it) }
    }

    inline fun <T> IDataFlow<T>.observe(noinline onChange: (T) -> Unit) {
        observe(baseView, onChange)
    }
}

inline fun BindingContext.defaultDomain(view: MView<*>, block: DefaultDomain.() -> Unit) {
    newDomain(DefaultDomain(view), block)
}