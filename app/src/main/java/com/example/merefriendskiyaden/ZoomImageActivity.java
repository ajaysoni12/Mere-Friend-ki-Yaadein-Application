package com.example.merefriendskiyaden;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ZoomImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        // Retrieve the image URL from the intent
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        // Find the ImageView widget
        ImageView imageView = findViewById(R.id.imageView);

        // Load the image using your preferred image loading library or method
        Glide.with(this)
                .load(imageUrl)
                .into(imageView);

        // Set an OnClickListener on the ImageView for zooming functionality
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle between zooming in and zooming out
                if (imageView.getScaleType() == ImageView.ScaleType.CENTER_INSIDE) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        });
    }

}
