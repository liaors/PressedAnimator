package com.rs.pressanim

import android.view.animation.PathInterpolator

internal class CubicInterpolator(
    x1: Float, y1: Float,
    x2: Float, y2: Float
) : PathInterpolator(x1, y1, x2, y2)