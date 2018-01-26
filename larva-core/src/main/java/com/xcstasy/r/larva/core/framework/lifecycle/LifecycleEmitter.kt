package com.xcstasy.r.larva.core.framework.lifecycle

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.Channel

/**
 * @author Drc_ZeaRot
 * @since 2018/1/23
 * @lastModified by Drc_ZeaRot on 2018/1/23
 */
interface LifecycleEmitter<in E : LifecycleEvent, out L, out R> {
    fun emit(event: E)
    val left: L
    val right: R
    fun destroy()
}

interface KoroutineReactEmitter : LifecycleEmitter<LifecycleEvent, BroadcastChannel<LifecycleEvent>, Observable<LifecycleEvent>>

@Suppress("FunctionName")
fun KoroutineReactEmitter(): KoroutineReactEmitter = KoroutineReactEmitterImpl()

class KoroutineReactEmitterImpl : KoroutineReactEmitter {

    private val broadcast: BroadcastChannel<LifecycleEvent> = BroadcastChannel(Channel.CONFLATED)
    private val subject: BehaviorSubject<LifecycleEvent> = BehaviorSubject.create()

    override fun emit(event: LifecycleEvent) {
        broadcast.offer(event)
        subject.onNext(event)
    }

    override val left: BroadcastChannel<LifecycleEvent> = broadcast
    override val right: Observable<LifecycleEvent> = subject.hide()

    override fun destroy() {
        broadcast.close()
    }
}