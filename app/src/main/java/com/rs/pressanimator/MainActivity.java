package com.rs.pressanimator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.rs.pressanim.PressAnimator;
import com.rs.pressanim.PressDelayAnimator;

public class MainActivity extends AppCompatActivity {
    private TextView two;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        two = findViewById(R.id.two);
        PressAnimator.get().setOnTouchListener(two)
                .addTargetAnimatorView(two)
                .init();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delay_bt:
                startActivity(new Intent(this, DelayPressDemoActivity.class));
                break;
        }
    }
}