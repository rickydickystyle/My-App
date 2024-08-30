package com.myname.myapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference userRef, postRef;
    private RecyclerView recyclerViewUsers, recyclerViewPosts;
    private UserAdapter userAdapter;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
//        postRef = FirebaseDatabase.getInstance().getReference("Posts");

        // Initialize RecyclerViews
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
//        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);

        // Set layout managers
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
//        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));

        // Initialize lists and adapters
        userList = new ArrayList<>();
//        postList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
//        postAdapter = new PostAdapter(postList, this);

        recyclerViewUsers.setAdapter(userAdapter);
//        recyclerViewPosts.setAdapter(postAdapter);

        // Load users and posts from Firebase
        loadUsers();
//        loadPosts();
    }

    private void loadUsers() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void loadPosts() {
//        postRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                postList.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Post post = ds.getValue(Post.class);
//                    postList.add(post);
//                }
//                postAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Toast.makeText(AdminActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}
