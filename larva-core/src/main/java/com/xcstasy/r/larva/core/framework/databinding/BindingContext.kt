package com.xcstasy.r.larva.core.framework.databinding

/**
 * you can only use databinding with this receiver
 */
interface BindingContext {
    fun addBinding(domain: BindingDomain)
    fun destroy()
}

/**
 *
 */
interface BindingContextHolder {
    /**
     *  start your binding using ctx as a receiver
     */
    fun onBinding(ctx: BindingContext)
}

/**
 * Create a new Domain
 */
inline fun <D : BindingDomain> BindingContext.newDomain(domain: D, block: D.() -> Unit) {
    addBinding(domain)
    domain.block()
}

@Suppress("FunctionName")
fun BindingContext(): BindingContext = BindingContextImpl()

class BindingContextImpl : BindingContext {

    private var domains: ArrayList<BindingDomain>? = null

    private fun getDomains(): ArrayList<BindingDomain> =
            domains ?: arrayListOf<BindingDomain>().also { domains = it }

    override fun addBinding(domain: BindingDomain) {
        getDomains().add(domain)
    }

    override fun destroy() {
        domains?.clear()
    }
}
