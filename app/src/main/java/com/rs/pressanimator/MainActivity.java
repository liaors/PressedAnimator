package com.rs.pressanimator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delay_bt:
                startActivity(new Intent(this, DelayPressDemoActivity.class));
                break;
            case R.id.press_bt:
                startActivity(new Intent(this, PressDemoActivity.class));
                break;
        }
    }
}