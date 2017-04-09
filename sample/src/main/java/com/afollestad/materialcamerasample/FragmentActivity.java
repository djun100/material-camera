package com.afollestad.materialcamerasample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cy.app.Log;

public class FragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        Log.w(this);

        Intent intent = getIntent();
        boolean support = intent.getBooleanExtra("support", false);

        if (savedInstanceState == null) {
            if (support)
                getSupportFragmentManager().beginTransaction().add(R.id.container, DemoSupportFragment.getInstance()).commit();
            else
                getFragmentManager().beginTransaction().add(R.id.container, DemoFragment.getInstance()).commit();
        }
    }
}