package com.rs.pressanimator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView one;
    private TextView two;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        DoubleDrawerLayout viewById = findViewById(R.id.root);
////        viewById.openFirstDrawer();
////        viewById.openSecondDrawer();
        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        PressAnimator.get().setOnTouchListener(on；。 e)
                .addTargetAnimatorView(one)
                .init();
        AnimationDrawable animationDrawable = (AnimationDrawable) getDrawable(R.drawable.test);
        Drawable frame = animationDrawable.getFrame(2);
       h
        PressAnimator.get().setOnTouchListener(two)
                .addTargetAnimatorView(two)
                .init();
    }
}