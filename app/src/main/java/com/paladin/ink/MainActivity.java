package com.paladin.ink;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends AppCompatActivity implements UnlockFragment.OnUnlockSuccessListener, ClockFragment.OnClockFragmentInteractionListener{
    ImageView background;
    private CustomViewPager mPager;
    private PagerAdapter mPagerAdapter;

    ClockFragment clockFragment;
    UnlockFragment unlockFragment;

    Api inkApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up our Lockscreen
        clockFragment = new ClockFragment();
        unlockFragment = new UnlockFragment();

        makeFullScreen();
        startService(new Intent(this, LockScreenService.class));



        //INITIALIZATION
        setContentView(R.layout.activity_main);



        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();

        background = (ImageView) findViewById(R.id.background);
        background.setImageDrawable(wallpaperDrawable);

        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(1);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == 0) {
                    clockFragment.setLayoutOpacity(positionOffset);
                } else {
                    clockFragment.setLayoutOpacity(1.0f);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1) {
                    clockFragment.setLayoutOpacity(1.0f);
                } else {
                    clockFragment.setLayoutOpacity(0.0f);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        SharedPreferences prefs = getSharedPreferences("inklocksharedprefs", MODE_PRIVATE);
        if (!prefs.contains("auth_key")) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        } else {

        }

    }

    /**
     * A simple method that sets the screen to fullscreen.  It removes the Notifications bar,
     *   the Actionbar and the virtual keys (if they are on the phone)
     */
    public void makeFullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    @Override
    public void onBackPressed() {
        if(clockFragment.getStatus() != ClockFragment.Mode.LOCK) {
            clockFragment.switchToLock();
            onSwitchToClock();
        }
    }

    public void unlockScreen(View view) {
        //Instead of using finish(), this totally destroys the process
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onSwitchToDraw() {
        mPager.setPagingEnabled(false);
    }

    @Override
    public void onSwitchToLock() {

    }

    @Override
    public void onSwitchToSelect() {
//        mPager.setPagingEnabled(false);
    }

    @Override
    public void onSwitchToClock() {
        mPager.setPagingEnabled(true);
    }

    @Override
    public void onUnlockSuccess() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return unlockFragment;
                case 1:
                    return clockFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void setPagingEnabled(boolean enabled) {
        mPager.setPagingEnabled(enabled);
    }
}
