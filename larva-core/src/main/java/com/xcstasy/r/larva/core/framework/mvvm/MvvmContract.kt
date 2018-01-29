package com.xcstasy.r.larva.core.framework.mvvm

import com.xcstasy.r.larva.core.framework.cancelable.MvvmDisposer
import com.xcstasy.r.larva.core.framework.databinding.BindingContextHolder
import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleEvent
import com.xcstasy.r.larva.core.framework.lifecycle.LifecycleProvider

/**
* @author Drc_ZeaRot
* @since 2018/1/23
* @lastModified by Drc_ZeaRot on 2018/1/23
*/

typealias MView<VM> = MvvmContract.View<VM>

typealias MViewModel = MvvmContract.ViewModel

interface MvvmContract {

    interface View<out VM : ViewModel> : LifecycleProvider<LifecycleEvent>, MvvmDisposer, JobHolder, BindingContextHolder {

        val viewModel: VM?

        fun showLoading()

        fun hideLoading()

        fun showToast(text: Any?)

        fun showToast(resId: Int)
    }

    /**
     * ViewModel that provides observable data
     */
    interface ViewModel : ErrorUnifiedDispatcher, LoadingDispatcher {

        val baseView: View<*>

        fun onViewCreated()

        fun onDestroyView()
    }

}