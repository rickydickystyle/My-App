package com.myname.myapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private EditText etPostContent;
    private Button btnSelectImage, btnPost, btnBack;
    private ImageView imgPostImage;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        etPostContent = findViewById(R.id.etPostContent);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imgPostImage = findViewById(R.id.imgPostImage);
        btnPost = findViewById(R.id.btnPost);
        btnBack = findViewById(R.id.btnBack);

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        btnPost.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToFirebaseStorage();
            } else {
                Toast.makeText(PostActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            // Quay lại HomeActivity
            Intent intent = new Intent(PostActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void openImageChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooser = Intent.createChooser(galleryIntent, "Select or Take a Picture");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        startActivityForResult(chooser, PICK_IMAGE_REQUEST);
    }

    private void uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("images/" + System.currentTimeMillis() + ".jpg");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Log.d("PostActivity", "Image URL: " + imageUrl); // Debug URL ảnh

                        // Hiển thị ảnh đã chọn
                        Glide.with(PostActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.bg_button)
                                .error(R.drawable.bg_button)
                                .override(Target.SIZE_ORIGINAL)  // Tải ảnh với kích thước gốc
                                .into(imgPostImage);

                        // Lưu bài đăng vào cơ sở dữ liệu
                        savePostToDatabase(imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        Log.e("PostActivity", "Failed to upload image: " + e.getMessage());
                        Toast.makeText(PostActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Hiển thị ảnh đã chọn trước khi tải lên
            imgPostImage.setVisibility(View.VISIBLE);
            imgPostImage.setImageURI(imageUri);
        }
    }

    private void savePostToDatabase(String imageUrl) {
        String postContent = etPostContent.getText().toString().trim();

        if (TextUtils.isEmpty(postContent)) {
            Toast.makeText(this, "Post content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference postsRef = database.getReference("posts");

        String postId = postsRef.push().getKey();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        long timestamp = System.currentTimeMillis();

        Post newPost = new Post(postId, postContent, imageUrl, userId, timestamp);
        postsRef.child(postId).setValue(newPost)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PostActivity", "Post saved successfully");
                    Toast.makeText(PostActivity.this, "Post submitted successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("PostActivity", "Failed to save post: " + e.getMessage());
                    Toast.makeText(PostActivity.this, "Failed to post. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
