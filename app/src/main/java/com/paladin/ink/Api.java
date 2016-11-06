package com.paladin.ink;

import android.app.Activity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by jason on 11/5/16.
 */
public class Api {

    public static void signUpUser(String username, String pass, Activity activity, OnCompleteListener<AuthResult> listener) {
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.createUserWithEmailAndPassword(username, pass).addOnCompleteListener(activity, listener);
    }
    public static void loginUser(String username, String pass, Activity activity, OnCompleteListener<AuthResult> listener) {
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.signInWithEmailAndPassword(username, pass).addOnCompleteListener(activity, listener);
    }


}
