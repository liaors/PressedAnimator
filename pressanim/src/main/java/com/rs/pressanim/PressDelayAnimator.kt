package com.rs.pressanim;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author rs
 * @description 延迟按压动画，（点击才有动画，滑动没有动画）
 */
class PressDelayAnimator extends PressAnimator {

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
            case MotionEvent.ACTION_DOWN:
                Runnable runnable = () -> {
                    actionDown(touchView);
                };
                getTargetView().postDelayed(runnable,150);
                break;
            case MotionEvent.ACTION_MOVE:
                actionDown(touchView);
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

    private void actionDown(View touchView) {
        if ((downAnimatorSet == null || !isStartedDownAnimate) && touchView.isPressed()) {
            if(!isAnimRunning()){
                isStartedDownAnimate = true;
                initAnimator();
                startDownAnimator();
            }
        }
    }

    private boolean isAnimRunning(){
        return (downAnimatorSet != null && downAnimatorSet.isRunning()) || upAnimatorSet != null && upAnimatorSet.isRunning();
    }

}
