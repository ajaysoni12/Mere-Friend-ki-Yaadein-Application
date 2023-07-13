package com.example.merefriendskiyaden;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

public class MemoriesAdapter extends RecyclerView.Adapter<MemoriesAdapter.ViewHolder> {
    private static List<Memory> memories;
    private Context context;
    FirebaseDB firebaseDB = new FirebaseDB();

    public MemoriesAdapter(List<Memory> memories, Context context) {
        MemoriesAdapter.memories = memories;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Memory memory = memories.get(position);

        holder.textVenue.setText(memory.getVenue());
        holder.textDate.setText(memory.getDate());
        /* holder.imageView.setImageURI(memory.getUri());*/

        Glide.with(holder.itemView.getContext())
                .load(memory.getUri())
                .into(holder.imageView);

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteConfirmationDialog(memory);
                // Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void showDeleteConfirmationDialog(final Memory memory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Memory");
        builder.setMessage("Are you sure you want to delete this memory?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteMemory(memory);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteMemory(Memory memory) {
        // Remove memory from the RecyclerView
        int position = memories.indexOf(memory);
        if (position != -1) {
            memories.remove(position);
            notifyItemRemoved(position);
        }

        String adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();
        if (adminEmailId != null) {
            String adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);

            // Save the memory data to the Realtime Database
            DatabaseReference databaseReference = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                    .child("Memories").child(memory.getMemoryId());

            StorageReference storageRef = firebaseDB.getFirebaseStorage().getReference().child("AdminDetails").child(adminEncodedEmailId)
                    .child("Memories").child(memory.getFileName());

            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    databaseReference.removeValue();
                    Toast.makeText(context, "Memory deleted", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed to delete file
                    Toast.makeText(context, "Failed to delete memory", Toast.LENGTH_SHORT).show();
                }
            });



        }
    }


    @Override
    public int getItemCount() {
        return memories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textVenue;
        public TextView textDate;
        public ImageView imageView;
        public LinearLayout llView;

        public ViewHolder(View itemView) {
            super(itemView);
            textVenue = itemView.findViewById(R.id.textVenue);
            textDate = itemView.findViewById(R.id.textDate);
            imageView = itemView.findViewById(R.id.imageView);
            llView = itemView.findViewById(R.id.llView);

            // Set an OnClickListener on the imageView
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Memory memory = memories.get(position);
                        Uri imageUri = memory.getUri();

                        // Launch the ZoomImageActivity with the clicked image URL
                        Intent intent = new Intent(v.getContext(), ZoomImageActivity.class);
                        intent.putExtra(ZoomImageActivity.EXTRA_IMAGE_URL, imageUri.toString());
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }
    }
}
