package com.myname.myapp;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerViewPosts;
    PostAdapter postAdapter;
    List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerViewPosts), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();

        // Thêm dữ liệu bài đăng vào danh sách
        postList.add(new Post("User 1", "Xin chào", "https://i.imgur.com/mjhluqn.jpeg"));
        postList.add(new Post("User 2", "Hello", "https://i.imgur.com/H7YSbAz.jpeg"));
        postList.add(new Post("User 3", "Hi", "https://i.imgur.com/5877KdX.jpeg"));
        postList.add(new Post("User 4", "Hee hee", "https://i.imgur.com/Dhwbe5T.jpeg"));

        // Thêm các bài đăng khác vào danh sách
        // ...

        postAdapter = new PostAdapter(postList);
        recyclerViewPosts.setAdapter(postAdapter);
    }
}