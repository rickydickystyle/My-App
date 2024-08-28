package com.myname.myapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;


public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 202;
    ImageView avatarImageView;
    Button btnPostList, btnBack, btnFriendList;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView txtDisplayName;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    public void onStart() {
        super.onStart();
        if (imageUri != null) {
            uploadImageToFirebase(imageUri);
        } else {
            loadUserAvatar();
        }
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            return;
        }
        else {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        avatarImageView = findViewById(R.id.avatar);

        avatarImageView.setOnClickListener(v -> openImageChooser());

        btnPostList = findViewById(R.id.btnPostList);
        btnBack = findViewById(R.id.btnBack);
        btnFriendList = findViewById(R.id.btnFriendList);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if (user == null) {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        else{
            txtDisplayName = findViewById(R.id.displayName);
            int i = user.getEmail().indexOf("@");
            txtDisplayName.setText(user.getEmail().substring(0, i));
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homeIntent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
        });

//        btnFriendList.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent friendListIntent = new Intent(ProfileActivity.this, FriendListActivity.class);
//                startActivity(friendListIntent);
//                finish();
//            }
//        });
//
//        btnPostList.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent postListIntent = new Intent(ProfileActivity.this, PostListActivity.class);
//                startActivity(postListIntent);
//                finish();
//            }
//        });
    }

    private void openImageChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooser = Intent.createChooser(galleryIntent, "Select or Take a Picture");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        startActivityForResult(chooser, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                imageUri = data.getData();
                // Hiển thị ảnh đã chọn trước khi tải lên
                avatarImageView.setVisibility(View.VISIBLE);
                avatarImageView.setImageURI(imageUri);

                // Sau khi hiển thị ảnh, bắt đầu tải ảnh lên Firebase
//                uploadImageToFirebase(imageUri);
            } else if (data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap) {
                // Trường hợp người dùng chọn chụp ảnh thay vì chọn ảnh từ thư viện
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                avatarImageView.setVisibility(View.VISIBLE);
                avatarImageView.setImageBitmap(bitmap);

                // Chuyển đổi Bitmap thành URI và tải lên Firebase
                imageUri = getImageUriFromBitmap(bitmap);
//                uploadImageToFirebase(imageUri);
            }
        }
    }

    // Hàm này để chuyển đổi Bitmap thành URI (trong trường hợp ảnh chụp từ camera)
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Avatar", null);
        return Uri.parse(path);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            // Lưu ảnh lên Firebase Storage
            String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storageReference.child("avatars/" + fileName);

            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Lấy URL của ảnh đã tải lên
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            // Lưu URL vào Firebase Database hoặc SharedPreferences
                            saveAvatarUrlToSharedPreferences(imageUrl);

                            // Cập nhật ảnh đại diện
                            Glide.with(ProfileActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_3)
                                    .into(avatarImageView);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Upload failed: " + e.getMessage());
                    });
        }
    }


//    private void uploadImageToFirebase(Uri imageUri) {
//        String fileName = "avatar_" + System.currentTimeMillis() + ".*";
//
//        StorageReference ref = storageReference.child("avatars/" + fileName);
//
//        ref.putFile(imageUri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        // Lấy URL của ảnh đã tải lên
//                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                String imageUrl = uri.toString();
//                                // Lưu URL vào Firebase Database hoặc sử dụng theo cách khác
//                                Log.d("Firebase", "Upload success: " + imageUrl);
//                            }
//                        });
//                        saveImageUrlToDatabase(imageUri.toString());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("Firebase", "Upload failed: " + e.getMessage());
//                    }
//                });
//    }

    private void saveImageUrlToDatabase(String imageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users")
                .child(Objects.requireNonNull(user.getUid())).child("avatar");
        databaseRef.setValue(imageUrl)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Avatar URL saved to database"))
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to save URL: " + e.getMessage()));
    }

    private void saveAvatarUrlToSharedPreferences(String imageUrl) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                .putString("avatar_url", imageUrl).apply();
    }

    private String getAvatarUrlFromSharedPreferences() {
        return getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("avatar_url", null);
    }

    private void loadUserAvatar() {
        String avatarUrl = getAvatarUrlFromSharedPreferences();
        if (avatarUrl != null) {
            Glide.with(ProfileActivity.this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_3)
                    .into(avatarImageView);
        } else {
            // Nếu không có URL trong SharedPreferences, tải từ Firebase
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(Objects.requireNonNull(user.getUid())).child("avatar");

            databaseRef.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String url = dataSnapshot.getValue(String.class);
                    if (!TextUtils.isEmpty(url)) {
                        saveAvatarUrlToSharedPreferences(url);
                        Glide.with(ProfileActivity.this)
                                .load(url)
                                .placeholder(R.drawable.ic_3)
                                .into(avatarImageView);
                    }
                }
            }).addOnFailureListener(e -> Log.e("Firebase", "Failed to load avatar: " + e.getMessage()));
        }
    }

}