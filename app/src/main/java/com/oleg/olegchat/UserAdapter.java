package com.oleg.olegchat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public static ArrayList<User> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener{
        void onUserClick(int position);
    }
    public void setOnUserClickListener(OnUserClickListener listener){
        this.listener = listener;

    }

    public UserAdapter(ArrayList<User> users){
        this.users = users;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        UserViewHolder viewHolder = new UserViewHolder(view,listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = users.get(position);
        Log.d("UserAdapterLogs", String.valueOf(position));
        holder.userNameTextView.setText(currentUser.getName());
        Glide.with(holder.avatarImageView.getContext())
                .load(currentUser.getPhotoUrl()).into(holder.avatarImageView);
        Log.d("UserAdapterLogs",currentUser.getName());
        if (UserListActivity.unreadDialogsPosition != null && UserListActivity.unreadDialogsPosition.contains(position)){
            holder.unreadDialogNotify.setVisibility(View.VISIBLE);
        }else{
            holder.unreadDialogNotify.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView avatarImageView;
        public TextView userNameTextView;
        public ImageView unreadDialogNotify;


        public UserViewHolder(@NonNull View itemView, OnUserClickListener listener) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            unreadDialogNotify = itemView.findViewById(R.id.unreadDialogNotify);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onUserClick(position);
                        }
                    }
                }
            });
        }
    }
}
