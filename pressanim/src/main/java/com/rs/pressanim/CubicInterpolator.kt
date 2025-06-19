package com.rs.pressanim

import android.graphics.PointF
import android.view.animation.Interpolator
import kotlin.math.pow

internal class CubicInterpolator(x1: Float, y1: Float, x2: Float, y2: Float) :
    Interpolator {
    private var mLastI = 0
    private val mControlPoint1 = PointF().apply {
        x = x1
        y = y1
    }
    private val mControlPoint2 = PointF().apply {
        x = x2
        y = y2
    }

    override fun getInterpolation(input: Float): Float {
        var t = input
        // 近似求解t的值[0,1]
        for (i in mLastI until ACCURACY) {
            t = 1.0f * i / ACCURACY
            val x = cubicCurves(
                t.toDouble(),
                0.0,
                mControlPoint1.x.toDouble(),
                mControlPoint2.x.toDouble(),
                1.0
            )
            if (x >= input) {
                mLastI = i
                break
            }
        }

        var value = cubicCurves(
            t.toDouble(),
            0.0,
            mControlPoint1.y.toDouble(),
            mControlPoint2.y.toDouble(),
            1.0
        )
        if (value > 0.999) {
            value = 1.0
            mLastI = 0
        }
        return value.toFloat()
    }

    companion object {
        private const val ACCURACY = 4096

        /**
         * 求三次贝塞尔曲线(四个控制点)一个点某个维度的值
         *
         * @param t 取值[0, 1]
         * @param value0
         * @param value1
         * @param value2
         * @param value3
         * @return
         */
        fun cubicCurves(
            t: Double,
            value0: Double,
            value1: Double,
            value2: Double,
            value3: Double
        ): Double {
            var value: Double
            val u = 1 - t
            value = value0 * u.pow(3.0)
            value += 3 * u.pow(2.0) * t * value1
            value += 3 * u * t.pow(2.0) * value2
            value += t.pow(3.0) * value3
            return value
        }
    }
}