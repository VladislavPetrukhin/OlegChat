package com.oleg.olegchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class ChatActivity extends AppCompatActivity {


    private ListView messageListView;
    private MessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton sendFilesButton, imageAttachButton, videoAttachButton, audioAttachButton, documentAttachButton,sendMessageButton;
    private EditText messageEditText;
    private LinearLayout attachLinearLayout;
    private boolean isVisibleAttachLinearLayout;
    private ScrollView scrollView;

    private String recipientUserId;
    private String recipientUserName;

    private String username = "Default User";

    private static final int RC_IMAGE_PICKER = 123;


    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference messagesDatabaseReference;
    private ChildEventListener messagesChildEventListener;
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;

    private FirebaseStorage storage;
    private StorageReference chatImagesStorageReference;

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        sharedPreferences = this.getSharedPreferences("lastMessages", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Intent intent = getIntent();
        if(intent!=null){
            recipientUserId = intent.getStringExtra("recipientUserId");
            recipientUserName = intent.getStringExtra("recipientUserName");
        }
        try {
            username = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        setTitle(recipientUserName);

        firebaseDatabase = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        messagesDatabaseReference = firebaseDatabase.getReference().child("messages");
        usersDatabaseReference = firebaseDatabase.getReference().child("users");
        chatImagesStorageReference = storage.getReference().child("chat_images");


        List<Message> messages = new ArrayList<>();
        adapter = new MessageAdapter(this, R.layout.message_item, messages);
        messageListView = findViewById(R.id.messageListView);
        messageListView.setAdapter(adapter);

        progressBar = findViewById(R.id.progressBar);
        sendFilesButton = findViewById(R.id.sendFilesButton);
        imageAttachButton = findViewById(R.id.imageAttachButton);
        videoAttachButton = findViewById(R.id.videoAttachButton);
        audioAttachButton = findViewById(R.id.audioAttachButton);
        documentAttachButton = findViewById(R.id.documentAttachButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);
        attachLinearLayout = findViewById(R.id.attachLinearLayout);

        progressBar.setVisibility(ProgressBar.INVISIBLE);



        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 0 ){
                    sendMessageButton.setEnabled(true);
                }else{
                    sendMessageButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        messageEditText.setFilters(new InputFilter[]
                {new InputFilter.LengthFilter(1000)});

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Message message = new Message();
                message.setMessageType("text");
                message.setMessage_id(UUID.randomUUID().toString());
                message.setText(messageEditText.getText().toString());
                message.setName(username);
                message.setSender(auth.getCurrentUser().getUid());
                message.setRecipient(recipientUserId);
                message.setUrl(null);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    ZonedDateTime zonedDateTime = getTime();
                    message.setDate(zonedDateTime.toString());
                }else{
                    message.setDate(null);
                }
                messagesDatabaseReference.push().setValue(message);

                messageEditText.setText("");
            }
        });

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isVisibleAttachLinearLayout) {
                    attachLinearLayout.animate().alpha(0).setDuration(1000);
                    sendFilesButton.setBackground(getResources().getDrawable(R.drawable.baseline_attach_file_24));
                }else{
                    attachLinearLayout.animate().alpha(1).setDuration(1000);
                    sendFilesButton.setBackground(getResources().getDrawable(R.drawable.baseline_close_24));
                }
                isVisibleAttachLinearLayout = !isVisibleAttachLinearLayout;
            }
        });
        imageAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");
//                String[] mimeTypes = {"image/*","video/*"};
//                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
            //    startActivityForResult(Intent.createChooser(intent,"Choose an image/video"),
                 //       RC_IMAGE_PICKER);
                startActivityForResult(Intent.createChooser(intent,"Choose an image"),
                        RC_IMAGE_PICKER);
                attachLinearLayout.animate().alpha(0).setDuration(1000);
                sendFilesButton.setBackground(getResources().getDrawable(R.drawable.baseline_attach_file_24));
                isVisibleAttachLinearLayout = false;
            }
        });
        videoAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Choose an video"),
                        RC_IMAGE_PICKER);
                attachLinearLayout.animate().alpha(0).setDuration(1000);
                sendFilesButton.setBackground(getResources().getDrawable(R.drawable.baseline_attach_file_24));
                isVisibleAttachLinearLayout = false;
            }
        });
        audioAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Choose an audio"),
                        RC_IMAGE_PICKER);
                attachLinearLayout.animate().alpha(0).setDuration(1000);
                sendFilesButton.setBackground(getResources().getDrawable(R.drawable.baseline_attach_file_24));
                isVisibleAttachLinearLayout = false;
            }
        });
        documentAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimeTypes = {"application/pdf","application/msword",
                        "application/vnd.ms-excel","application/vnd.ms-powerpoint",
                        "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.spreadsheet",
                        "application/vnd.oasis.opendocument.presentation"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Choose an document"),
                        RC_IMAGE_PICKER);
                attachLinearLayout.animate().alpha(0).setDuration(1000);
                sendFilesButton.setBackground(getResources().getDrawable(R.drawable.baseline_attach_file_24));
                isVisibleAttachLinearLayout = false;
            }
        });

        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);

                if((message.getSender().equals(auth.getCurrentUser().getUid())
                        && message.getRecipient().equals(recipientUserId)) ||
                        (message.getRecipient().equals(auth.getCurrentUser().getUid())
                                && message.getSender().equals(recipientUserId))) {
                    if(message.getSender().equals(auth.getCurrentUser().getUid())
                            && message.getRecipient().equals(recipientUserId)){
                        message.setMine(true);
                    }else{
                        message.setMine(false);
                    }
                    adapter.add(message);
                    editor.remove(message.getSender());
                    editor.putString(message.getSender(),message.getMessage_id());
                    editor.apply();



                    for (User user : UserAdapter.users){
                       if(user.getId().equals(message.getSender())){
                           try {
                               int to_remove = UserListActivity.unreadDialogsPosition
                                       .indexOf(UserAdapter.users.indexOf(user));
                               if(to_remove >=0) {
                                   UserListActivity.unreadDialogsPosition.
                                           remove(to_remove);
                               }
                           } catch (Exception e) {
                               throw new RuntimeException(e);
                           }

                       }
                    }
                UserListActivity.userAdapter.notifyDataSetChanged();
                messageListView.smoothScrollToPosition(adapter.getCount() - 1);
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

        usersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                User user = snapshot.getValue(User.class);
//                if(user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
//                    username = user.getName();
//                }

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

        @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                UserListActivity.userAdapter.notifyDataSetChanged();
                startActivity(new Intent(ChatActivity.this,UserListActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        if(requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            final StorageReference imageReference = chatImagesStorageReference.
                    child(selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

            uploadTask = imageReference.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Message message = new Message();
                        message.setMessageType("image");
                        message.setMessage_id(UUID.randomUUID().toString());
                        message.setUrl(downloadUri.toString());
                        message.setName(username);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recipientUserId);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            ZonedDateTime zonedDateTime = getTime();
                            message.setDate(zonedDateTime.toString());
                        }else{
                            message.setDate(null);
                        }
                        messagesDatabaseReference.push().setValue(message);
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }else{
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private ZonedDateTime getTime(){
            ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.systemDefault());
            return currentDateTime;
    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.message_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.messageCopyButton:

                        return true;
                    case R.id.messageReplyButton:

                        return true;
                    case R.id.messageDeleteButton:

                        return true;
                    case R.id.messageEditButton:

                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }
}