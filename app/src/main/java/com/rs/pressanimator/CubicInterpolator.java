package com.rs.pressanimator;

import android.graphics.PointF;
import android.view.animation.Interpolator;

class CubicInterpolator  implements Interpolator {
    private final static int ACCURACY = 4096;
    private int mLastI = 0;
    private final PointF mControlPoint1 = new PointF();
    private final PointF mControlPoint2 = new PointF();

    /**
     * 设置中间两个控制点.<br>
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public CubicInterpolator(float x1, float y1, float x2, float y2) {
        mControlPoint1.x = x1;
        mControlPoint1.y = y1;
        mControlPoint2.x = x2;
        mControlPoint2.y = y2;
    }
    @Override
    public float getInterpolation(float input) {
        float t = input;
        // 近似求解t的值[0,1]
        for (int i = mLastI; i < ACCURACY; i++) {
            t = 1.0f * i / ACCURACY;
            double x = cubicCurves(t, 0, mControlPoint1.x, mControlPoint2.x, 1);
            if (x >= input) {
                mLastI = i;
                break;
            }
        }

        double value = cubicCurves(t, 0, mControlPoint1.y, mControlPoint2.y, 1);
        if (value > 0.999d) {
            value = 1;
            mLastI = 0;
        }
        return (float) value;
    }
    /**
     *求三次贝塞尔曲线(四个控制点)一个点某个维度的值.<br>
     *
     * @param t 取值[0, 1]
     * @param value0
     * @param value1
     * @param value2
     * @param value3
     * @return
     */
    public static double cubicCurves(double t, double value0, double value1,

                                     double value2, double value3) {
        double value;
        double u = 1 - t;
        value =  value0 * Math.pow(u,3);
        value += 3 * Math.pow(u,2) * t * value1;
        value += 3 * u * Math.pow(t,2) * value2;
        value += Math.pow(t,3) * value3;
        return value;
    }
}