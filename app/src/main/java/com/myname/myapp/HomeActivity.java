package com.myname.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerViewPosts;
    FirebaseAuth auth;
    FirebaseUser user;
    PostAdapter postAdapter;
    List<Post> postList;
    Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawerLayout;


    @Override
    public void onStart() {
        super.onStart();
        // Kiểm tra nếu user đã đăng nhập
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            return;
        }
        // Chưa đăng nhập thì chuyển ra màn hình đăng nhập
        else {
            Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        FirebaseDatabase.getInstance().goOnline();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        // Inflate nội dung của HomeActivity vào FrameLayout
        getLayoutInflater().inflate(R.layout.activity_home, findViewById(R.id.contentFrame), true);

        if (user == null) {
            Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        //Sửa từ đây trở xuống nha sinh
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);

        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        postsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Post> newPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = new Post();

                    // Lấy các giá trị đơn giản
                    post.setPostId(postSnapshot.child("postId").getValue(String.class));
                    post.setContent(postSnapshot.child("content").getValue(String.class));
                    post.setImageUrl(postSnapshot.child("imageUrl").getValue(String.class));
                    post.setUserId(postSnapshot.child("userId").getValue(String.class));

                    // Xử lý likeCount và timestamp, sử dụng giá trị mặc định nếu null
                    Integer likeCount = postSnapshot.child("likeCount").getValue(Integer.class);
                    post.setLikeCount(likeCount != null ? likeCount : 0);

                    Long timestamp = postSnapshot.child("timestamp").getValue(Long.class);
                    post.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());

                    // Xử lý trường likes nếu nó tồn tại
                    if (postSnapshot.hasChild("likes")) {
                        GenericTypeIndicator<Map<String, Boolean>> likesIndicator = new GenericTypeIndicator<Map<String, Boolean>>() {};
                        Map<String, Boolean> likesMap = postSnapshot.child("likes").getValue(likesIndicator);
                        post.setLikes(likesMap != null ? new HashMap<>(likesMap) : new HashMap<>());
                    } else {
                        post.setLikes(new HashMap<>());
                    }

                    // Xử lý trường comments nếu nó tồn tại
                    if (postSnapshot.hasChild("comments")) {
                        GenericTypeIndicator<Map<String, Comment>> commentsIndicator = new GenericTypeIndicator<Map<String, Comment>>() {};
                        Map<String, Comment> commentsMap = postSnapshot.child("comments").getValue(commentsIndicator);
                        post.setComments(commentsMap != null ? new ArrayList<>(commentsMap.values()) : new ArrayList<>());
                    } else {
                        post.setComments(new ArrayList<>());
                    }

                    newPosts.add(post);
                }
                Collections.reverse(newPosts);
                updatePosts(newPosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load posts.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePosts(List<Post> posts) {
        postList.clear();
        postList.addAll(posts);
        postAdapter.notifyDataSetChanged();
    }

    //Đừng đụng cái này nha sinh
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            int itemId = item.getItemId();
            if (itemId == R.id.my_profile) {
                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            } else if (itemId == R.id.logout) {
                auth.signOut();
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
            } else if (itemId == R.id.home) {
                Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(homeIntent);
                return true;
            } else if (itemId == R.id.posting) {
                Intent postingIntent = new Intent(getApplicationContext(), PostActivity.class);
                startActivity(postingIntent);
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onDestroy() {
        FirebaseDatabase.getInstance().goOffline();
        Toast.makeText(getApplicationContext(), "Disconnecting", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
