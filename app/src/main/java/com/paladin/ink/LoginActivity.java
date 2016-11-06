package com.paladin.ink;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "INKLOGIN" ;
    ImageView background;
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    FancyButton emailButtonLogin, emailButtonSignup;
    EditText usernameField, passwordField;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;

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

        usernameField = (EditText) findViewById(R.id.email);
        passwordField = (EditText) findViewById(R.id.password);


        emailButtonLogin = (FancyButton) findViewById(R.id.btn_email_login);
        emailButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "LOGIN");
                String user = usernameField.getText().toString();
                String pass = passwordField.getText().toString();
                Api.loginUser(user, pass, LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        Log.d(TAG, "login:onComplete:" + task.isSuccessful());

                        mFirebaseAuth = FirebaseAuth.getInstance();
                        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

                        firebaseDatabase = FirebaseDatabase.getInstance();
                        dbRef = firebaseDatabase.getReference("users");

//                        dbRef.child(mFirebaseUser.getUid()).child("pics").setValue(1, new DatabaseReference.CompletionListener() {
//                            @Override
//                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                Log.d(TAG, "ADDED VAL");
//                            }
//                        });


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, ":'(",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }

                    }
                });
            }
        });

        emailButtonSignup = (FancyButton) findViewById(R.id.btn_email_signup);
        emailButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "SIGN UP");
                final String user = usernameField.getText().toString();
                String pass = passwordField.getText().toString();
                Api.signUpUser(user, pass, LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        mFirebaseAuth = FirebaseAuth.getInstance();



                        FirebaseDatabase firebaseDatabase;
                        DatabaseReference dbRef;
                        firebaseDatabase = FirebaseDatabase.getInstance();
                        dbRef = firebaseDatabase.getReference("users");

                        firebaseDatabase = FirebaseDatabase.getInstance();
                        dbRef = firebaseDatabase.getReference("users");
                        dbRef.child(task.getResult().getUser().getUid()).child("email").setValue(user);


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, ":'(",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }

                    }
                });
            }
        });


    }

}
