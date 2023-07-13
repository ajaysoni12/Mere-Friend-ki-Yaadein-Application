package com.example.merefriendskiyaden;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class MyProfileFragment extends Fragment {

    Context context;

    String who = "", friendAdminEmail = "", friendEmail = "";

    TextView txtName, txtEmail, txtMobNo;
    FirebaseDB firebaseDB = new FirebaseDB();

    public MyProfileFragment(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        if (getArguments() != null) {
            who = getArguments().getString("Who");
            friendAdminEmail = getArguments().getString("AdminEmail");
            friendEmail = getArguments().getString("friendEmail");
        } else {
            Toast.makeText(context, "Something Error", Toast.LENGTH_SHORT).show();
        }

        firebaseDB = new FirebaseDB();


        txtEmail = view.findViewById(R.id.textViewEmailValue);
        txtName = view.findViewById(R.id.textViewNameValue);
        txtMobNo = view.findViewById(R.id.textViewMobNoValue);

        String adminEmailId;
        if (who.equals("")) {
            // Toast.makeText(context, "Login", Toast.LENGTH_SHORT).show();
            adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();

            if (adminEmailId != null) {
                String encodedEmail = firebaseDB.encodeEmail(adminEmailId);

                DatabaseReference databaseReference = firebaseDB.getDatabaseReference().child("AdminDetails").child(encodedEmail).child("AdminInfo");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String emailId = snapshot.child("emailId").getValue(String.class);
                        String name = snapshot.child("name").getValue(String.class);
                        String mobileNum = snapshot.child("mobNo").getValue(String.class);
                        txtEmail.setText(emailId);
                        txtName.setText(name);
                        txtMobNo.setText(mobileNum);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        } else {
            adminEmailId = friendAdminEmail;
            //Toast.makeText(context, "Join", Toast.LENGTH_SHORT).show();

            if (adminEmailId != null) {
                String adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);
                DatabaseReference databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                        .child("GroupInfo");

                databaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            String FEmailId = snapshot.child("FEmailId").getValue(String.class);

                            if (FEmailId != null && FEmailId.equals(friendEmail)) {

                                String FName = snapshot.child("FName").getValue(String.class);
                                String FMobNo = snapshot.child("FMobNo").getValue(String.class);

                                txtEmail.setText(FEmailId);
                                txtName.setText(FName);
                                txtMobNo.setText(FMobNo);
                                break;
                            }
                            // Toast.makeText(context, "" + FEmailId + " " + FName, Toast.LENGTH_SHORT).show();
                        }
                        //Toast.makeText(context, "Fetch Successful", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Something Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            }
            return view;
        }
    }