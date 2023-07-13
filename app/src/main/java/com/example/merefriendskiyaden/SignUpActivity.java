package com.example.merefriendskiyaden;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;

public class SignUpActivity extends AppCompatActivity {

    private boolean isPasswordShowing = false;
    private boolean isConfirmPasswordShowing = false;
    private EditText edtAdminName, edtAdminEmailId, edtAdminPassword, edtAdminConfirmPassword, edtAdminMobNo;
    private ImageView imgPasswordIcon1, imgPasswordIcon2;
    private AppCompatButton btnSignUp;
    private TextView txtSignIn;
    FirebaseDB firebaseDB;
    ProgessDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtAdminName = findViewById(R.id.edtAdminName);
        edtAdminEmailId = findViewById(R.id.edtAdminEmail);
        edtAdminPassword = findViewById(R.id.edtAdminPassword);
        edtAdminConfirmPassword = findViewById(R.id.edtAdminConPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        imgPasswordIcon1 = findViewById(R.id.imgPasswordIcon1);
        imgPasswordIcon2 = findViewById(R.id.imgPasswordIcon2);
        edtAdminMobNo = findViewById(R.id.edtAdminMobNo);
        txtSignIn = findViewById(R.id.txtSignIn);

        firebaseDB = new FirebaseDB();
        progressDialog = new ProgessDialog(SignUpActivity.this);

        imgPasswordIcon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isPasswordShowing) {
                    isPasswordShowing = false;

                    edtAdminPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgPasswordIcon1.setImageResource(R.drawable.password_show);
                } else {
                    isPasswordShowing = true;

                    edtAdminPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgPasswordIcon1.setImageResource(R.drawable.password_hide);
                }

                // move the cursor at last of the text
                edtAdminPassword.setSelection(edtAdminPassword.length());

            }
        });

        imgPasswordIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isConfirmPasswordShowing) {
                    isConfirmPasswordShowing = false;

                    edtAdminConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgPasswordIcon2.setImageResource(R.drawable.password_show);
                } else {
                    isConfirmPasswordShowing = true;

                    edtAdminConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgPasswordIcon2.setImageResource(R.drawable.password_hide);
                }

                // move the cursor at last of the text
                edtAdminConfirmPassword.setSelection(edtAdminConfirmPassword.length());

            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtAdminName.getText().toString().trim();
                String email = edtAdminEmailId.getText().toString().trim();
                String password = edtAdminPassword.getText().toString().trim();
                String mobNo = edtAdminMobNo.getText().toString().trim();
                String confirmPassword = edtAdminConfirmPassword.getText().toString().trim();

                signUpAdmin(name, email, password, confirmPassword, mobNo);
            }
        });

        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finishAffinity();
            }
        });

    }




    private void signUpAdmin(final String name, final String email, final String password, String confirmPassword, String mobNo) {

        EmailValideter emailValideter = new EmailValideter(getApplicationContext());
        if (!emailValideter.isValidCredentials(name, email, password, confirmPassword, mobNo)) {
            return;
        }

        // showLoadingDialog(SignUpActivity.this);
        progressDialog.showLoadingDialog();

        firebaseDB.getFirebaseAuthInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // Hide the progress bar
                        progressDialog.hideLoadingDialog();

                        if (task.isSuccessful()) {

                            // Admin signed up successfully
                            String encodedEmail = firebaseDB.encodeEmail(email);

                            // Save admin details in Realtime Database
                            AdminDetails adminDetails = new AdminDetails(name, email, password, mobNo);

                            DatabaseReference databaseReference = firebaseDB.getDatabaseReference().child("AdminDetails").child(encodedEmail).child("AdminInfo");
                            databaseReference.setValue(adminDetails);

                            Toast.makeText(SignUpActivity.this, "Admin sign-up successful", Toast.LENGTH_SHORT).show();

                            // Redirect to LoginActivity or HomeActivity
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            finishAffinity();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Admin sign-up failed or Email Exist", Toast.LENGTH_SHORT).show();
                            progressDialog.hideLoadingDialog();
                        }
                    }
                });
    }
}