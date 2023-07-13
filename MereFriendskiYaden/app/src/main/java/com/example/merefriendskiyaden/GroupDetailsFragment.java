package com.example.merefriendskiyaden;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupDetailsFragment extends Fragment {

    private TextView txtGroupName;
    private RecyclerView groupDetailsRecyclerView;
    private GroupDetailsAdapter groupDetailsAdapter;
    private List<GroupMember> groupMembers;
    Context context;
    FirebaseDB firebaseDB;
    String who = "", friendAdminEmail = "", friendEmail = "";

    public GroupDetailsFragment(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_details, container, false);

        if (getArguments() != null) {
            who = getArguments().getString("Who");
            friendAdminEmail = getArguments().getString("AdminEmail");
            friendEmail = getArguments().getString("friendEmail");
            // Toast.makeText(context, "" + friendAdminEmail, Toast.LENGTH_SHORT).show();
            // Toast.makeText(context, "" + friendEmail, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "None", Toast.LENGTH_SHORT).show();
        }

        txtGroupName = view.findViewById(R.id.txtGroupName);
        groupDetailsRecyclerView = view.findViewById(R.id.groupDetailsRecyclerView);

        firebaseDB = new FirebaseDB();
        // Set the group name
        txtGroupName.setText("Group Name");
        txtGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInputDialog();
            }
        });

        // Initialize the RecyclerView and its adapter
        groupMembers = new ArrayList<>();
        groupDetailsAdapter = new GroupDetailsAdapter(groupMembers, context);
        groupDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupDetailsRecyclerView.setAdapter(groupDetailsAdapter);

        // Add some dummy group member data for testing

        fetchFriendsDetails();

        // groupDetailsAdapter.notifyDataSetChanged();

        return view;
    }

    public void uploadGroupName(String groupName) {
        // String adminEmailId = friendAdminEmail;
        String adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();

        // String adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();
        if (adminEmailId != null) {
            String adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);
            DatabaseReference databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                    .child("Group Name").child("Name");
            databaseRef.setValue(groupName);
            Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT).show();

        }

    }

    private void openInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Group Name");

        // Create an EditText view for user input
        final EditText inputEditText = new EditText(context);
        builder.setView(inputEditText);

        // Set the positive button to handle data retrieval
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredData = inputEditText.getText().toString();
                txtGroupName.setText(enteredData);
                uploadGroupName(txtGroupName.getText().toString());

            }
        });

        // Set the negative button to cancel the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }


    public void fetchFriendsDetails() {
        String adminEmailId;
        if (who.equals("")) {
            // Toast.makeText(context, "Login", Toast.LENGTH_SHORT).show();
            adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();
        } else {
            adminEmailId = friendAdminEmail;
            // Toast.makeText(context, "Join", Toast.LENGTH_SHORT).show();
        }

        if (adminEmailId != null) {
            String adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);
            DatabaseReference databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                    .child("GroupInfo");

            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    groupMembers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        // list.add(snapshot.getValue(String.class));
                        String FEmailId = snapshot.child("FEmailId").getValue(String.class);
                        String FName = snapshot.child("FName").getValue(String.class);
                        String FMobNo = snapshot.child("FMobNo").getValue(String.class);
                        String FId = snapshot.child("Id").getValue(String.class);

                        groupMembers.add(new GroupMember(FName, FEmailId, FMobNo, FId));
                        // Toast.makeText(context, "" + FEmailId + " " + FName, Toast.LENGTH_SHORT).show();
                    }
                    // Notify the adapter that the data has changed
                    // Toast.makeText(context, "size: " + groupMemberList.size(), Toast.LENGTH_SHORT).show();
                    groupDetailsAdapter.notifyDataSetChanged();
                    // Toast.makeText(context, "Fetch Successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Something Error", Toast.LENGTH_SHORT).show();
                }
            });

           databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                    .child("Group Name");
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String groupName = snapshot.child("Name").getValue(String.class);
                    if (groupName == null || groupName.equals("")) {
                        txtGroupName.setText("Group Name");
                    } else
                        txtGroupName.setText(groupName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Something Error!", Toast.LENGTH_SHORT).show();
        }

    }
}