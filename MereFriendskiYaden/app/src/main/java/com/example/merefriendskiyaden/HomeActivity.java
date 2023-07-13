package com.example.merefriendskiyaden;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView toolbarTitle;
    private Toolbar toolbar;
    private Fragment homeFragment;
    private Fragment memoriesFragment;
    private Fragment groupDetailsFragment;
    private Fragment adminDetailsFragment;
    private Fragment myProfileFragment;
    private Fragment activeFragment;
    FirebaseDB firebaseDB;

    ProgessDialog progessDialog;
    String who = "", friendAdminEmail = "", friendEmail = "";
    private String userPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        SharedPreferences loginPref = getSharedPreferences("LoginPref", MODE_PRIVATE);
        userPassword = loginPref.getString("userPassword", "Default");

        SharedPreferences preferences2 = getSharedPreferences("JoinPref", MODE_PRIVATE);
        boolean isJoinIn = preferences2.getBoolean("isJoinIn", false);
        if (isJoinIn) {
            who = "Join";
        }
        friendAdminEmail = preferences2.getString("adminEmail", "value");
        friendEmail = preferences2.getString("friendEmail", "Default");

        // Set up the custom toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        firebaseDB = new FirebaseDB();
        progessDialog = new ProgessDialog(HomeActivity.this);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (R.id.nav_home == id) {
                    switchFragment(homeFragment, "Home");
                } else if (R.id.nav_memories == id) {
                    switchFragment(memoriesFragment, "Memories");
                } else if (R.id.nav_groupDetails == id) {
                    switchFragment(groupDetailsFragment, "Group Details");
                } else if (R.id.nav_adminDetails == id) {
                    switchFragment(adminDetailsFragment, "Admin Details");
                } else if (R.id.nav_myPofile == id) {
                    switchFragment(myProfileFragment, "My Profile");
                }
                return true;
            }
        });

        // Initialize the fragments
        homeFragment = new HomeFragment(HomeActivity.this);
        memoriesFragment = new MemoriesFragment(HomeActivity.this);
        groupDetailsFragment = new GroupDetailsFragment(HomeActivity.this);
        adminDetailsFragment = new AdminDetailsFragment(HomeActivity.this);
        myProfileFragment = new MyProfileFragment(HomeActivity.this);

        // Set the homeFragment as the default active fragment
        activeFragment = homeFragment;
        switchFragment(activeFragment, "Home");
    }

    private void switchFragment(Fragment fragment, String fragmentName) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // pass, who is join, either admin or his friends
        Bundle bundle = new Bundle();
        bundle.putString("Who", who);
        bundle.putString("AdminEmail", friendAdminEmail);
        bundle.putString("friendEmail", friendEmail);
        fragment.setArguments(bundle);

        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
        activeFragment = fragment;

        if (!(activeFragment instanceof HomeFragment)) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        } else {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        }
        // Set the fragment name in the toolbar
        toolbarTitle.setText(fragmentName);
    }

    @Override
    public void onBackPressed() {

        if (activeFragment instanceof HomeFragment) {
            finishAffinity();
            System.exit(0);
        } else if (!(activeFragment instanceof HomeFragment)) {
            switchFragment(homeFragment, "Home");
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.opt_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private void showPromptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Prompt");
        builder.setMessage("Are you sure you want to proceed?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(false); // Set to true if you want the dialog to be cancelable
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.optDeleteAccount) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning!");
            builder.setMessage("If you are admin, then your group deleted!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progessDialog.showLoadingDialog();
                    if (who.equals("")) {
                        String adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();

                        if (adminEmailId != null) {
                            String adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);
                            DatabaseReference databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId);
                            StorageReference storageRef = firebaseDB.getFirebaseStorage().getReference().child("AdminDetails").child(adminEmailId);
                            storageRef.delete();
                            databaseRef.removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    if (error == null) {
                                        // Images deleted successfully from Realtime Database
                                        Log.d("DeleteImagesActivity", "Delete Images Realtime Database");
                                    } else {
                                        // Error occurred while deleting images from Realtime Database
                                        Log.e("DeleteImagesActivity", "Error deleting images from Realtime Database: " + error.getMessage());
                                    }
                                }
                            });
                        }

                        deleteUser(userPassword);

                        SharedPreferences loginPref = getSharedPreferences("LoginPref", MODE_PRIVATE);
                        SharedPreferences.Editor loginEditor = loginPref.edit();
                        loginEditor.putBoolean("isLoggedIn", false);
                        loginEditor.apply();

                    } else {
                        SharedPreferences loginPref = getSharedPreferences("JoinPref", MODE_PRIVATE);
                        SharedPreferences.Editor loginEditor = loginPref.edit();
                        loginEditor.putBoolean("isJoinIn", false);
                        loginEditor.putString("adminEmail", "");
                        loginEditor.putString("friendEmail", "");
                        loginEditor.apply();
                    }

                    progessDialog.hideLoadingDialog();

                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setCancelable(false); // Set to true if you want the dialog to be cancelable
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (itemId == R.id.optLogin) {
            if (who.equals("Join")) {
                SharedPreferences loginPref = getSharedPreferences("JoinPref", MODE_PRIVATE);
                SharedPreferences.Editor loginEditor = loginPref.edit();
                loginEditor.putBoolean("isJoinIn", false);
                loginEditor.putString("adminEmail", "");
                loginEditor.putString("friendEmail", "");
                loginEditor.apply();
            } else {
                SharedPreferences loginPref = getSharedPreferences("LoginPref", MODE_PRIVATE);
                SharedPreferences.Editor loginEditor = loginPref.edit();
                loginEditor.putBoolean("isLoggedIn", false);
                loginEditor.apply();
            }
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finishAffinity();
        }


        return super.onOptionsItemSelected(item);
    }

    public void deleteUser(String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = null;
        if (user != null) {
            credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), password);

            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(HomeActivity.this, "Account delete", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Log.d(TAG, "User account deleted.");
                                    }
                                }
                            });
                }
            });
        }
    }
}