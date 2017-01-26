package com.paladin.ink;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import mehdi.sakout.fancybuttons.FancyButton;
import shem.com.materiallogin.DefaultLoginView;
import shem.com.materiallogin.DefaultRegisterView;
import shem.com.materiallogin.MaterialLoginView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "INKLOGIN" ;
    ImageView background;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();

        background = (ImageView) findViewById(R.id.background);
        background.setImageDrawable(wallpaperDrawable);

        final MaterialLoginView login = (MaterialLoginView) findViewById(R.id.login);
        ((DefaultLoginView)login.getLoginView()).setListener(new DefaultLoginView.DefaultLoginViewListener() {
            @Override
            public void onLogin(TextInputLayout loginUser, TextInputLayout loginPass) {
                String user = loginUser.getEditText().getText().toString();
                String pass = loginPass.getEditText().getText().toString();
                Api.loginUser(getApplicationContext(), user, pass, new Api.OnApiResult() {
                    @Override
                    public void onActionComplete(int status) {
                        switch(status) {
                            case 0:
                                //success
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                                break;
                            case 1:
                                Toast.makeText(getApplicationContext(), "Incorrect Username/Password", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                Toast.makeText(getApplicationContext(), "An unexpected error occured", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "Default Code", Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });

        ((DefaultRegisterView)login.getRegisterView()).setListener(new DefaultRegisterView.DefaultRegisterViewListener() {
            @Override
            public void onRegister(TextInputLayout registerUser, TextInputLayout registerPass, TextInputLayout registerPassRep) {
                String user = registerUser.getEditText().getText().toString();
                String pass = registerPass.getEditText().getText().toString();
                String passRep = registerPassRep.getEditText().getText().toString();
                if(!pass.equals(passRep)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                } else {
                    Api.signUpUser(getApplicationContext(), user, pass, new Api.OnApiResult() {
                        @Override
                        public void onActionComplete(int status) {
                            switch(status) {
                                case 0:
                                    //success
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                    break;
                                case 1:
                                    Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    Toast.makeText(getApplicationContext(), "An unexpected error occured", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(getApplicationContext(), "Default Code", Toast.LENGTH_SHORT);
                            }
                        }
                    });
                }

            }
        });
    }

}
