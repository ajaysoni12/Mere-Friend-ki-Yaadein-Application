package com.example.merefriendskiyaden;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splash_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activity);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // this if for admin
                SharedPreferences preferences1 = getSharedPreferences("LoginPref", MODE_PRIVATE);
                boolean isLoggedIn = preferences1.getBoolean("isLoggedIn", false);

                // this is for user or friends
                SharedPreferences preferences2 = getSharedPreferences("JoinPref", MODE_PRIVATE);
                boolean isJoinIn = preferences2.getBoolean("isJoinIn", false);

                // if isLoggedIn or isJoinIn true, then go main(home) activity
                if (isLoggedIn || isJoinIn) {
                    startActivity(new Intent(Splash_Activity.this, HomeActivity.class));
                }
                // otherwise go, login activity
                else {
                    startActivity(new Intent(Splash_Activity.this, LoginActivity.class));
                }
                finish();
            }
        }, 3000);

    }
}