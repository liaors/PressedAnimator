package com.rs.pressanim

import android.view.MotionEvent
import android.view.View

/**
 * @author rs
 * @description 延迟按压动画，（点击才有动画，滑动没有动画）
 */
internal class PressDelayAnimator : PressAnimator() {
    override fun onTouchHandler(v: View, event: MotionEvent) {
        super.onTouchHandler(v, event)
        getTargetView() ?: return
        if (upAnimatorSet != null && upAnimatorSet!!.isRunning) {
            return
        }
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val runnable = Runnable {
                    actionDown(v)
                }
                getTargetView()!!.postDelayed(runnable, 150)
            }

            MotionEvent.ACTION_MOVE -> actionDown(v)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (downAnimatorSet == null || upAnimatorSet == null) {
                    return
                }
                if (!isStartedDownAnimate) {
                    return
                }
                if (upAnimatorSet!!.isRunning) {
                    return
                }
                if (!downAnimatorSet!!.isRunning) {
                    startUpAnimator()
                } else {
                    waitUpAnimator = true
                }
            }

            else -> {}
        }
    }

    private fun actionDown(touchView: View) {
        if ((downAnimatorSet == null || !isStartedDownAnimate) && touchView.isPressed) {
            if (!isAnimRunning) {
                isStartedDownAnimate = true
                initAnimator()
                startDownAnimator()
            }
        }
    }

    private val isAnimRunning: Boolean
        get() = (downAnimatorSet != null && downAnimatorSet!!.isRunning) || upAnimatorSet != null && upAnimatorSet!!.isRunning
}
