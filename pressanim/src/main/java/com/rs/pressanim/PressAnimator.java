package com.rs.pressanim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author rs
 */
public abstract class PressAnimator {
    private static final int DEFAULT_RADIUS = R.dimen.press_radius;
    // 按压动画颜色
    private static final String DEFAULT_COLOR = "#0F000000";
    /**
     * 是否等待抬起动画
     */
    protected boolean waitUpAnimator;
    private final static int DOWN_DURATION = 200;
    private final static int UP_DURATION = 200;
    private final float[] downCurvature = {0.33f, 0f, 0.67f, 1f};
    private final float[] upCurvature = {0.33f, 0f, 0.67f, 1f};
    /**
     * 目标view;
     */
    private View targetView;

    private Drawable foregroundDrawable;
    private @DrawableRes int maskDrawable;
    private int colorRes;
    private int leftTopCornerRadius = DEFAULT_RADIUS;
    private int rightTopCornerRadius = DEFAULT_RADIUS;
    private int leftBottomCornerRadius = DEFAULT_RADIUS;
    private int rightBottomCornerRadius = DEFAULT_RADIUS;

    private final List<View> animatorViews = new ArrayList<>();
    /**
     * 默认缩放率
     */
    private float scaleRatio = 0.95f;

    protected AnimatorSet upAnimatorSet;
    protected AnimatorSet downAnimatorSet;
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
    /**
     * 是否是圆形背景
     */
    private boolean circular;
    protected boolean isStartedDownAnimate;

    protected abstract void onTouchHandler(View v, MotionEvent event);


    /**
     * 初始化
     *
     * @return this
     */
    public PressAnimator init() {
        View targetView = getTargetView();
        if (targetView != null && targetView.getWidth() > 0) {
            addForeground();
        }
        return this;
    }

    /**
     * 缩放比例
     *
     * @param scaleRatio 0.0f -1.0f 1.0f表示不缩放
     * @return this
     */
    public PressAnimator setScaleRatio(@FloatRange(from = 0.0f, to = 1.0f) float scaleRatio) {
        this.scaleRatio = scaleRatio;
        return this;
    }

    /**
     * @param maskDrawable 遮罩资源
     * @return this
     */
    public PressAnimator setMaskDrawable(@DrawableRes int maskDrawable) {
        this.maskDrawable = maskDrawable;
        return this;
    }

    /**
     * 添加需要动画的view
     *
     * @param animatorView 需要动画的view
     * @return this
     */
    public PressAnimator addTargetAnimatorView(View animatorView) {
       return addTargetAnimatorView(animatorView,false);
    }

    /**
     * 添加需要动画的view
     *
     * @param animatorView 需要动画的view
     * @param isMaxWh      这一组动画view中，这个view是否是最大宽高
     * @return this
     */
    public PressAnimator addTargetAnimatorView(View animatorView, boolean isMaxWh) {
        if (animatorView != null && !animatorViews.contains(animatorView)) {
            if (isMaxWh) {
                targetView = animatorView;
                animatorViews.add(0, animatorView);
            } else {
                animatorViews.add(animatorView);
            }
        }
        return this;
    }

    /**
     * 添加动画集合
     *
     * @param views 动画集合
     * @return this
     */
    public PressAnimator addTargetAnimatorViews(View... views) {
        return addTargetAnimatorViews(false,views);
    }

    /**
     * 添加动画集合
     *
     * @param views              动画集合
     * @param firstViewMaxWh 如果为true，需要此view宽高最大
     * @return this
     */
    public PressAnimator addTargetAnimatorViews(boolean firstViewMaxWh, View... views) {
        if (views == null || views.length == 0) {
            return this;
        }
        if (firstViewMaxWh) {
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
    public PressAnimator setNeedForeground(boolean isNeedForeground) {
        this.isNeedForeground = isNeedForeground;
        return this;
    }

    /**
     * 设置可点击的view的监听
     *
     * @param touchView 可点击的view，用来接听监听
     * @return this
     */
    public PressAnimator setOnTouchListener(View touchView) {
        if (touchView != null) {
            touchView.setOnTouchListener(this::onTouch);
        }
        return this;
    }

    /**
     * 获取onTouch事件回调
     *
     * @return OnTouchListener
     */
    public OnTouchListener getOnTouchListener() {
        return onTouchListener;
    }

    /**
     * 设置前景drawable
     *
     * @param foregroundDrawable foregroundDrawable
     * @return drawable
     */
    public PressAnimator setForegroundDrawable(Drawable foregroundDrawable) {
        this.foregroundDrawable = foregroundDrawable;
        return this;
    }

    /**
     * 是否是圆形背景
     *
     * @param circular true表示是圆形背景
     * @return PressAnimator的具体实现类
     */
    public PressAnimator setCircular(boolean circular) {
        this.circular = circular;
        return this;
    }

    /**
     * 设置背景颜色值
     *
     * @param colorId colorId
     * @return this
     */
    public PressAnimator setColor(@ColorRes int colorId) {
        this.colorRes = colorId;
        return this;
    }

    /**
     * 设置按压背景圆角大小
     *
     * @param cornerRadius 圆角大小
     * @return this
     */
    public PressAnimator setCornerRadius(@DimenRes int cornerRadius) {
        this.leftTopCornerRadius = cornerRadius;
        this.rightTopCornerRadius = cornerRadius;
        this.leftBottomCornerRadius = cornerRadius;
        this.rightBottomCornerRadius = cornerRadius;
        return this;
    }

    /**
     * 设置按压背景圆角大小
     *
     * @param topCornerRadius    上测左右圆角大小
     * @param bottomCornerRadius 下测左右圆角大小
     * @return this
     */
    public PressAnimator setCornerRadius(@DimenRes int topCornerRadius, @DimenRes int bottomCornerRadius) {
        this.leftTopCornerRadius = topCornerRadius;
        this.rightTopCornerRadius = topCornerRadius;
        this.leftBottomCornerRadius = bottomCornerRadius;
        this.rightBottomCornerRadius = bottomCornerRadius;
        return this;
    }

    /**
     * 设置按压背景圆角大小
     *
     * @param leftTopCornerRadius     左上角圆角大小
     * @param rightTopCornerRadius    右上角圆角大小
     * @param leftBottomCornerRadius  左下角圆角大小
     * @param rightBottomCornerRadius 右下角圆角大小
     * @return PressAnimator
     */
    public PressAnimator setCornerRadius(@DimenRes int leftTopCornerRadius, @DimenRes int rightTopCornerRadius, @DimenRes int leftBottomCornerRadius, @DimenRes int rightBottomCornerRadius) {
        this.leftTopCornerRadius = leftTopCornerRadius;
        this.rightTopCornerRadius = rightTopCornerRadius;
        this.leftBottomCornerRadius = leftBottomCornerRadius;
        this.rightBottomCornerRadius = rightBottomCornerRadius;
        return this;
    }

    protected void startDownAnimator() {
        addForeground();
        downAnimatorSet.start();
    }

    protected void startUpAnimator() {
        waitUpAnimator = false;
        if (upAnimatorSet != null) {
            upAnimatorSet.start();
        } else if (!animatorViews.isEmpty()) {
            Log.i(getClass().getSimpleName(), "startUpAnimator: upAnimatorSet is null");
            initAnimator();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                upAnimatorSet.reverse();
            } else {
                upAnimatorSet.start();
            }
        }
    }

    /**
     * 将最大的view作为targetView
     *
     * @return View
     */
    protected View getTargetView() {
        if (targetView == null) {
            Collections.sort(animatorViews, (o1, o2) -> {
                int o1Result = o1.getWidth() + o1.getHeight();
                int o2Result = o2.getWidth() + o2.getHeight();
                return o2Result - o1Result;
            });
            View firstView = animatorViews.get(0);
            if (firstView != null && firstView.getWidth() > 0) {
                targetView = animatorViews.get(0);
            }
        }
        return targetView;
    }

    protected void initAnimator() {
        if (targetView == null) {
            return;
        }
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
        downAnimatorSet.setDuration(DOWN_DURATION);
        downAnimatorSet.setInterpolator(new CubicInterpolator(
                downCurvature[0],
                downCurvature[1],
                downCurvature[2],
                downCurvature[3]));
        downAnimatorSet.playTogether(animatorSet[0]);
        // 抬起动画
        upAnimatorSet.setDuration(UP_DURATION);
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
        upAnimator.addUpdateListener(valueAnimator -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                setForegroundInVisible();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                isStartedDownAnimate = false;
                setForegroundInVisible();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                Log.i(getClass().getSimpleName(), "onAnimationRepeat: ");
            }
        });
    }


    private void setForegroundInVisible() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (targetView.getForeground() != null && isNeedForeground) {
                targetView.getForeground().setAlpha(0);
            }
        }
    }

    private void iniDownAnimator() {
        downAnimator.addUpdateListener(valueAnimator -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                waitUpAnimator = false;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private Collection[] createAnimatorSet(int targetViewCenterX, int targetViewCenterY) {
        List<Animator> downAnimators = new ArrayList<>(animatorViews.size());
        List<Animator> upAnimators = new ArrayList<>(animatorViews.size());
        for (int i = 0; i < animatorViews.size(); i++) {
            View view = animatorViews.get(i);
            // 第一个view为targetView,不需要设置偏移量
            if (view == targetView) {
                upAnimator = ObjectAnimator.ofPropertyValuesHolder(view, upAnimatorX, upAnimatorY);
                downAnimator = ObjectAnimator.ofPropertyValuesHolder(view, downAnimatorX, downAnimatorY);
                upAnimators.add(upAnimator);
                downAnimators.add(downAnimator);
            } else {
                // 计算附属view的带偏移量的集合
                float offsetX = (1 - scaleRatio) * (targetView.getWidth() - view.getWidth()) / 2;
                float offsetY = (1 - scaleRatio) * (targetView.getHeight() - view.getHeight()) / 2;
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                int centerX = location[0] + view.getWidth() / 2;
                int centerY = location[1] + view.getHeight() / 2;
                PropertyValuesHolder downTranslationX;
                PropertyValuesHolder upTranslationX;
                if (view.getVisibility() == View.GONE || centerX == targetViewCenterX) {
                    downTranslationX = PropertyValuesHolder.ofFloat("translationX", 0, 0);
                    upTranslationX = PropertyValuesHolder.ofFloat("translationX", 0, 0);
                } else if (centerX > targetViewCenterX) {
                    downTranslationX = PropertyValuesHolder.ofFloat("translationX", 0, -offsetX);
                    upTranslationX = PropertyValuesHolder.ofFloat("translationX", -offsetX, 0);
                } else {
                    downTranslationX = PropertyValuesHolder.ofFloat("translationX", 0, offsetX);
                    upTranslationX = PropertyValuesHolder.ofFloat("translationX", offsetX, 0);
                }
                PropertyValuesHolder downTranslationY;
                PropertyValuesHolder upTranslationY;
                if (view.getVisibility() == View.GONE || centerY == targetViewCenterY) {
                    downTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, 0);
                    upTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, 0);
                } else if (centerY > targetViewCenterY) {
                    downTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, -offsetY);
                    upTranslationY = PropertyValuesHolder.ofFloat("translationY", -offsetY, 0);
                } else {
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

    private void addForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (targetView != null && targetView.getForeground() == null && isNeedForeground) {
                Drawable foregroundDrawable = getForegroundDrawable();
                targetView.setForeground(foregroundDrawable);
                foregroundDrawable.setAlpha(0);
            }
        }
    }

    /**
     * 获取前景
     *
     * @return Drawable
     */
    private Drawable getForegroundDrawable() {
        if (foregroundDrawable != null) {
            return foregroundDrawable;
        }
        if (maskDrawable != 0) {
            foregroundDrawable = ContextCompat.getDrawable(targetView.getContext(), maskDrawable);
        } else {
            int colorInt = colorRes == 0 ? Color.parseColor(DEFAULT_COLOR) : ContextCompat.getColor(targetView.getContext(), colorRes);
            foregroundDrawable = getBackgroundDrawable(targetView.getContext(), colorInt);
        }
        return foregroundDrawable;
    }

    private Drawable getBackgroundDrawable(@NonNull Context context, @ColorInt int colorInt) {
        ShapeDrawable shapeDrawable;
        if (circular) {
            shapeDrawable = new ShapeDrawable(new OvalShape());
        } else {
            float leftTopRadius = context.getResources().getDimension(leftTopCornerRadius);
            float rightTopRadius = context.getResources().getDimension(rightTopCornerRadius);
            float leftBottomRadius = context.getResources().getDimension(leftBottomCornerRadius);
            float rightBottomRadius = context.getResources().getDimension(rightBottomCornerRadius);
            float[] outerRadii = new float[]{
                    leftTopRadius, leftTopRadius,
                    rightTopRadius, rightTopRadius,
                    leftBottomRadius, leftBottomRadius,
                    rightBottomRadius, rightBottomRadius,
            };
            RoundRectShape roundRectShape = new RoundRectShape(outerRadii, null, null);
            shapeDrawable = new ShapeDrawable(roundRectShape);
        }
        Paint drawablePaint = shapeDrawable.getPaint();
        drawablePaint.setColor(colorInt);
        drawablePaint.setStyle(Paint.Style.FILL);
        return shapeDrawable;
    }

    private final OnTouchListener onTouchListener = this::onTouchHandler;

    private boolean onTouch(View v, MotionEvent event) {
        onTouchHandler(v, event);
        return false;
    }

    public interface OnTouchListener {
        void onTouch(View view, MotionEvent event);
    }

    public static class Builder {
        private int mType;

        public Builder() {
        }

        public Builder(@PressType int type) {
            this.mType = type;
        }

        public PressAnimator build() {
            if (mType == PressType.TYPE_DELAY) {
                return new PressDelayAnimator();
            } else {
                return new PressNormalAnimator();
            }
        }
    }
}
