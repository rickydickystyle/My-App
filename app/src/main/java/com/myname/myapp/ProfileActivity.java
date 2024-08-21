package com.myname.myapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 101;
    ImageView avatarImageView;
    Button btnPostList, btnBack, btnFriendList;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView txtDisplayName;

    @Override
    public void onStart() {
        super.onStart();
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
        avatarImageView.setOnClickListener(v -> askCameraPermission());

        String avatarPath = loadAvatarPath();
        if (avatarPath != null) {
            Bitmap savedAvatar = BitmapFactory.decodeFile(avatarPath);
            if (savedAvatar != null) {
                avatarImageView.setImageBitmap(savedAvatar);
            }
        }

        btnPostList = findViewById(R.id.btnPostList);
        btnBack = findViewById(R.id.btnBack);
        btnFriendList = findViewById(R.id.btnFriendList);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

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

    private void askCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //noinspection deprecation
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            avatarImageView.setImageBitmap(photo);
            try {
                saveImageToStorage(photo);
                String filePath = saveImageToStorage(photo); // lưu và lấy đường dẫn file
                saveAvatarPath(filePath); // lưu đường dẫn file vào SharedPreferences
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Cho phép quyền truy cập vào máy ảnh?.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToStorage(Bitmap bitmap) throws IOException {
        File directory = new File(getFilesDir(), "avatars");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, "avatar_image.jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        }
        return file.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage(String path) {
        return BitmapFactory.decodeFile(path);
    }

    private void saveAvatarPath(String path) {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("avatar_path", path);
        editor.apply();
    }

    private String loadAvatarPath() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return preferences.getString("avatar_path", null);
    }
}