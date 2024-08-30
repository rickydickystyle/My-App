package com.myname.myapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private FirebaseUser currentUser;
    private DatabaseReference postsRef;
    private Context context;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.postsRef = FirebaseDatabase.getInstance().getReference("posts");
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvPostContent.setText(post.getContent());

        // Sử dụng Glide để tải ảnh từ URL
        Glide.with(holder.itemView.getContext())
                .load(post.getImageUrl())
                .placeholder(R.drawable.bg_button)  // Ảnh hiển thị khi tải
                .error(R.drawable.bg_button)  // Ảnh hiển thị khi lỗi
                .into(holder.imgPostImage);


        // Hiển thị số lượng like
        holder.tvLikeCount.setText(post.getLikeCount() + " Likes");

        // Kiểm tra trạng thái like
        if (post.getLikes().containsKey(currentUser.getUid())) {
            holder.imgLike.setImageResource(R.drawable.ic_like_filled);  // Biểu tượng like khi đã like
        } else {
            holder.imgLike.setImageResource(R.drawable.ic_like_outline);  // Biểu tượng like khi chưa like
        }

        // Xử lý sự kiện khi nhấn vào nút like
        holder.imgLike.setOnClickListener(v -> {
            handleLike(post, holder.tvLikeCount, holder.imgLike);
        });

        // Hiển thị danh sách bình luận
        List<Comment> comments = post.getComments();
        holder.recyclerViewComments.setVisibility(View.GONE);
        if (comments != null && !comments.isEmpty()) {
            holder.layoutToggleComments.setVisibility(View.VISIBLE);
            CommentAdapter commentAdapter = new CommentAdapter(context, comments);
            holder.recyclerViewComments.setAdapter(commentAdapter);
            holder.recyclerViewComments.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.layoutToggleComments.setOnClickListener(v -> {
                if (holder.recyclerViewComments.getVisibility() == View.VISIBLE) {
                    holder.recyclerViewComments.setVisibility(View.GONE);
                    holder.imgToggleIcon.setImageResource(R.drawable.ic_expand_more);
                    holder.tvToggleComments.setText("Show Comments");
                } else {
                    holder.recyclerViewComments.setVisibility(View.VISIBLE);
                    holder.imgToggleIcon.setImageResource(R.drawable.ic_expand_less);
                    holder.tvToggleComments.setText("Hide Comments");
                }
            });
        } else {
            holder.layoutToggleComments.setVisibility(View.GONE);
        }

        // Xử lý sự kiện khi nhấn vào nút comment
        holder.btnComment.setOnClickListener(v -> {
            openCommentDialog(post);
        });

        // Lấy thông tin user từ Firebase Realtime Database dựa vào userId của post
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(post.getUserId());
        userRef.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userEmail = snapshot.getValue(String.class);
                    if (userEmail != null) {
                        String userName = userEmail.split("@")[0];
                        holder.tvUserName.setText(userName);
                    }
                } else {
                    holder.tvUserName.setText("user name");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.tvUserName.setText("user name");
            }
        });

        // Hiển thị thời gian bài đăng
        String timeAgo = getTimeAgo(post.getTimestamp());
        holder.tvPostTime.setText(timeAgo);


    }
    // Hàm chuyển đổi timestamp thành định dạng thời gian dễ đọc (ví dụ: "5 minutes ago")
    private String getTimeAgo(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }

    private void handleLike(Post post, TextView tvLikeCount, ImageView imgLike) {
        if (post.getLikes() == null) {
            post.setLikes(new HashMap<>()); // Khởi tạo nếu null
        }
        DatabaseReference postRef = postsRef.child(post.getPostId());
        boolean isLiked = post.getLikes().containsKey(currentUser.getUid());

        if (isLiked) {
            // Nếu đã like, thì bỏ like
            postRef.child("likes").child(currentUser.getUid()).removeValue();
            postRef.child("likeCount").setValue(post.getLikeCount() - 1);

            // Cập nhật giao diện
            post.setLikeCount(post.getLikeCount() - 1);
            post.getLikes().remove(currentUser.getUid());
            imgLike.setImageResource(R.drawable.ic_like_outline);
        } else {
            // Nếu chưa like, thì thêm like
            postRef.child("likes").child(currentUser.getUid()).setValue(true);
            postRef.child("likeCount").setValue(post.getLikeCount() + 1);

            // Cập nhật giao diện
            post.setLikeCount(post.getLikeCount() + 1);
            post.getLikes().put(currentUser.getUid(), true);
            imgLike.setImageResource(R.drawable.ic_like_filled);
        }

        // Cập nhật số lượng like hiển thị
        tvLikeCount.setText(post.getLikeCount() + " Likes");
    }

    private void openCommentDialog(Post post) {
        // Tạo View từ layout dialog_add_comment.xml
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_comment, null);

        // Tìm EditText trong dialog_add_comment.xml
        EditText etCommentContent = dialogView.findViewById(R.id.etCommentContent);

        // Sử dụng MaterialAlertDialogBuilder để hiển thị Dialog
        new MaterialAlertDialogBuilder(context)
                .setTitle("Add Comment")
                .setView(dialogView)  // Đặt View của Dialog là dialogView
                .setPositiveButton("Post", (dialog, which) -> {
                    String commentContent = etCommentContent.getText().toString().trim();
                    if (!commentContent.isEmpty()) {
                        addComment(post, commentContent);
                    } else {
                        Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addComment(Post post, String commentContent) {
        DatabaseReference commentsRef = postsRef.child(post.getPostId()).child("comments");
        String commentId = commentsRef.push().getKey();  // Tạo một comment ID duy nhất

        if (commentId != null) {
            // Lấy username từ Firebase dựa trên userId
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("name").getValue(String.class);

                    // Lưu bình luận với username
                    Comment comment = new Comment(commentId, currentUser.getUid(), commentContent, System.currentTimeMillis());
                    commentsRef.child(commentId).setValue(comment)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Comment added!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to add comment.", Toast.LENGTH_SHORT).show();
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to fetch username.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "Failed to generate comment ID.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvPostContent, tvLikeCount, tvUserName, tvPostTime, tvToggleComments;
        ImageView imgPostImage, imgLike, imgToggleIcon;
        Button btnComment;
        RecyclerView recyclerViewComments;
        LinearLayout layoutToggleComments;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            imgPostImage = itemView.findViewById(R.id.imgPostImage);
            imgLike = itemView.findViewById(R.id.imgLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            btnComment = itemView.findViewById(R.id.btnComment);
            recyclerViewComments = itemView.findViewById(R.id.recyclerViewComments);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            layoutToggleComments = itemView.findViewById(R.id.layoutToggleComments);
            tvToggleComments = itemView.findViewById(R.id.tvToggleComments);
            imgToggleIcon = itemView.findViewById(R.id.imgToggleIcon);
        }
    }
}

