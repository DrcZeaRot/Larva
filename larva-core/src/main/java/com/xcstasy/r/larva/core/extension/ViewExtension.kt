@file:Suppress("FunctionName")

package com.xcstasy.r.larva.core.extension

import android.view.View
import android.view.ViewGroup

fun View.removeFromParent() = (this.parent as? ViewGroup)?.removeView(this)

fun View.removeFrom(container: View?) = (container as? ViewGroup)?.removeView(this)

inline var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }