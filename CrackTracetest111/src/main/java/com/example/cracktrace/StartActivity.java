package com.example.cracktrace;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class StartActivity extends Activity {
    Button button_yes;
    Button button_no;
    EditText edittext_projectname;
    EditText edittext_projecttime;
    EditText edittext_projectlocation;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }
    class ButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {

        }
    }
}

