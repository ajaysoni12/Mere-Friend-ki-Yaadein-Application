package com.example.merefriendskiyaden;

import android.content.Context;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValideter {

    Context context;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    public EmailValideter(Context context) {
        this.context = context;
    }

    public boolean isValidEmail(String userEmailId) {
        Matcher matcher = emailPattern.matcher(userEmailId);
        return matcher.matches();
    }

    public boolean isValidPassword(String userPassword) {
        Matcher matcher = passwordPattern.matcher(userPassword);
        return matcher.matches();
    }

    public boolean isSamePassword(String userPassword, String userConfirmPassword) {
        return userPassword.equals(userConfirmPassword);
    }

    public boolean isValidMobNo(String mobNo) {
        return !mobNo.equals("") && mobNo.length() == 13;
    }

    public boolean isValidCredentials(String userEmailId, String userPassword) {

        if (userEmailId.equals("") && userPassword.equals("")) {
            Toast.makeText(context, "Enter Details", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (userEmailId.equals("")) {
            Toast.makeText(context, "Enter EmailId", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (userPassword.equals("")) {
            Toast.makeText(context, "Enter Password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(userEmailId)) {
            Toast.makeText(context, "Enter valid emailId", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidPassword(userPassword)) {
            Toast.makeText(context, "Enter valid Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean isValidCredentials(String name, String userEmailId, String userPassword, String conUserPassword,
                                      String userMobNo) {

        if (name.equals("") || userEmailId.equals("") || userPassword.equals("") ||
                conUserPassword.equals("") || userMobNo.equals("")) {
            Toast.makeText(context, "Enter Details", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(userEmailId)) {
            Toast.makeText(context, "Enter valid emailId", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidPassword(userPassword) || !isValidPassword(conUserPassword)) {
            Toast.makeText(context, "Enter valid Password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!isValidMobNo(userMobNo)) {
            Toast.makeText(context, "Enter valid mobile no", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!isSamePassword(userPassword, conUserPassword)) {
            Toast.makeText(context, "Password not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
