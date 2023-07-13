package com.example.merefriendskiyaden;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailId, edtPassword, edtFriendName, edtAdminEmail, edtFriendEmail, edtFriendMobNo;
    private AppCompatButton btnSignIn, btnJoinGroup;
    private TextView txtSignUp, txtForgotPassword;
    private ImageView imgPasswordIcon;
    FirebaseDB firebaseDB;
    boolean isPasswordShowing = false;
    ProgessDialog progessDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnSignIn = findViewById(R.id.btnSignIn);
        txtSignUp = findViewById(R.id.txtSignUp);
        edtEmailId = findViewById(R.id.edtEmailId);
        edtPassword = findViewById(R.id.edtPassword);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        edtFriendName = findViewById(R.id.edtFName);
        edtAdminEmail = findViewById(R.id.edtAEmail);
        edtFriendEmail = findViewById(R.id.edtFEmail);
        edtFriendMobNo = findViewById(R.id.edtFMobileNo);
        imgPasswordIcon = findViewById(R.id.imgPasswordIcon);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);


        firebaseDB = new FirebaseDB();
        progessDialog = new ProgessDialog(LoginActivity.this);

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = edtEmailId.getText().toString();
                forgotPassword(email);
            }
        });

        imgPasswordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isPasswordShowing) {
                    isPasswordShowing = false;

                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgPasswordIcon.setImageResource(R.drawable.password_show);
                } else {
                    isPasswordShowing = true;

                    edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgPasswordIcon.setImageResource(R.drawable.password_hide);
                }

                // move the cursor at last of the text
                edtPassword.setSelection(edtPassword.length());

            }
        });

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iSignIn = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(iSignIn);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userEmailId = edtEmailId.getText().toString().trim();
                String userPassword = edtPassword.getText().toString().trim();

                signInAdmin(userEmailId, userPassword);

            }
        });

        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String adminEmailId = edtAdminEmail.getText().toString().trim();
                String friendEmailId = edtFriendEmail.getText().toString().trim();
                String friendName = edtFriendName.getText().toString().trim();
                String friendMobNo = edtFriendMobNo.getText().toString().trim();

                joinGroup(adminEmailId, friendEmailId, friendName, friendMobNo);
            }
        });

    }

    public void forgotPassword(String email) {
        EmailValideter emailValideter = new EmailValideter(LoginActivity.this);
        if (!emailValideter.isValidEmail(email)) {
            Toast.makeText(LoginActivity.this, "Enter Email Id", Toast.LENGTH_SHORT).show();
            return;
        }

        progessDialog.showLoadingDialog();

        firebaseDB.getFirebaseAuthInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Password reset email sent successfully
                        // You can display a success message or navigate to a success screen
                        progessDialog.hideLoadingDialog();
                        Toast.makeText(LoginActivity.this, "Check you get reset email", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidUserException e) {
                            // User with the provided email does not exist
                            // Display an error message or alert dialog
                            progessDialog.hideLoadingDialog();
                            Toast.makeText(LoginActivity.this, "Admin not exist", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            // Other exceptions occurred
                            // Handle the exception and display an appropriate error message
                            progessDialog.hideLoadingDialog();
                            Toast.makeText(LoginActivity.this, "Admin not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void joinGroup(String adminEmailId, String friendEmailId, String friendName, String friendMobNo) {
        EmailValideter emailValideter = new EmailValideter(getApplicationContext());
        if (!emailValideter.isValidEmail(adminEmailId) || !emailValideter.isValidEmail(friendEmailId) || !emailValideter.isValidMobNo(friendMobNo)
                || friendName.equals("")) {
            Toast.makeText(this, "Invalid details", Toast.LENGTH_SHORT).show();
            return;
        }

        progessDialog.showLoadingDialog();

        firebaseDB.getFirebaseAuthInstance().fetchSignInMethodsForEmail(adminEmailId).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    if (result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                        // Email is registered with Firebase
                        // Handle the presence of the email
                        Toast.makeText(LoginActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                        progessDialog.hideLoadingDialog();

                        storeFriendInfo(adminEmailId, friendEmailId, friendName, friendMobNo);
                    } else {
                        // Email is not registered with Firebase
                        // Handle the absence of the email
                        Toast.makeText(LoginActivity.this, "Group not created yet", Toast.LENGTH_SHORT).show();
                        progessDialog.hideLoadingDialog();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error occurred while checking the email", Toast.LENGTH_SHORT).show();
                    progessDialog.hideLoadingDialog();
                }
            }
        });

    }

    public void storeFriendInfo(String adminEmailId, String friendEmailId, String friendName, String friendMobNo) {
        String adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);

        // Save the memory data to the Realtime Database
        DatabaseReference databaseReference = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                .child("GroupInfo");

        String memoryId = databaseReference.push().getKey();
        if (memoryId != null) {
            databaseReference = databaseReference.child(memoryId);

            HashMap<String, String> hs = new HashMap<>();
            hs.put("Id", memoryId);
            hs.put("FEmailId", friendEmailId);
            hs.put("FName", friendName);
            hs.put("FMobNo", friendMobNo);
            databaseReference.setValue(hs);


            // Redirect to HomeActivity
            SharedPreferences loginPref = getSharedPreferences("JoinPref", MODE_PRIVATE);
            SharedPreferences.Editor loginEditor = loginPref.edit();
            loginEditor.putBoolean("isJoinIn", true);
            loginEditor.putString("adminEmail", adminEmailId);
            loginEditor.putString("friendEmail", friendEmailId);
            loginEditor.apply();

            Intent iHome = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(iHome);
            finishAffinity();
        }

    }

    private void signInAdmin(String email, String password) {

        EmailValideter emailValideter = new EmailValideter(getApplicationContext());
        if (!emailValideter.isValidCredentials(email, password)) {
            return;
        }

        // Show the progress bar
        progessDialog.showLoadingDialog();

        firebaseDB.getFirebaseAuthInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide the progress bar
                        progessDialog.hideLoadingDialog();

                        if (task.isSuccessful()) {
                            // Admin sign-in successful
                            // Redirect to LoginActivity or HomeActivity
                            // Toast.makeText(LoginActivity.this, "Admin sign-in successful", Toast.LENGTH_SHORT).show();

                            SharedPreferences loginPref = getSharedPreferences("LoginPref", MODE_PRIVATE);
                            SharedPreferences.Editor loginEditor = loginPref.edit();
                            loginEditor.putBoolean("isLoggedIn", true);
                            loginEditor.putString("userPassword", password);
                            loginEditor.apply();

                            // Redirect to HomeActivity
                            Intent iHome = new Intent(LoginActivity.this, HomeActivity.class);
                            iHome.putExtra("Who", "Login");
                            startActivity(iHome);
                        } else {
                            // Admin sign-in failed
                            Toast.makeText(LoginActivity.this, "Details not found, Sign Up", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}