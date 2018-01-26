package com.xcstasy.r.larva.core.framework.lifecycle

/**
 * @author Drc_ZeaRot
 * @since 2018/1/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */

interface LifecycleEvent {
    fun getIndex(): Int
}

enum class ActivityEvent : LifecycleEvent {
    CREATE,
    START,
    RESUME,
    PAUSE,
    STOP,
    DESTROY;

    override fun getIndex(): Int = ordinal
}

enum class FragmentEvent : LifecycleEvent {
    ATTACH,
    CREATE,
    CREATE_VIEW,
    START,
    RESUME,
    PAUSE,
    STOP,
    DESTROY_VIEW,
    DESTROY,
    DETACH;

    override fun getIndex(): Int = ordinal
}

enum class CommonEventMvvm : LifecycleEvent {
    /** 显示Dialog */
    DIALOG_SHOW,
    /** 关闭Dialog */
    DIALOG_DISMISS;

    override fun getIndex(): Int = ordinal
}