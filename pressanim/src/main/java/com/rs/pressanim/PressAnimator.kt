package com.rs.pressanim

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.math.abs
import kotlin.math.min

/**
 * @author rs
 */
abstract class PressAnimator {

    private val downCurvature = floatArrayOf(0.33f, 0f, 0.67f, 1f)
    private val upCurvature = floatArrayOf(0.33f, 0f, 0.67f, 1f)

    /**
     * 目标view;
     */
    private var targetView: View? = null

    private var foregroundDrawable: Drawable? = null

    @DrawableRes
    private var maskDrawable = 0
    private var colorRes = 0
    private var leftTopCornerRadius = DEFAULT_RADIUS
    private var rightTopCornerRadius = DEFAULT_RADIUS
    private var leftBottomCornerRadius = DEFAULT_RADIUS
    private var rightBottomCornerRadius = DEFAULT_RADIUS
    private var mVibrator:Boolean = true

    private val animatorViews: MutableList<View> = ArrayList()

    /**
     * 默认缩放率
     */
    private var scaleRatio = 0.65f
    private var upAnimator: ObjectAnimator? = null
    private var downAnimator: ObjectAnimator? = null
    private var upAnimatorX: PropertyValuesHolder? = null
    private var upAnimatorY: PropertyValuesHolder? = null
    private var downAnimatorX: PropertyValuesHolder? = null
    private var downAnimatorY: PropertyValuesHolder? = null

    /**
     * 是否需要前景 默认需要
     */
    private var isNeedForeground = true

    /**
     * 是否是圆形背景
     */
    private var circular = false
    protected var isStartedDownAnimate: Boolean = false
    private var lifecycleBoundObserver: LifecycleBoundObserver? = null
    private var isTouch = false

    /**
     * 是否等待抬起动画
     */
    protected var waitUpAnimator: Boolean = false
    protected var upAnimatorSet: AnimatorSet? = null
    protected var downAnimatorSet: AnimatorSet? = null

    protected open fun onTouchHandler(v: View, event: MotionEvent) {
        isTouch =
            (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE)
    }

    /**
     * 初始化
     *
     * @return this
     */
    fun init(): PressAnimator {
        val targetView = getTargetView()
        if (targetView != null && targetView.width > 0) {
            addForeground()
        }
        return this
    }

    /**
     * 缩放比例
     *
     * @param scaleRatio 0.0f -1.0f 1.0f表示不缩放
     * @return this
     */
    fun setScaleRatio(@FloatRange(from = 0.0, to = 1.0) scaleRatio: Float): PressAnimator {
        this.scaleRatio = scaleRatio
        return this
    }

    fun cancel() {
        if (upAnimatorSet?.isRunning == true) {
            upAnimatorSet!!.cancel()
        } else if (downAnimator?.isRunning == true) {
            downAnimator!!.cancel()
        }
        lifecycleBoundObserver?.finish()
    }

    fun with(context: Context): PressAnimator {
        val activity = getActivity(context)
        if (activity is FragmentActivity) {
            lifecycleBoundObserver = LifecycleBoundObserver(this, activity)
            activity.lifecycle.addObserver(lifecycleBoundObserver!!)
        }
        return this
    }

    fun with(fragment: Fragment): PressAnimator {
        lifecycleBoundObserver = LifecycleBoundObserver(this, fragment)
        fragment.lifecycle.addObserver(lifecycleBoundObserver!!)
        return this
    }


    /**
     * @param maskDrawable 遮罩资源
     * @return this
     */
    fun setMaskDrawable(@DrawableRes maskDrawable: Int): PressAnimator {
        this.maskDrawable = maskDrawable
        return this
    }

    fun setVibrator(enable: Boolean): PressAnimator {
        this.mVibrator = enable
        return this
    }

    /**
     * 添加需要动画的view
     *
     * @param animatorView 需要动画的view
     * @param isMaxWh      这一组动画view中，这个view是否是最大宽高
     * @return this
     */
    @JvmOverloads
    fun addTargetAnimatorView(animatorView: View?, isMaxWh: Boolean = false): PressAnimator {
        if (animatorView != null && !animatorViews.contains(animatorView)) {
            if (isMaxWh) {
                targetView = animatorView
                animatorViews.add(0, animatorView)
            } else {
                animatorViews.add(animatorView)
            }
        }
        return this
    }

    /**
     * 添加动画集合
     *
     * @param views              动画集合
     * @param firstViewMaxWh 如果为true，需要此view宽高最大
     * @return this
     */
    @JvmOverloads
    fun addTargetAnimatorViews(
        vararg views: View?,
        firstViewMaxWh: Boolean = false
    ): PressAnimator {
        if (views.isEmpty()) {
            return this
        }
        if (firstViewMaxWh) {
            targetView = views[0]
        }
        for (view in views) {
            if (!animatorViews.contains(view)) {
                animatorViews.add(view!!)
            }
        }
        return this
    }

    /**
     * 设置是否需要前景
     *
     * @param isNeedForeground true便是需要
     * @return this
     */
    fun setNeedForeground(isNeedForeground: Boolean): PressAnimator {
        this.isNeedForeground = isNeedForeground
        return this
    }

    /**
     * 设置可点击的view的监听
     *
     * @param touchView 可点击的view，用来接听监听
     * @return this
     */
    fun setOnTouchListener(touchView: View?): PressAnimator {
        touchView?.setOnTouchListener { v: View, event: MotionEvent ->
            this.onTouch(v, event)
        }
        return this
    }

    /**
     * 设置前景drawable
     *
     * @param foregroundDrawable foregroundDrawable
     * @return drawable
     */
    fun setForegroundDrawable(foregroundDrawable: Drawable?): PressAnimator {
        this.foregroundDrawable = foregroundDrawable
        return this
    }

    /**
     * 是否是圆形背景
     *
     * @param circular true表示是圆形背景
     * @return PressAnimator的具体实现类
     */
    fun setCircular(circular: Boolean): PressAnimator {
        this.circular = circular
        return this
    }

    /**
     * 设置背景颜色值
     *
     * @param colorId colorId
     * @return this
     */
    fun setColor(@ColorRes colorId: Int): PressAnimator {
        this.colorRes = colorId
        return this
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
    @JvmOverloads
    fun setCornerRadius(
        @DimenRes leftTopCornerRadius: Int = DEFAULT_RADIUS,
        @DimenRes rightTopCornerRadius: Int = DEFAULT_RADIUS,
        @DimenRes leftBottomCornerRadius: Int = DEFAULT_RADIUS,
        @DimenRes rightBottomCornerRadius: Int = DEFAULT_RADIUS
    ): PressAnimator {
        this.leftTopCornerRadius = leftTopCornerRadius
        this.rightTopCornerRadius = rightTopCornerRadius
        this.leftBottomCornerRadius = leftBottomCornerRadius
        this.rightBottomCornerRadius = rightBottomCornerRadius
        return this
    }

    protected fun startDownAnimator() {
        addForeground()
        downAnimatorSet?.start()
    }

    protected fun startUpAnimator() {
        waitUpAnimator = false
        if (upAnimatorSet != null) {
            upAnimatorSet?.start()
        } else if (animatorViews.isNotEmpty()) {
            Log.i(javaClass.simpleName, "startUpAnimator: upAnimatorSet is null")
            initAnimator()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                upAnimatorSet?.reverse()
            } else {
                upAnimatorSet?.start()
            }
        }
    }

    /**
     * 将最大的view作为targetView
     *
     * @return View
     */
    protected fun getTargetView(): View? {
        if (targetView == null) {
            animatorViews.sortWith { o1: View, o2: View ->
                val o1Result = o1.width + o1.height
                val o2Result = o2.width + o2.height
                o2Result - o1Result
            }
            val firstView = animatorViews[0]
            if (firstView != null && firstView.width > 0) {
                targetView = animatorViews[0]
            }
            targetView?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    cancel()
                }

                override fun onViewDetachedFromWindow(v: View) {
                    targetView!!.removeOnAttachStateChangeListener(this)
                }
            })

        }
        return targetView
    }

    protected fun initAnimator() {
        if (targetView == null) {
            return
        }
        if (upAnimatorSet != null) {
            return
        }
        upAnimatorSet = AnimatorSet()
        downAnimatorSet = AnimatorSet()
        upAnimatorX = PropertyValuesHolder.ofFloat("scaleX", scaleRatio, 1f)
        upAnimatorY = PropertyValuesHolder.ofFloat("scaleY", scaleRatio, 1f)
        downAnimatorX = PropertyValuesHolder.ofFloat("scaleX", 1f, scaleRatio)
        downAnimatorY = PropertyValuesHolder.ofFloat("scaleY", 1f, scaleRatio)
        val targetViewLocation = IntArray(2)
        targetView!!.getLocationOnScreen(targetViewLocation)
        val targetViewCenterX = targetViewLocation[0] + targetView!!.width / 2
        val targetViewCenterY = targetViewLocation[1] + targetView!!.height / 2
        val animatorSet = createAnimatorSet(targetView!!,targetViewCenterX, targetViewCenterY)
        downAnimatorSet?.setDuration(DOWN_DURATION.toLong())
        downAnimatorSet?.interpolator = CubicInterpolator(
            downCurvature[0],
            downCurvature[1],
            downCurvature[2],
            downCurvature[3]
        )
        downAnimatorSet?.playTogether(animatorSet[0])
        // 抬起动画
        upAnimatorSet?.setDuration(UP_DURATION.toLong())
        upAnimatorSet?.interpolator = CubicInterpolator(
            upCurvature[0],
            upCurvature[1],
            upCurvature[2],
            upCurvature[3]
        )
        upAnimatorSet?.playTogether(animatorSet[1])
        iniDownAnimator()
        initUpAnimator()
    }

    private fun initUpAnimator() {
        upAnimator!!.addUpdateListener { valueAnimator: ValueAnimator ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (targetView != null && targetView!!.foreground != null && isNeedForeground) {
                    val animatedFraction = valueAnimator.animatedFraction
                    val alpha = ((1 - animatedFraction) * 255).toInt()
                    targetView!!.foreground.alpha = alpha
                }
            }
        }
        upAnimatorSet!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                isStartedDownAnimate = false
                setForegroundInVisible()
            }

            override fun onAnimationCancel(animator: Animator) {
                isStartedDownAnimate = false
                setForegroundInVisible()
            }

            override fun onAnimationRepeat(animator: Animator) {
                Log.i(javaClass.simpleName, "onAnimationRepeat: ")
            }
        })
    }


    private fun setForegroundInVisible() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (targetView?.foreground != null && isNeedForeground) {
                targetView?.foreground?.alpha = 0
            }
        }
    }

    private fun iniDownAnimator() {
        downAnimator?.addUpdateListener { valueAnimator: ValueAnimator ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (targetView != null && targetView!!.foreground != null && isNeedForeground) {
                    val animatedFraction = valueAnimator.animatedFraction
                    val alpha = (255 * animatedFraction).toInt()
                    targetView!!.foreground.alpha = alpha
                }
            }
        }
        downAnimatorSet?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                if (waitUpAnimator) {
                    startUpAnimator()
                } else {
                    vibrator()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                waitUpAnimator = false
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
    }

    private fun vibrator() {
        if (mVibrator && targetView != null && isTouch) {
            val vibrator = targetView!!.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATOR_TIME, VIBRATOR_VALUE))
            } else {
                vibrator.vibrate(VIBRATOR_TIME)
            }
        }
    }

    private fun createAnimatorSet(
        targetView:View,
        targetViewCenterX: Int,
        targetViewCenterY: Int
    ): Array<Collection<Animator>> {
        val downAnimators: MutableList<Animator> = ArrayList(animatorViews.size)
        val upAnimators: MutableList<Animator> = ArrayList(animatorViews.size)
        for (i in animatorViews.indices) {
            val view = animatorViews[i]
            // 第一个view为targetView,不需要设置偏移量
            if (view === targetView) {
                upAnimator = ObjectAnimator.ofPropertyValuesHolder(view, upAnimatorX, upAnimatorY)
                downAnimator =
                    ObjectAnimator.ofPropertyValuesHolder(view, downAnimatorX, downAnimatorY)
                upAnimators.add(upAnimator!!)
                downAnimators.add(downAnimator!!)
            } else {
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                val centerX = location[0] + view.width / 2
                val centerY = location[1] + view.height / 2
                // 计算附属view的带偏移量的集合
                var downTranslationX: PropertyValuesHolder?
                var upTranslationX: PropertyValuesHolder?
                // Math.abs(centerX - targetViewCenterX) <= 1 系统获取的中心点位置可能有1以内的误差
                if (view.visibility == View.GONE || centerX == targetViewCenterX) {
                    downTranslationX = PropertyValuesHolder.ofFloat(TRANSLATION_X, 0f, 0f)
                    upTranslationX = PropertyValuesHolder.ofFloat(TRANSLATION_X, 0f, 0f)
                } else {
                    val offsetX = (1 - scaleRatio) * (targetViewCenterX - centerX)
                    downTranslationX = PropertyValuesHolder.ofFloat(TRANSLATION_X, 0f, offsetX)
                    upTranslationX = PropertyValuesHolder.ofFloat(TRANSLATION_X, offsetX, 0f)
                }
                val downTranslationY: PropertyValuesHolder
                val upTranslationY: PropertyValuesHolder
                if (view.visibility == View.GONE || centerY == targetViewCenterY) {
                    downTranslationY = PropertyValuesHolder.ofFloat(TRANSLATION_Y, 0f, 0f)
                    upTranslationY = PropertyValuesHolder.ofFloat(TRANSLATION_Y, 0f, 0f)
                }  else {
                    val offsetY = (1 - scaleRatio) * (targetViewCenterY - centerY)
                    downTranslationY = PropertyValuesHolder.ofFloat(TRANSLATION_Y, 0f, offsetY)
                    upTranslationY = PropertyValuesHolder.ofFloat(TRANSLATION_Y, offsetY, 0f)
                }
                val upAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    view,
                    upTranslationX,
                    upTranslationY,
                    upAnimatorX,
                    upAnimatorY
                )
                val downAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    view,
                    downTranslationX,
                    downTranslationY,
                    downAnimatorX,
                    downAnimatorY
                )
                upAnimators.add(upAnimator)
                downAnimators.add(downAnimator)
            }
        }
        val upDownAnimators: Array<Collection<Animator>> = arrayOf(downAnimators, upAnimators)
        upDownAnimators[0] = downAnimators
        upDownAnimators[1] = upAnimators
        return upDownAnimators
    }

    private fun offsetY(
        centerY: Int,
        targetViewCenterY: Int,
    ): Float {
        val offsetY = (1 - scaleRatio) * if (centerY > targetViewCenterY) {
            // 由来：  val bottomMargin = (targetViewCenterY + targetView.height / 2) - (centerY + view.height / 2)
            // result =  (targetView!!.height - view.height)/ 2f - bottomMargin
            centerY - targetViewCenterY
        } else {
            // 由来： val topMargin = (centerY - view.height / 2) - (targetViewCenterY - targetView.height / 2)
            // result = (targetView!!.height - view.height) * 1.0f / 2f  - topMargin
            // result = targetViewCenterY - centerY
            targetViewCenterY - centerY
        }
        return offsetY
    }

    private fun offsetX(
        centerX: Int,
        targetViewCenterX: Int,
    ): Float {
        val offsetX =   if(centerX < targetViewCenterX){
            // (1 - scaleRatio) *（targetView!!.width - view.width) / 2 - marginStart）
         //   (targetView!!.width - view.width) / 2f - (centerX - view.width / 2) + (targetViewCenterX - targetView.width / 2)
            (1 - scaleRatio)*(targetViewCenterX- centerX).toFloat()
        } else {
            // - ((Ow-Ow1)/2 - marginLeft )
//            -((targetView.width - view.width)/2 - (centerX - view.width / 2) + (targetViewCenterX - targetView.width / 2))
             // 或者
           //(targetView!!.width - view.width) / 2f - ((targetViewCenterX + targetView.width / 2) + (centerX + view.width / 2))
           (1 - scaleRatio) * (-targetViewCenterX + centerX)
        }
        return offsetX
    }

    private fun addForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (targetView != null && targetView!!.foreground == null && isNeedForeground) {
                val foregroundDrawable = getForegroundDrawable()
                targetView!!.foreground = foregroundDrawable
                foregroundDrawable!!.alpha = 0
            }
        }
    }

    /**
     * 获取前景
     *
     * @return Drawable
     */
    private fun getForegroundDrawable(): Drawable? {
        if (foregroundDrawable != null) {
            return foregroundDrawable
        }
        if (maskDrawable != 0) {
            foregroundDrawable = ContextCompat.getDrawable(targetView!!.context, maskDrawable)
        } else {
            val colorInt =
                if (colorRes == 0) Color.parseColor(DEFAULT_COLOR) else ContextCompat.getColor(
                    targetView!!.context, colorRes
                )
            foregroundDrawable = getBackgroundDrawable(targetView!!.context, colorInt)
        }
        return foregroundDrawable
    }

    private fun getBackgroundDrawable(context: Context, @ColorInt colorInt: Int): Drawable {
        val shapeDrawable: ShapeDrawable
        if (circular) {
            shapeDrawable = ShapeDrawable(OvalShape())
        } else {
            val leftTopRadius = context.resources.getDimension(leftTopCornerRadius)
            val rightTopRadius = context.resources.getDimension(rightTopCornerRadius)
            val leftBottomRadius = context.resources.getDimension(leftBottomCornerRadius)
            val rightBottomRadius = context.resources.getDimension(rightBottomCornerRadius)
            val outerRadii = floatArrayOf(
                leftTopRadius, leftTopRadius,
                rightTopRadius, rightTopRadius,
                leftBottomRadius, leftBottomRadius,
                rightBottomRadius, rightBottomRadius,
            )
            val roundRectShape = RoundRectShape(outerRadii, null, null)
            shapeDrawable = ShapeDrawable(roundRectShape)
        }
        val drawablePaint = shapeDrawable.paint
        drawablePaint.color = colorInt
        drawablePaint.style = Paint.Style.FILL
        return shapeDrawable
    }

    /**
     * 获取onTouch事件回调
     *
     * @return OnTouchListener
     */
    val onTouchListener = OnTouchListener(::onTouchHandler)

    private fun onTouch(v: View, event: MotionEvent): Boolean {
        onTouchHandler(v, event)
        return false
    }

    private fun getActivity(context: Context): Activity? {
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context.baseContext
        }
        return null
    }

    fun interface OnTouchListener {
        fun onTouch(view: View, event: MotionEvent)
    }

    class Builder(@PressType type: Int = PressType.TYPE_NORMAL) {
        private var mType = type

        fun build(): PressAnimator {
            return if (mType == PressType.TYPE_DELAY) {
                PressDelayAnimator()
            } else {
                PressNormalAnimator()
            }
        }
    }

    companion object {
        private val DEFAULT_RADIUS = R.dimen.press_radius

        // 按压动画颜色
        private const val DEFAULT_COLOR = "#4F000000"
        private const val DOWN_DURATION = 200
        private const val UP_DURATION = 200
        private const val TRANSLATION_X = "translationX"
        private const val TRANSLATION_Y = "translationY"
        private const val VIBRATOR_TIME = 100L
        private const val VIBRATOR_VALUE = 255
    }
}
