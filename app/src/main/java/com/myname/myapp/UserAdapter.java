package com.myname.myapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private DatabaseReference userRef;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.userRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.username.setText(user.getName());
        holder.email.setText(user.getEmail());
        holder.role.setText(user.getRole());

        // Handle role change logic
        holder.btnChangeRole.setOnClickListener(v -> showChangeRoleDialog(user));
        // Handle user deletion logic
        holder.btnDeleteUser.setOnClickListener(v -> deleteUser(user));
    }

    // Method to show a dialog for changing the user's role
    private void showChangeRoleDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Change Role");
        String[] roles = {"user", "admin"};
        builder.setItems(roles, (dialog, which) -> {
            String selectedRole = roles[which];
            changeUserRole(user, selectedRole);
        });
        builder.show();
    }

    // Method to change user role
    private void changeUserRole(User user, String newRole) {
        userRef.child(user.getUserId()).child("role").setValue(newRole)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Role updated successfully", Toast.LENGTH_SHORT).show();
                    user.setRole(newRole); // Update local object
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update role", Toast.LENGTH_SHORT).show());
    }

    // Method to delete a user
    private void deleteUser(User user) {
        userRef.child(user.getUserId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    userList.remove(user); // Remove from local list
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show());
    }

    // Method to ban/unban a user
//    private void banUser(User user) {
//        boolean newBanStatus = !user.isBanned(); // Toggle ban status
//        userRef.child(user.getUserId()).child("isBanned").setValue(newBanStatus)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(context, "User ban status updated", Toast.LENGTH_SHORT).show();
//                    user.setBanned(newBanStatus); // Update local object
//                    notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update ban status", Toast.LENGTH_SHORT).show());
//    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username, email, role;
        Button btnChangeRole, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvUsername);
            email = itemView.findViewById(R.id.tvEmail);
            role = itemView.findViewById(R.id.tvRole);
            btnChangeRole = itemView.findViewById(R.id.btnChangeRole);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}

