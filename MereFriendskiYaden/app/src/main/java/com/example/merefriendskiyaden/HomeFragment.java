package com.example.merefriendskiyaden;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CODE_IMAGE_PICK = 100;
    private List<Uri> selectedImages;
    private RecyclerView recyclerView;
    private Button btnSelectImage, btnUploadImage;
    private ImageAdapter imageAdapter;
    private Context context;
    FirebaseDB firebaseDB;
    private boolean isUploaded = false;
    private EditText edtGroupDescription;
    private Button btnUploadGroupDescription;

    String who = "", friendAdminEmail = "", friendEmail = "";
    ProgessDialog progessDialog;

    public HomeFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void uploadImagesToFirebase() {
        if (selectedImages.isEmpty() || !isUploaded) {
            Toast.makeText(context, "Please select images", Toast.LENGTH_SHORT).show();
            return;
        }

        progessDialog.showLoadingDialog();
        isUploaded = true;
        // deleteImagesFromFirebase();

        List<UploadTask> uploadTasks = new ArrayList<>();
        String adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();

        if (adminEmailId != null) {

            String adminEncodedEmailId;
            adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);

            deleteImagesFromFirebase(adminEncodedEmailId);

            // Upload each selected image to Firebase Storage
            Toast.makeText(context, "Image cnt" + selectedImages.size(), Toast.LENGTH_SHORT).show();
            for (Uri imageUri : selectedImages) {

                String filename = "memory_" + System.currentTimeMillis() + ".jpg";

                // Upload image to Firebase Storage
                StorageReference storageRef = firebaseDB.getFirebaseStorage().getReference();
                StorageReference imageRef = storageRef.child("AdminDetails").child(adminEncodedEmailId)
                        .child("Images").child(filename);

                UploadTask uploadTask = imageRef.putFile(imageUri);

                uploadTasks.add(uploadTask);

                // Add success and failure listeners to the upload task
                // Add progress listener to the upload task
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // Image uploaded successfully
                        // Get the download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Save the download URL to the Realtime Database
                                DatabaseReference databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                                        .child("Images");
                                String imageKey = databaseRef.push().getKey();
                                databaseRef.child(imageKey).setValue(downloadUri.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred during image upload
                        Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                        progessDialog.hideLoadingDialog();
                    }
                });
            }
        }
        // Hide the progress bar when all uploads are completed
        Tasks.whenAllComplete(uploadTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                progessDialog.hideLoadingDialog();
                Toast.makeText(context, "All images uploaded on Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImages() {
        fetchImagesFromFirebase();
    }

    public void deleteImagesFromFirebase(String adminEncodedEmailId) {

        StorageReference storageRef = firebaseDB.getFirebaseStorage().getReference();
        StorageReference imageRef = storageRef.child("AdminDetails").child(adminEncodedEmailId)
                .child("Images");
        DatabaseReference databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                .child("Images");

        // DatabaseReference databaseRef = firebaseDB.getDatabaseReference();

        // Delete images from Firebase Storage
        imageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // Delete each image from Firebase Storage
                for (StorageReference item : listResult.getItems()) {
                    item.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Image deleted successfully from Firebase Storage
                            Log.d("DeleteImagesActivity", "Image deleted from Storage");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error occurred while deleting image from Firebase Storage
                            Log.e("DeleteImagesActivity", "Error deleting image from Storage: " + e.getMessage());
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error occurred while listing images in Firebase Storage
                Log.e("DeleteImagesActivity", "Error listing images in Storage: " + e.getMessage());
            }
        });

        // Delete image references from Realtime Database
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

    private void fetchImagesFromFirebase() {

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
                    .child("Images");

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String imageUrl = snapshot.getValue(String.class);
                            Uri imageUri = Uri.parse(imageUrl);
                            selectedImages.add(imageUri);
                            imageAdapter.notifyDataSetChanged();
                        }

                        // Toast.makeText(context, "Fetch Images From Firebase Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        // Toast.makeText(context, "Admin not upload any content", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Error occurred while retrieving data
                    Toast.makeText(getContext(), "Error retrieving images", Toast.LENGTH_SHORT).show();
                }
            });

            databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                    .child("Group Description");

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String groupDisc = snapshot.getValue(String.class);
                            edtGroupDescription.setText(groupDisc);
                        }

                        // Toast.makeText(context, "Fetch Images From Firebase Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        // Toast.makeText(context, "No Image Available on Firebase!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Something Error!", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(context, "Something Error!", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveGroupInfoToFirebase(String groupInfo) {

        String adminEmailId;
        adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();

        progessDialog.showLoadingDialog();

        if (adminEmailId != null) {
            String adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);
            DatabaseReference databaseRef = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                    .child("Group Description");

            // Delete image references from Realtime Database
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

            databaseRef.child("Group Description").setValue(groupInfo);
            progessDialog.hideLoadingDialog();
            Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT).show();
        } else {
            progessDialog.hideLoadingDialog();
            Toast.makeText(context, "Something Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    isUploaded = true;
                    selectedImages.clear();
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(imageUri);
                    }
                    imageAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (getArguments() != null) {
            who = getArguments().getString("Who");
            friendAdminEmail = getArguments().getString("AdminEmail");
            friendEmail = getArguments().getString("friendEmail");
        } else {
            Toast.makeText(context, "Something Error", Toast.LENGTH_SHORT).show();
        }

        selectedImages = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        btnSelectImage = view.findViewById(R.id.btnSelectImages);
        btnUploadImage = view.findViewById(R.id.btnUploadImages);
        edtGroupDescription = view.findViewById(R.id.edtGroupDescription);
        btnUploadGroupDescription = view.findViewById(R.id.btnUploadGroupDescription);

        imageAdapter = new ImageAdapter(selectedImages, context);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        firebaseDB = new FirebaseDB();
        progessDialog = new ProgessDialog(context);

        if (who.equals("Join")) {
            btnUploadGroupDescription.setVisibility(View.GONE);
            btnSelectImage.setVisibility(View.GONE);
            btnUploadImage.setVisibility(View.GONE);
        }

        btnUploadGroupDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupInfo = edtGroupDescription.getText().toString();
                if (groupInfo.isEmpty()) {
                    Toast.makeText(context, "Please write something about group", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveGroupInfoToFirebase(groupInfo);
            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImagesToFirebase();
            }
        });

        loadImages();

        return view;
    }

}