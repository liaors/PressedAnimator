package com.rs.pressanim;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * @author rs
 */
public class LifecycleBoundObserver implements LifecycleEventObserver {
    private PressAnimator mPressAnimator;
    private LifecycleOwner mLifecycleOwner;

    public LifecycleBoundObserver(PressAnimator pressAnimator, LifecycleOwner lifecycleOwner) {
        if (pressAnimator != null) {
            this.mPressAnimator = pressAnimator;
        }
        mLifecycleOwner = lifecycleOwner;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            lifecycleOwner.getLifecycle().removeObserver(this);
            if (mPressAnimator != null) {
                mPressAnimator.cancel();
            }
        }
    }

    public void finish() {
        if (mLifecycleOwner != null) {
            mLifecycleOwner.getLifecycle().removeObserver(this);
            mLifecycleOwner = null;
            mPressAnimator = null;
        }
    }
}
