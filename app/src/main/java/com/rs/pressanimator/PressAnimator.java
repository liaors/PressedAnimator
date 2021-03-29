package com.rs.pressanimator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.opengl.Visibility;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PressAnimator {
    private static final String TAG = PressAnimator.class.getSimpleName();
    private static final int DEFAULT_RADIUS = R.dimen.press_radius;
    private static final String DEFAULT_COLOR = "#0D000000";
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
    int resourceId;
    private int color;
    private int leftTopCornerRadius = DEFAULT_RADIUS;
    private int rightTopCornerRadius = DEFAULT_RADIUS;
    private int leftBottomCornerRadius = DEFAULT_RADIUS;
    private int rightBottomCornerRadius = DEFAULT_RADIUS;

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
    /**
     * 是否是圆形背景
     */
    private boolean circular;

    public static PressAnimator get() {
        return new PressAnimator();
    }

    public PressAnimator init() {
        addForeground();
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
    public PressAnimator addTargetAnimatorView(View animatorView) {
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
    public PressAnimator addTargetAnimatorViews(View... views) {
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
    public PressAnimator setIsNeedForeground(boolean isNeedForeground) {
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
            touchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    onTouchHandler(v,event);
                    return false;
                }
            });
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
     * @param circular
     * @return
     */
    public PressAnimator setCircular(boolean circular) {
        this.circular = circular;
        return this;
    }

    /**
     * 设置背景颜色值
     *
     * @param colorId colorId
     * @return
     */
    public PressAnimator setColor(@ColorRes int colorId) {
        this.color = colorId;
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
     * @return
     */
    public PressAnimator setCornerRadius(@DimenRes int leftTopCornerRadius, @DimenRes int rightTopCornerRadius, @DimenRes int leftBottomCornerRadius, @DimenRes int rightBottomCornerRadius) {
        this.leftTopCornerRadius = leftTopCornerRadius;
        this.rightTopCornerRadius = rightTopCornerRadius;
        this.leftBottomCornerRadius = leftBottomCornerRadius;
        this.rightBottomCornerRadius = rightBottomCornerRadius;
        return this;
    }

    private void onTouchHandler(View touchView, MotionEvent event) {
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
            @RequiresApi(api = Build.VERSION_CODES.M)
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
                setForegoundInVisible();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
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
                waitUpAnimator = false;
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
                } else if (centerX > targetViewCenterX) {
                    downTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, -offsetY);
                    upTranslationY = PropertyValuesHolder.ofFloat("translationY", -offsetY, 0);
                } else if (centerX < targetViewCenterX) {
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
        if (foregroundDrawable != null) {
            return foregroundDrawable;
        }
        if (resourceId != 0) {
            foregroundDrawable = targetView.getContext().getDrawable(resourceId);
        } else {
            int colorInt = color == 0 ? Color.parseColor(DEFAULT_COLOR) : ContextCompat.getColor(targetView.getContext(), color);
            getBackgroundDrawabel(targetView.getContext(), colorInt);
        }
        return foregroundDrawable;
    }

    private Drawable getBackgroundDrawabel(@NonNull Context context, @ColorInt int colorInt) {
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
