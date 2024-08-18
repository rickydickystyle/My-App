package com.myname.myapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] spinnerOptions = {"Log out", "User Profile"};
    RecyclerView recyclerViewPosts;
    FirebaseAuth auth;
    FirebaseUser user;
    PostAdapter postAdapter;
    List<Post> postList;
//    Button btnLogout;
    TextView txtUsername;
    Spinner spinner;
    private boolean isSpinnerTouched = false;



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

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        else{
            txtUsername = findViewById(R.id.helloUserName);
            int i = user.getEmail().indexOf("@");
            txtUsername.setText("Hello, " + user.getEmail().substring(0, i));
        }

        spinner = findViewById(R.id.spinner);

        ArrayAdapter spinnerAdapter = new ArrayAdapter(getApplicationContext(),
                R.layout.empty_spinner_item, spinnerOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        int initialSelectedPosition= spinner.getSelectedItemPosition();
        spinner.setSelection(initialSelectedPosition, false);
        spinner.setOnItemSelectedListener(this);

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isSpinnerTouched = true;
                return false;
            }
        });


//        btnLogout = findViewById(R.id.btnLogout);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();

        postList.add(new Post("User 1", "Xin chào", "https://i.imgur.com/mjhluqn.jpeg"));
        postList.add(new Post("User 2", "Hello", "https://i.imgur.com/H7YSbAz.jpeg"));
        postList.add(new Post("User 3", "Hi", "https://i.imgur.com/5877KdX.jpeg"));
        postList.add(new Post("User 4", "Hee hee", "https://i.imgur.com/Dhwbe5T.jpeg"));

        postAdapter = new PostAdapter(postList);
        recyclerViewPosts.setAdapter(postAdapter);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        if (!isSpinnerTouched) return;
        switch (pos){
            case 0:
                auth.signOut();
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;
            case 1:
//                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
//                startActivity(profileIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}