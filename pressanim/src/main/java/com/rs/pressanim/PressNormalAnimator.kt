package com.rs.pressanim;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author rs
 */
class PressNormalAnimator extends PressAnimator {
    // 是否跳过当次动画
   private boolean isReturn = false;
    @Override
    protected void onTouchHandler(View touchView, MotionEvent event) {
        View targetView = getTargetView();
        if (targetView == null) {
            return;
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isReturn = false;
                initAnimator();
                if (upAnimatorSet.isRunning() || downAnimatorSet.isRunning()) {
                    // 连续点击太快，前一次的按下动画还没有执行完成，当次整个触摸过程不需要再执行动画
                    isReturn = true;
                    break;
                }
                startDownAnimator();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                if (upAnimatorSet.isRunning()) {
//                    waitUpAnimator = false;
//                    isReturn = false;
//                    break;
//                }
                if (!downAnimatorSet.isRunning()) {
                    if (isReturn) {
                        isReturn = false;
                        waitUpAnimator = false;
                        break;
                    }
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
