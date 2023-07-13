package com.example.merefriendskiyaden;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MemoriesFragment extends Fragment {

    FirebaseDB firebaseDB;
    private List<Memory> memories;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddMemory;
    private Context context;
    private MemoriesAdapter adapter;
    String who = "", friendAdminEmail = "", friendEmail = "";

    public MemoriesFragment(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_memories, container, false);

        if (getArguments() != null) {
            who = getArguments().getString("Who");
            friendAdminEmail = getArguments().getString("AdminEmail");
            friendEmail = getArguments().getString("friendEmail");
        } else {
            Toast.makeText(context, "Something Error", Toast.LENGTH_SHORT).show();
        }

        firebaseDB = new FirebaseDB();

        // Initialize memories list
        memories = new ArrayList<>();

        // Initialize RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new MemoriesAdapter(memories, context);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize the FloatingActionButton for adding a new memory
        fabAddMemory = view.findViewById(R.id.fabAddMemory);
        fabAddMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the button click to navigate to NewMemoryActivity
                Intent intent = new Intent(context, NewMemoryActivity.class);
                startActivity(intent);
            }
        });

        if(who.equals("Join")) {
            fabAddMemory.setVisibility(View.GONE);
        }

        fetchMemoriesFromDatabase();

        return view;
    }

    private void fetchMemoriesFromDatabase() {

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
                    .child("Memories");

            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    memories.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Retrieve data for each memory

                        String memoryId = snapshot.child("Id").getValue(String.class);
                        String imageUri = snapshot.child("Uri").getValue(String.class);
                        String venue = snapshot.child("Venue").getValue(String.class);
                        String date = snapshot.child("data").getValue(String.class);
                        String fileName = snapshot.child("FileName").getValue(String.class);
                        // Create a Memory object
                       Memory memory = new Memory(memoryId, venue, date, Uri.parse(imageUri), fileName);
                       // Toast.makeText(context, "" + venue, Toast.LENGTH_SHORT).show();
                       memories.add(memory);

                    }
                    // Notify the adapter that the data has changed
                    adapter.notifyDataSetChanged();
                    // Toast.makeText(context, "Fetch Successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Something Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}