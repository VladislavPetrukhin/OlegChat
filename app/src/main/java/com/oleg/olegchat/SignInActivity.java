package com.oleg.olegchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private static final String TAG = "SignInActivityLogs";

    private TextInputLayout emailEditText, passwordEditText, passwordEditText2, nameEditText;
    private TextView toggleLoginTextView;
    private Button loginSignUpButton;

    private boolean loginModeActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Получаем экземпляр SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Читаем значение
        String theme = sharedPreferences.getString("pref_key_theme", "system");

        switch (theme) {
            case "system":
                Log.d("Theme", "Theme: system");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;

            case "light":
                Log.d("Theme", "Theme: light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case "dark":
                Log.d("Theme", "Theme: dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference usersDatabaseReference = firebaseDatabase.getReference().child("users");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordEditText2 = findViewById(R.id.passwordEditText2);
        nameEditText = findViewById(R.id.nameEditText);
        toggleLoginTextView = findViewById(R.id.toggleLoginTextView);
        loginSignUpButton = findViewById(R.id.loginSignUpButton);

        loginSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    loginSignUpUser(emailEditText.getEditText().getText().toString().trim(),
                            passwordEditText.getEditText().getText().toString(),nameEditText.getEditText().getText().toString().trim());
            }

        });

        auth = FirebaseAuth.getInstance();

        loginModeActive = true;
        toggleLoginTextView.setText("Tap to sign up");
        loginSignUpButton.setText("Log in");
        passwordEditText2.setVisibility(View.GONE);
        nameEditText.setVisibility(View.GONE);

    }
    public void loginSignUpUser(String email, String password, String name) {

        if(loginModeActive){
            logInUser(email,password);
        }else {
            signUp(email, password,name);
        }
    }

    public void toggleLogIn(View view) {
        if(loginModeActive){
            loginModeActive = false;
            toggleLoginTextView.setText("Tap to log in");
            loginSignUpButton.setText("Sign up");
            passwordEditText2.setVisibility(View.VISIBLE);
            nameEditText.setVisibility(View.VISIBLE);
        } else{
            loginModeActive = true;
            toggleLoginTextView.setText("Tap to sign up");
            loginSignUpButton.setText("Log in");
            passwordEditText2.setVisibility(View.GONE);
            nameEditText.setVisibility(View.GONE);
        }
    }
    private void signUp(String email, String password, String name){

        if(containForbiddenSymbols(name)){
            emailEditText.setError("");
            passwordEditText.setError("");
            passwordEditText2.setError("");
            nameEditText.setError("Username cannot contain such symbols");
        }else if(nameEditText.getEditText().getText().toString().trim().isEmpty()){
            emailEditText.setError("");
            passwordEditText.setError("");
            passwordEditText2.setError("");
            nameEditText.setError("Input your name");
        }else if(emailEditText.getEditText().getText().toString().trim().isEmpty()){
            nameEditText.setError("");
            passwordEditText.setError("");
            passwordEditText2.setError("");
            emailEditText.setError("Input your email");
        }else if(passwordEditText.getEditText().getText().toString().trim().isEmpty()){
            nameEditText.setError("");
            emailEditText.setError("");
            passwordEditText2.setError("");
            passwordEditText.setError("Input your password");
        }else if(passwordEditText2.getEditText().getText().toString().trim().isEmpty()){
            nameEditText.setError("");
            emailEditText.setError("");
            passwordEditText.setError("");
            passwordEditText2.setError("Confirm your password");
        }else if(!passwordEditText.getEditText().getText().toString().equals(passwordEditText2.getEditText().getText().toString())){
            nameEditText.setError("");
            emailEditText.setError("");
            passwordEditText.setError("Passwords do not match");
            passwordEditText2.setError("Passwords do not match");
        }else if(passwordEditText.getEditText().getText().toString().length() < 6){
            nameEditText.setError("");
            emailEditText.setError("");
            passwordEditText2.setError("");
            passwordEditText.setError("Password must be 6 symbols at least");
        }else if(nameEditText.getEditText().getText().toString().trim().length() < 3){
            emailEditText.setError("");
            passwordEditText.setError("");
            passwordEditText2.setError("");
            nameEditText.setError("Username must be a 3 symbols at least");
            }else if(containForbiddenSymbols(passwordEditText.getEditText().getText().toString())) {
            nameEditText.setError("");
            emailEditText.setError("");
            passwordEditText2.setError("");
            passwordEditText.setError("Password cannot contain such symbols");
        }else {
            nameEditText.setError("");
            emailEditText.setError("");
            passwordEditText.setError("");
            passwordEditText2.setError("");
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                Log.d(TAG,name);
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                FirebaseUser user = auth.getCurrentUser();
                                if(user != null){
                                    Log.d(TAG,user.getUid().toString());
                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Log.d(TAG, "User profile updated.");
                                                    createUser(user);
                                                }else{
                                                    Log.d(TAG, "User profile update failed");
                                                    createUser(user);
                                                }
                                            });
                                    //createUser(user);
                                    startActivity(new Intent(SignInActivity.this, UserListActivity.class));
                                }
                                else{
                                    Log.e(TAG,"user = null");
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Registration failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void logInUser(String email, String password){
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            startActivity(new Intent(SignInActivity.this, UserListActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Incorrect password",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    private void createUser(FirebaseUser firebaseUser){

        FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("id").setValue(firebaseUser.getUid());
        FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("email").setValue(firebaseUser.getEmail());
        Log.d(TAG,"firabaseuser name: "+firebaseUser.getDisplayName());
        FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("name").setValue(firebaseUser.getDisplayName());
        FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("photoUrl").setValue("");
        FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("contacts").setValue("");

    }
    private void getUser(){

    }

    private boolean containForbiddenSymbols(String string){

        String[] symbols={"'", "#","/"};

        for (String symbol : symbols) {
            if (string.contains(symbol)) {
                return true;
            }
        }

        return false;
    }

}