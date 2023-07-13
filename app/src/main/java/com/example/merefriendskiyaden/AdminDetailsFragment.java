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


public class AdminDetailsFragment extends Fragment {

    private TextView textViewNameValue;
    private TextView textViewEmailValue;
    private TextView textViewMobNoValue;
    Context context;
    String who = "", friendAdminEmail = "", friendEmail = "";
    FirebaseDB firebaseDB;


    public AdminDetailsFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_details, container, false);

        if (getArguments() != null) {
            who = getArguments().getString("Who");
            friendAdminEmail = getArguments().getString("AdminEmail");
            friendEmail = getArguments().getString("friendEmail");
        } else {
            Toast.makeText(context, "Something Error", Toast.LENGTH_SHORT).show();
        }

        firebaseDB = new FirebaseDB();

        textViewNameValue = view.findViewById(R.id.textViewNameValue);
        textViewEmailValue = view.findViewById(R.id.textViewEmailValue);
        textViewMobNoValue = view.findViewById(R.id.textViewMobNoValue);

        textViewNameValue.setText("Admin Not Login");
        textViewEmailValue.setText("Not Available");
        textViewMobNoValue.setText("Not Available");

        fetchAdminDetails();

        return view;
    }

    private void fetchAdminDetails() {
        String adminEmailId;
        if (who.equals("")) {
            //Toast.makeText(context, "Login", Toast.LENGTH_SHORT).show();
            adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();
        } else {
            adminEmailId = friendAdminEmail;
            //Toast.makeText(context, "Join", Toast.LENGTH_SHORT).show();
            //Toast.makeText(context, "" + adminEmailId, Toast.LENGTH_SHORT).show();
        }

        if (adminEmailId != null) {
            String encodedEmail = firebaseDB.encodeEmail(adminEmailId);

            DatabaseReference databaseReference = firebaseDB.getDatabaseReference().child("AdminDetails").child(encodedEmail).child("AdminInfo");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String emailId = snapshot.child("emailId").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String mobNo = snapshot.child("mobNo").getValue(String.class);
                    textViewEmailValue.setText(emailId);
                    textViewNameValue.setText(name);
                    textViewMobNoValue.setText(mobNo);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}