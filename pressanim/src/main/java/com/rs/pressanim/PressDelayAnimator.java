package com.rs.pressanim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PressDelayAnimator implements View.OnTouchListener {
    private static final String TAG = PressDelayAnimator.class.getSimpleName();
    /**
     * 是否等待抬起动画
     */
    private boolean waitUpAnimator;
    private int downDuration = 200;
    private int upDuration = 200;
    private float[] downCurvature = {0.33f, 0f, 0.67f, 1f};
    private float[] upCurvature = {0.33f, 0f, 0.67f, 1f};
    /**
     * 目标view;
     */
    private View targetView;

    private Drawable foregroundDrawable;
    private @DrawableRes
    int resourceId = R.drawable.press_animator;
    private List<View> animatorViews = new ArrayList<>();
    /**
     * 默认缩放率
     */
    private float scaleRatio = 0.95f;

    private AnimatorSet upAnimatorSet;
    private AnimatorSet downAnimatorSet;
    private ObjectAnimator upAnimator;
    private ObjectAnimator downAnimator;
    private PropertyValuesHolder upAnimatorX;
    private PropertyValuesHolder upAnimatorY;
    private PropertyValuesHolder downAnimatorX;
    private PropertyValuesHolder downAnimatorY;
    /**
     * 是否需要前景 默认需要
     */
    private boolean isNeedForeground = true;

    public static PressDelayAnimator get() {
        return new PressDelayAnimator();
    }

    public PressDelayAnimator init() {
        addForeground();
        return this;
    }

    private void addForeground() {
        if (targetView != null && targetView.getForeground() == null && isNeedForeground) {
            targetView.setForeground(getForeGroundDrawable());
            targetView.getForeground().setAlpha(0);
        }
    }

    /**
     * 添加需要动画的view
     *
     * @param animatorView
     * @return
     */
    public PressDelayAnimator addTargetAnimatorView(View animatorView) {
        if (animatorView != null && !animatorViews.contains(animatorView)) {
            if (animatorViews.size() == 0) {
                targetView = animatorView;
            }
            animatorViews.add(animatorView);
        }
        return this;
    }

    /**
     * 添加动画集合
     *
     * @param views 动画集合
     * @return this
     */
    public PressDelayAnimator addTargetAnimatorViews(View... views) {
        if (views == null || views.length == 0) {
            return this;
        }
        if (targetView == null) {
            targetView = views[0];
        }
        for (View view : views) {
            if (!this.animatorViews.contains(view)) {
                animatorViews.add(view);
            }
        }
        return this;
    }

    /**
     * 设置是否需要前景
     *
     * @param isNeedForeground true便是需要
     * @return this
     */
    public PressDelayAnimator setIsNeedForeground(boolean isNeedForeground) {
        this.isNeedForeground = isNeedForeground;
        return this;
    }

    /**
     * 设置可点击的view的监听
     *
     * @param touchView 可点击的view，用来接听监听
     * @return this
     */
    public PressDelayAnimator setOnTouchListener(View touchView) {
        if (touchView != null) {
            touchView.setOnTouchListener(this);
        }
        return this;
    }

    public OnTouchListener getTouchListener() {
        return onTouchListener;
    }

    /**
     * 设置前景drawable
     *
     * @param foregroundDrawable foregroundDrawable
     * @return drawable
     */
    public PressDelayAnimator setForegroundDrawable(Drawable foregroundDrawable) {
        this.foregroundDrawable = foregroundDrawable;
        return this;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        onTouchHandler(v, event);
        return false;
    }

    private boolean isStartedDownAnimate;

    private void onTouchHandler(View touchView, MotionEvent event) {
        if (targetView == null) {
            return;
        }
        if (upAnimatorSet != null && upAnimatorSet.isRunning()) {
            return;
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
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

    private void startDownAnimator() {
        addForeground();
        downAnimatorSet.start();
    }

    private void startUpAnimator() {
        waitUpAnimator = false;
        if (upAnimatorSet != null) {
            upAnimatorSet.start();
        } else if (animatorViews.size() > 0) {
            Log.i(TAG, "startUpAnimator: upAnimatorSet is null");
            initAnimator();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                upAnimatorSet.reverse();
            } else {
                upAnimatorSet.start();
            }
        }
    }

    private void initAnimator() {
        if (upAnimatorSet != null) {
            return;
        }
        upAnimatorSet = new AnimatorSet();
        downAnimatorSet = new AnimatorSet();
        upAnimatorX = PropertyValuesHolder.ofFloat("scaleX", scaleRatio, 1f);
        upAnimatorY = PropertyValuesHolder.ofFloat("scaleY", scaleRatio, 1f);
        downAnimatorX = PropertyValuesHolder.ofFloat("scaleX", 1f, scaleRatio);
        downAnimatorY = PropertyValuesHolder.ofFloat("scaleY", 1f, scaleRatio);
        int[] targetViewLocation = new int[2];
        targetView.getLocationOnScreen(targetViewLocation);
        int targetViewCenterX = targetViewLocation[0] + targetView.getWidth() / 2;
        int targetViewCenterY = targetViewLocation[1] + targetView.getHeight() / 2;
        Collection[] animatorSet = createAnimatorSet(targetViewCenterX, targetViewCenterY);
        downAnimatorSet.setDuration(downDuration);
        downAnimatorSet.setInterpolator(new CubicInterpolator(
                downCurvature[0],
                downCurvature[1],
                downCurvature[2],
                downCurvature[3]));
        downAnimatorSet.playTogether(animatorSet[0]);
        // 抬起动画
        upAnimatorSet.setDuration(upDuration);
        upAnimatorSet.setInterpolator(new CubicInterpolator(
                upCurvature[0],
                upCurvature[1],
                upCurvature[2],
                upCurvature[3]
        ));
        upAnimatorSet.playTogether(animatorSet[1]);
        iniDownAnimator();
        initUpAnimator();
    }

    private void initUpAnimator() {
        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (targetView != null && targetView.getForeground() != null && isNeedForeground) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    int alpha = (int) ((1 - animatedFraction) * 255);
                    targetView.getForeground().setAlpha(alpha);
                }
            }
        });
        upAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isStartedDownAnimate = false;
                setForegoundInVisible();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                isStartedDownAnimate = false;
                setForegoundInVisible();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                Log.i(TAG, "onAnimationRepeat: ");
            }
        });
    }

    private void setForegoundInVisible() {
        if (targetView != null && targetView.getForeground() != null && isNeedForeground) {
            targetView.getForeground().setAlpha(0);
        }
    }

    private void iniDownAnimator() {
        downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (targetView != null && targetView.getForeground() != null && isNeedForeground) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    int alpha = (int) (255 * animatedFraction);
                    targetView.getForeground().setAlpha(alpha);
                }
            }
        });
        downAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (waitUpAnimator) {
                    startUpAnimator();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private Collection[] createAnimatorSet(int targetViewCenterX, int targetViewCenterY) {
        Collection<Animator> downAnimators = new ArrayList<>();
        Collection<Animator> upAnimators = new ArrayList<>();
        for (int i = 0; i < animatorViews.size(); i++) {
            View view = animatorViews.get(i);
            // 第一个view为argetView,不需要设置偏移量
            if (i == 0) {
                upAnimator = ObjectAnimator.ofPropertyValuesHolder(view, upAnimatorX, upAnimatorY);
                downAnimator = ObjectAnimator.ofPropertyValuesHolder(view, downAnimatorX, downAnimatorY);
                upAnimators.add(upAnimator);
                downAnimators.add(downAnimator);
            } else {
                // 计算附属view的带偏移量的集合
                float offsetX = (1 - scaleRatio) * (targetView.getWidth() - view.getWidth()) / 2;
                float offsetY = (1 - scaleRatio) * (targetView.getHeight() - view.getHeight()) / 2;
                int location[] = new int[2];
                view.getLocationOnScreen(location);
                int centerX = location[0] + view.getWidth() / 2;
                int centerY = location[1] + view.getHeight() / 2;
                PropertyValuesHolder downTranslationX = null;
                PropertyValuesHolder upTranslationX = null;
                if (view.getVisibility() == View.GONE || centerX == targetViewCenterX) {
                    downTranslationX = PropertyValuesHolder.ofFloat("translationX", 0, 0);
                    upTranslationX = PropertyValuesHolder.ofFloat("translationX", 0, 0);
                } else if (centerX > targetViewCenterX) {
                    downTranslationX = PropertyValuesHolder.ofFloat("translationX", 0, -offsetX);
                    upTranslationX = PropertyValuesHolder.ofFloat("translationX", -offsetX, 0);
                } else if (centerX < targetViewCenterX) {
                    downTranslationX = PropertyValuesHolder.ofFloat("translationX", 0, offsetX);
                    upTranslationX = PropertyValuesHolder.ofFloat("translationX", offsetX, 0);
                }
                PropertyValuesHolder downTranslationY = null;
                PropertyValuesHolder upTranslationY = null;
                if (view.getVisibility() == View.GONE || centerY == targetViewCenterY) {
                    downTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, 0);
                    upTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, 0);
                } else if (centerY > targetViewCenterY) {
                    downTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, -offsetY);
                    upTranslationY = PropertyValuesHolder.ofFloat("translationY", -offsetY, 0);
                } else if (centerY < targetViewCenterY) {
                    downTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, offsetY);
                    upTranslationY = PropertyValuesHolder.ofFloat("translationY", offsetY, 0);
                }
                ObjectAnimator upAnimator = ObjectAnimator.ofPropertyValuesHolder(view, upTranslationX, upTranslationY, upAnimatorX, upAnimatorY);
                ObjectAnimator downAnimator = ObjectAnimator.ofPropertyValuesHolder(view, downTranslationX, downTranslationY, downAnimatorX, downAnimatorY);
                upAnimators.add(upAnimator);
                downAnimators.add(downAnimator);
            }
        }
        Collection[] upDownAnimators = new Collection[2];
        upDownAnimators[0] = downAnimators;
        upDownAnimators[1] = upAnimators;
        return upDownAnimators;
    }

    /**
     * 获取前景
     *
     * @return
     */
    private Drawable getForeGroundDrawable() {
        if (foregroundDrawable == null) {
            foregroundDrawable = targetView.getContext().getDrawable(resourceId);
        }
        return foregroundDrawable;
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public void onTouch(View view, MotionEvent event) {
            onTouchHandler(view, event);
        }
    };

    private interface OnTouchListener {
        void onTouch(View view, MotionEvent event);
    }
}
