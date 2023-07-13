package com.example.merefriendskiyaden;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class NewMemoryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editVenue;
    private EditText editDate;
    private ImageView imageView;
    private Button btnSelectImage;
    private Button btnUpload;

    private Uri imageUri;
    FirebaseDB firebaseDB;
    ProgessDialog progessDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memory);

        firebaseDB = new FirebaseDB();
        progessDialog = new ProgessDialog(NewMemoryActivity.this);

        editVenue = findViewById(R.id.editVenue);
        editDate = findViewById(R.id.editDate);
        imageView = findViewById(R.id.imageView);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadMemory();
            }
        });

        // Set an OnClickListener on the imageView
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // int position = getAdapterPosition();
                // if (position != RecyclerView.NO_POSITION) {
                if (imageUri == null)
                    return;
                String imageUrl = String.valueOf(imageUri);

                // Launch the ZoomImageActivity with the clicked image URL
                Intent intent = new Intent(v.getContext(), ZoomImageActivity.class);
                intent.putExtra(ZoomImageActivity.EXTRA_IMAGE_URL, imageUrl);
                v.getContext().startActivity(intent);
            }
        });
    }

    private void uploadMemory() {
        // Get the entered venue and date
        String venue = editVenue.getText().toString();
        String date = editDate.getText().toString();

        if (TextUtils.isEmpty(venue) || TextUtils.isEmpty(date) || imageUri == null) {
            Toast.makeText(this, "Please enter all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        progessDialog.showLoadingDialog();

        // Upload the image and save the memory data
        // You can use the same code for uploading the image and saving the data as shown in the previous examples

        String adminEmailId = Objects.requireNonNull(firebaseDB.getFirebaseAuthInstance().getCurrentUser()).getEmail();
        if (adminEmailId != null) {
            String adminEncodedEmailId = firebaseDB.encodeEmail(adminEmailId);

            StorageReference storageRef = firebaseDB.getFirebaseStorage().getReference().child("AdminDetails").child(adminEncodedEmailId)
                    .child("Memories");

            // Generate a unique filename for the image
            String filename = "memory_" + System.currentTimeMillis() + ".jpg";
            final StorageReference imageRef = storageRef.child(filename);

            // Upload the image file to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {

                            // Save the memory data to the Realtime Database
                            DatabaseReference databaseReference = firebaseDB.getDatabaseReference().child("AdminDetails").child(adminEncodedEmailId)
                                    .child("Memories");

                            String memoryId = databaseReference.push().getKey();
                            if (memoryId != null) {
                                databaseReference = databaseReference.child(memoryId);
                                // databaseReference.setValue(memory);
                                HashMap<String, String> hs = new HashMap<>();
                                hs.put("Id", memoryId);
                                hs.put("Uri", downloadUri.toString());
                                hs.put("Venue", venue);
                                hs.put("data", date);
                                hs.put("FileName", filename);
                                databaseReference.setValue(hs);
                                progessDialog.hideLoadingDialog();
                                Toast.makeText(getApplicationContext(), "Memories Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

}