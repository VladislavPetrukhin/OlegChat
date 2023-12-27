package com.oleg.olegchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    TextView profileNameEditText,profileIdTextView,logOutProfileTextView;
    CircleImageView profileImageView;
    ImageButton doneEditNameButton;

    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private StorageReference profileImagesStorageReference;
    private DatabaseReference userDatabaseReference;
    private ChildEventListener userChildEventListener;

    private String username;
    private String TAG = "ProfileActivityLog";

    private static final int RC_PROFILE_IMAGE_PICKER = 122;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("My Profile");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        auth = FirebaseAuth.getInstance();

        try {
            username = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        storage = FirebaseStorage.getInstance();
        profileImagesStorageReference = storage.getReference().child("profile_images");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseReference = firebaseDatabase.getReference().child("users").child(auth.getCurrentUser().getUid());

        profileNameEditText = findViewById(R.id.profileNameEditText);
        profileIdTextView = findViewById(R.id.profileIdTextView);
        logOutProfileTextView = findViewById(R.id.logOutProfileTextView);
        profileImageView = findViewById(R.id.profileImageView);
        doneEditNameButton = findViewById(R.id.doneEditNameButton);

        Log.d("profileuserLog",auth.getCurrentUser().getUid());

        profileIdTextView.setText(auth.getCurrentUser().getUid());

        profileNameEditText.setText(username);

        attachUserPhotoListener();

//        // Получаем ссылку на узел базы данных
//        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("user");
//
//        // Добавляем слушатель на изменения в узле
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // Получаем значение из узла
//                String photo = dataSnapshot.getValue(String.class);
//                // Обрабатываем значение
//                if (photo != null && !photo.equals("")){
//                    try {
//                        Glide.with(profileImageView.getContext())
//                                .load(photo).into(profileImageView);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Обработка ошибок чтения из базы данных
//                Log.e("photoUpdate", "Failed to read value.", error.toException());
//            }
//        });



        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Choose an image"),
                        RC_PROFILE_IMAGE_PICKER);
            }
        });
        profileIdTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", auth.getCurrentUser().getUid());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ProfileActivity.this, "ID copied",
                        Toast.LENGTH_SHORT).show();
            }
        });

        doneEditNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUsername = profileNameEditText.getText().toString();
                doneEditNameButton.setVisibility(View.GONE);
                updateUserName(newUsername);
            }
        });
        profileNameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doneEditNameButton.setVisibility(View.VISIBLE);
            }
        });

        logOutProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PROFILE_IMAGE_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            final StorageReference imageReference = profileImagesStorageReference.
                    child(selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

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
                        updateUserPhoto(downloadUri.toString());
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }
    private void updateUserPhoto(String url){
        try{
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(auth.getCurrentUser().getUid());
            userRef.child("photoUrl").setValue(url);

            Glide.with(profileImageView.getContext())
                    .load(url).into(profileImageView);
            Toast.makeText(ProfileActivity.this, "Photo update successfully",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ProfileActivity.this, "Photo update failed",
                    Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

    }
    private void updateUserName(String newUsername){
        try {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(auth.getCurrentUser().getUid());
            userRef.child("name").setValue(newUsername);
            profileNameEditText.setText(newUsername);
            Toast.makeText(ProfileActivity.this, "Username update successfully",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ProfileActivity.this, "Username update failed",
                    Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

    }


    private void attachUserPhotoListener() {
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if(userChildEventListener == null){
            userChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if(user.getId().equals(auth.getCurrentUser().getUid())){
                        Glide.with(profileImageView.getContext())
                                .load(user.getPhotoUrl()).into(profileImageView);
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

            userDatabaseReference.addChildEventListener(userChildEventListener);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profileactivity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settingsButton:
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
                return true;
            case android.R.id.home:
                startActivity(new Intent(ProfileActivity.this,UserListActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Устанавливаем заголовок и сообщение для AlertDialog
        builder.setTitle("Log out?");

        // Добавляем кнопку "OK"
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this,SignInActivity.class));
                dialog.dismiss(); // Закрываем диалоговое окно после нажатия кнопки "OK"
            }
        });

        // Добавляем кнопку "Cancel"
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Закрываем диалоговое окно после нажатия кнопки "Cancel"
            }
        });

        // Создаем и отображаем AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}