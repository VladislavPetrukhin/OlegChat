package com.oleg.olegchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FindUsersActivity extends AppCompatActivity {

    EditText searchUserEditText;
    ImageButton searchUserImageButton;
    TextView foundedTextView;
    private ArrayList<User> userArrayList;
    private String username;
    private String userId;
    private String searchUser;
    private String searchUserId;
    private String TAG = "FindUsersActivityLogs";

    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        Intent intent = getIntent();
        username = intent.getStringExtra("userName");
        userId = intent.getStringExtra("userId");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        searchUserEditText = findViewById(R.id.searchUserEditText);
        searchUserImageButton = findViewById(R.id.searchUserImageButton);
        foundedTextView = findViewById(R.id.foundedTextView);

        searchUserEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foundedTextView.setVisibility(View.GONE);
            }
        });

        searchUserEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(28)});

        searchUserImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUser = searchUserEditText.getText().toString().trim();
                Log.d(TAG,"searchUser: " + searchUser);
                for (User user : userArrayList){
                    Log.d(TAG,"user: " + user.getName() + " " + user.getId());
                    if(searchUser.equals(user.getId()) || searchUser.equals(user.getName())){
                        foundedTextView.setVisibility(View.VISIBLE);
                        searchUser = user.getName();
                        searchUserId = user.getId();
                        foundedTextView.setText("Founded " + searchUser + "\n with id: " + searchUserId);
                        break;
                    }else{
                        foundedTextView.setVisibility(View.VISIBLE);
                        foundedTextView.setText("Not founded");
                    }
                }
            }
        });

        foundedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserContact(searchUserId);
            }
        });



    }

    private void addUserContact(String addUserId){
        try {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(userId);
            userRef.child("contacts").child(addUserId).setValue(addUserId);

          //  UserListActivity.userArrayList.add();

            Toast.makeText(FindUsersActivity.this, "The user added to your contacts",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(FindUsersActivity.this, "The user wasn't added to your contacts",
                    Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

    }

    private void attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if(usersChildEventListener == null){
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if(!user.getId().equals(userId)){

                        userArrayList.add(user);
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
}