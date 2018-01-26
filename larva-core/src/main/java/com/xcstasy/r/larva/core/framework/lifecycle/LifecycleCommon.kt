package com.xcstasy.r.larva.core.framework.lifecycle

/**
 * @author Drc_ZeaRot
 * @since 2018/1/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
enum class ProviderType {
    KOROUTINE,
    REACTIVE
}

interface LifecycleProvider<in E : LifecycleEvent> {

    /**
     * 绑定到Lifecycle
     */
    fun <T> bindToLifecycle(providerType: ProviderType = ProviderType.REACTIVE): LifecycleTransformer<T>

    /**
     * 绑定到指定 公共事件
     * @param event 公共事件
     */
    fun <T> bindUntilEvent(event: E, providerType: ProviderType = ProviderType.REACTIVE): LifecycleTransformer<T>
}

class OutsideLifecycleException(detailMessage: String) : IllegalStateException(detailMessage)

val ACTIVITY_LIFECYCLE_MVVM: (LifecycleEvent) -> LifecycleEvent = {
    when (it) {
        ActivityEvent.CREATE, ActivityEvent.STOP -> ActivityEvent.DESTROY
        ActivityEvent.START, ActivityEvent.PAUSE -> ActivityEvent.STOP
        ActivityEvent.RESUME -> ActivityEvent.PAUSE
        CommonEventMvvm.DIALOG_SHOW -> CommonEventMvvm.DIALOG_DISMISS
        ActivityEvent.DESTROY -> throw OutsideLifecycleException("Cannot bind to Fragment lifecycle when outside of it.")
        else -> throw UnsupportedOperationException("Binding to $it not yet implemented")
    }
}
val FRAGMENT_LIFECYCLE_MVVM: (LifecycleEvent) -> LifecycleEvent = {
    when (it) {
        FragmentEvent.ATTACH, FragmentEvent.DESTROY -> FragmentEvent.DETACH
        FragmentEvent.CREATE, FragmentEvent.DESTROY_VIEW -> FragmentEvent.DESTROY
        FragmentEvent.CREATE_VIEW, FragmentEvent.STOP -> FragmentEvent.DESTROY_VIEW
        FragmentEvent.START, FragmentEvent.PAUSE -> FragmentEvent.STOP
        FragmentEvent.RESUME -> FragmentEvent.PAUSE
        CommonEventMvvm.DIALOG_SHOW -> CommonEventMvvm.DIALOG_DISMISS
        FragmentEvent.DETACH -> throw OutsideLifecycleException("Cannot bind to Fragment lifecycle when outside of it.")
        else -> throw UnsupportedOperationException("Binding to $it not yet implemented")
    }
}