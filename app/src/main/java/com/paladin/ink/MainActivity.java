package com.paladin.ink;

import android.app.WallpaperManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.veritas1.verticalslidecolorpicker.VerticalSlideColorPicker;
import com.nineoldandroids.animation.Animator;

public class MainActivity extends AppCompatActivity {
    private static final int ANIM_SPEED = 350;
    InkView inkView;
    ImageView background;

    RelativeLayout drawingView;
    RelativeLayout lockView;

    Button colorButton;
    Button undoButton;
    Button drawButton;


    VerticalSlideColorPicker colorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up our Lockscreen
        makeFullScreen();
        startService(new Intent(this, LockScreenService.class));



        //INITIALIZATION
        setContentView(R.layout.activity_main);

        drawingView = (RelativeLayout) findViewById(R.id.drawingView);
        lockView = (RelativeLayout) findViewById(R.id.lock_layout);

        colorButton = (Button) findViewById(R.id.colorButton);
        undoButton = (Button) findViewById(R.id.undoButton);
        drawButton = (Button) findViewById(R.id.drawButton);

        colorPicker = (VerticalSlideColorPicker) findViewById(R.id.color_picker);
        colorPicker.setOnColorChangeListener(new VerticalSlideColorPicker.OnColorChangeListener() {
            @Override
            public void onColorChange(int i) {
                colorButton.setBackgroundColor(i);
                inkView.setColor(i);
            }
        });



        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = inkView.undo();
            }
        });
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDraw();
            }
        });

        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();

        background = (ImageView) findViewById(R.id.background);
        background.setImageDrawable(wallpaperDrawable);



        inkView = (InkView) findViewById(R.id.ink);
        inkView.setColor(getResources().getColor(android.R.color.white));
        inkView.setMinStrokeWidth(1.5f);
        inkView.setMaxStrokeWidth(6f);

        inkView.addListener(new InkView.InkListener() {
            @Override
            public void onInkClear() {

            }

            @Override
            public void onInkDraw() {
                YoYo.with(Techniques.FadeOut).duration(350).playOn(colorButton);
                YoYo.with(Techniques.FadeOut).duration(350).playOn(colorPicker);
                YoYo.with(Techniques.FadeOut).duration(350).playOn(undoButton);
            }

            @Override
            public void onInkUp() {
                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorButton);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorPicker);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(undoButton);
            }
        });


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
        return; //Do nothing!
    }

    public void unlockScreen(View view) {
        //Instead of using finish(), this totally destroys the process
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    private void switchToDraw() {
        YoYo.with(Techniques.ZoomOut).duration(ANIM_SPEED).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lockView.setVisibility(View.GONE);
                drawingView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn).duration(ANIM_SPEED).playOn(drawingView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(lockView);
    }
    private void switchToLock() {
        YoYo.with(Techniques.ZoomOut).duration(ANIM_SPEED).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                drawingView.setVisibility(View.GONE);
                lockView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn).duration(ANIM_SPEED).playOn(lockView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(drawingView);
    }
}
