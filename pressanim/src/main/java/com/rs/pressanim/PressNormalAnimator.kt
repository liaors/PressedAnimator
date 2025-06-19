package com.rs.pressanim

import android.view.MotionEvent
import android.view.View

/**
 * @author rs
 */
internal class PressNormalAnimator : PressAnimator() {
    // 是否跳过当次动画
    private var isReturn = false
    override fun onTouchHandler(v: View, event: MotionEvent) {
        super.onTouchHandler(v, event)
        getTargetView() ?: return
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                isReturn = false
                initAnimator()
                if (upAnimatorSet!!.isRunning || downAnimatorSet!!.isRunning) {
                    // 连续点击太快，前一次的按下动画还没有执行完成，当次整个触摸过程不需要再执行动画
                    isReturn = true
                    return
                }
                startDownAnimator()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> //                if (upAnimatorSet.isRunning()) {
//                    waitUpAnimator = false;
//                    isReturn = false;
//                    break;
//                }
                if (!downAnimatorSet!!.isRunning) {
                    if (isReturn) {
                        isReturn = false
                        waitUpAnimator = false
                        return
                    }
                    startUpAnimator()
                } else {
                    waitUpAnimator = true
                }

            else -> {}
        }
    }
}
