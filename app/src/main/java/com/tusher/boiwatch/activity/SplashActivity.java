package com.tusher.boiwatch.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.tusher.boiwatch.MainActivity;
import com.tusher.boiwatch.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.iv_splash_logo);
        TextView tagline = findViewById(R.id.tv_splash_tagline);

        // Logo animation: scale in with overshoot
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_in);
        logoAnim.setStartOffset(200);
        logoAnim.setFillAfter(true);
        logo.startAnimation(logoAnim);

        // Tagline animation: fade in + slide up, delayed
        Animation taglineAnim = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);
        taglineAnim.setStartOffset(600);
        taglineAnim.setFillAfter(true);
        tagline.startAnimation(taglineAnim);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 3000); // Slightly longer to appreciate the new splash
    }
}
