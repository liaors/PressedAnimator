package com.rs.pressanim;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author rs
 * @description 延迟按压动画，（点击才有动画，滑动没有动画）
 */
class PressDelayAnimator extends  PressAnimator {
    @Override
    protected void onTouchHandler(View touchView, MotionEvent event) {
        View targetView = getTargetView();
        if (targetView == null) {
            return;
        }
        if (upAnimatorSet != null && upAnimatorSet.isRunning()) {
            return;
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if ((downAnimatorSet == null || !isStartedDownAnimate) && touchView.isPressed()) {
                    if ((downAnimatorSet != null && downAnimatorSet.isRunning()) || upAnimatorSet != null && upAnimatorSet.isRunning()) {
                        break;
                    }
                    isStartedDownAnimate = true;
                    initAnimator();
                    startDownAnimator();
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (downAnimatorSet == null || upAnimatorSet == null) {
                    break;
                }
                if (!isStartedDownAnimate) {
                    break;
                }
                if (upAnimatorSet.isRunning()) {
                    break;
                }
                if (!downAnimatorSet.isRunning()) {
                    startUpAnimator();
                } else {
                    waitUpAnimator = true;
                }
                break;
            default:
                break;
        }
    }

}
