package com.rs.pressanim;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author rs
 */
class PressNormalAnimator extends PressAnimator {

    @Override
    protected void onTouchHandler(View touchView, MotionEvent event) {
        View targetView = getTargetView();
        if (targetView == null) {
            return;
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initAnimator();
                startDownAnimator();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
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
