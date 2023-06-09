package com.oleg.olegchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;


public class UserListActivity extends AppCompatActivity {


    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;

    private FirebaseAuth auth;
    private User currentUser;
    private String username = "Default User";
    private ArrayList<User> userArrayList;
    private RecyclerView userRecyclerView;
    public static UserAdapter userAdapter;
    private RecyclerView.LayoutManager userLayoutManager;

    public static ArrayList<Integer> unreadDialogsPosition = new ArrayList<>();

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        setTitle("My Contacts");
        auth = FirebaseAuth.getInstance();
        userArrayList = new ArrayList<>();

        sharedPreferences = this.getSharedPreferences("lastMessages", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        try {
            username = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        //   Log.d("currentUserLog",SignInActivity.currentUser.getId());

        attachUserDatabaseReferenceListener();
        checkUnreadMessages();
        buildRecyclerView();
    }

    private void attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if(usersChildEventListener == null){
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if(!user.getId().equals(auth.getCurrentUser().getUid())){
                        //user.setAvatarMockUpResource(R.drawable.baseline_photo_camera_50);
                        userArrayList.add(user);
//                        account.setName(user.getName());
//                        account.setPhotoUrl(user.getPhotoUrl());
//                        account.setId(user.getId());
                        userAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };

            usersDatabaseReference.addChildEventListener(usersChildEventListener);

        }
    }

    private void buildRecyclerView() {
        userRecyclerView = findViewById(R.id.userListRecycleView);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.addItemDecoration(new DividerItemDecoration(userRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        userLayoutManager = new LinearLayoutManager(this);
        userAdapter = new UserAdapter(userArrayList);

        userRecyclerView.setLayoutManager(userLayoutManager);
        userRecyclerView.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToChat(position);
            }
        });
    }

    private void goToChat(int position){
        Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
        intent.putExtra("recipientUserId",userArrayList.get(position).getId());
        intent.putExtra("recipientUserName",userArrayList.get(position).getName());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userlistactivity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.accountButton:
                startActivity(new Intent(UserListActivity.this, ProfileActivity.class));
                return true;
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserListActivity.this,SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkUnreadMessages(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference messagesDatabaseReference = firebaseDatabase.getReference().child("messages");


        ChildEventListener messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AwesomeMessage message = snapshot.getValue(AwesomeMessage.class);

                if(message.getRecipient().equals(auth.getCurrentUser().getUid())){

                    for (User user : userArrayList){
                        if(user.getId().equals(message.getSender())){
                            String lastMessage_id = "";
                            int position = userArrayList.indexOf(user);
                            try {
                                sharedPreferences.getString(user.getId(),lastMessage_id);
                                Log.d("lastMessage_id","get: " + lastMessage_id);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            if(!message.getMessage_id().equals(lastMessage_id)){
                                unreadDialogsPosition.add(position);
                            }else if(unreadDialogsPosition.contains(position)){
                                try {
                                    unreadDialogsPosition.remove(unreadDialogsPosition.indexOf(position));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }

                            }
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messagesDatabaseReference.addChildEventListener(messagesChildEventListener);

    }
}