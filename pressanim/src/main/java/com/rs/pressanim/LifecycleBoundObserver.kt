package com.rs.pressanim

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * @author rs
 */
class LifecycleBoundObserver(pressAnimator: PressAnimator, lifecycleOwner: LifecycleOwner) :
    LifecycleEventObserver {
    private var mPressAnimator: PressAnimator? = pressAnimator
    private var mLifecycleOwner: LifecycleOwner? = lifecycleOwner

    override fun onStateChanged(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            lifecycleOwner.lifecycle.removeObserver(this)
            mPressAnimator?.cancel()
        }
    }

    fun finish() {
        mLifecycleOwner?.let {
            it.lifecycle.removeObserver(this)
            mLifecycleOwner = null
            mPressAnimator = null
        }
    }
}
