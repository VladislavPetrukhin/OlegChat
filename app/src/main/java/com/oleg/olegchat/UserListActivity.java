package com.oleg.olegchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class UserListActivity extends AppCompatActivity {


    private DatabaseReference usersDatabaseReference;
    private DatabaseReference databaseReference;
    private ChildEventListener usersChildEventListener;

    private String TAG = "UserListActivityLog";

    private FirebaseAuth auth;
    private String username = "Default User";
    private ArrayList<User> userArrayList;
    public static ArrayList<User> contactArrayList;
    private RecyclerView userRecyclerView;
    public static UserAdapter userAdapter;
    private RecyclerView.LayoutManager userLayoutManager;
    private FloatingActionButton floatingActionButton;

    public static ArrayList<Integer> unreadDialogsPosition = new ArrayList<>();

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onResume() {
        super.onResume();
        attachUserContacts();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        setTitle("My Contacts");
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(UserListActivity.this, SignInActivity.class);
            startActivity(intent);
        }
        userArrayList = new ArrayList<>();
        contactArrayList = new ArrayList<>();
        //sharedPreferences = this.getSharedPreferences("lastMessages", Context.MODE_PRIVATE);
        //  editor = sharedPreferences.edit();

        floatingActionButton = findViewById(R.id.floatingActionButton);

        try {
            username = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //   Log.d("currentUserLog",SignInActivity.currentUser.getId());

        //initAttachUserContacts();

//        boolean contain;
//        for (User user : userArrayList){
//            contain = false;
//            for (String contact : contacts) {
//                if (user.getId().equals(contact)){
//                    contain = true;
//                }
//            }
//            if (!contain){
//                userArrayList.remove(user);
//            }
//        }
//        userAdapter.notifyDataSetChanged();

        // checkUnreadMessages();
        buildRecyclerView();
        attachUserDatabaseReferenceListener();


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserListActivity.this, FindUsersActivity.class);
                intent.putExtra("userName", username);
                intent.putExtra("userId", auth.getCurrentUser().getUid());
                startActivity(intent);
            }
        });
    }

    private void attachUserDatabaseReferenceListener() {
        Log.d(TAG, "attachUserDatabaseReferenceListener");
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
            userArrayList.clear();
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if (!user.getId().equals(auth.getCurrentUser().getUid())) {
                        userArrayList.add(user);
                       // userAdapter.notifyDataSetChanged();
                        attachUserContacts();
                        userAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if (!user.getId().equals(auth.getCurrentUser().getUid())) {
                        userArrayList.add(user);
                        // userAdapter.notifyDataSetChanged();
                    }
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
    private void attachUserContacts() {
        Log.d(TAG, "attachUserContacts");
        contactArrayList.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(auth.getCurrentUser().getUid())
                .child("contacts");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Получение значения ветви из снимка (DataSnapshot)
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "event");
                if (value != null) {
                    Log.d(TAG,"value not null");
                    String[] contacts = value.split(":");
                    Log.d(TAG,"contact1 "+contacts[0]);
                    for(String contact : contacts){
                        for(User user : userArrayList){
                            Log.d(TAG,"user"+user.getId());
                            Log.d(TAG,"contact"+contact);
                            if(user.getId().equals(contact)){
                                if(!contactArrayList.contains(user)){
                                    contactArrayList.add(user);
                                    userAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок при чтении данных
            }
        });
    }

    private void buildRecyclerView() {
        userRecyclerView = findViewById(R.id.userListRecycleView);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.addItemDecoration(new DividerItemDecoration(userRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        userLayoutManager = new LinearLayoutManager(this);
        userAdapter = new UserAdapter(contactArrayList);

        userRecyclerView.setLayoutManager(userLayoutManager);
        userRecyclerView.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                Log.d(TAG,"position: "+ position);
                goToChat(position);
            }
        });
    }

    private void goToChat(int position) {
        Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
        Log.d(TAG,"userPosition: "+ contactArrayList.get(position).getName());
        intent.putExtra("recipientUserId", contactArrayList.get(position).getId());
        intent.putExtra("recipientUserName", contactArrayList.get(position).getName());
        startActivity(intent);
    }
    public static void deleteUserContact(Integer position) {
       // Log.d(TAG, "deleteUserContact");
        User toDelete = contactArrayList.get(position);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        contactArrayList.remove(Integer.parseInt(position.toString()));
        userAdapter.notifyDataSetChanged();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(auth.getCurrentUser().getUid())
                .child("contacts");
        String contacts="";
        for(User user : contactArrayList){
            if(!user.getId().equals(toDelete.getId())){
                contacts = contacts + ":" + user.getId();
            }
        }
        userRef.removeValue();
        userRef.setValue(contacts);
    }
    public static void blockUserContact(Integer position){
        User toDelete = contactArrayList.get(position);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userlistactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accountButton:
                startActivity(new Intent(UserListActivity.this, ProfileActivity.class));
                return true;
            case R.id.log_out:
                showAlertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkUnreadMessages() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference messagesDatabaseReference = firebaseDatabase.getReference().child("messages");


        ChildEventListener messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);

                if (message.getRecipient().equals(auth.getCurrentUser().getUid())) {

                    for (User user : userArrayList) {
                        if (user.getId().equals(message.getSender())) {
                            String lastMessage_id = "";
                            int position = userArrayList.indexOf(user);
                            try {
                                sharedPreferences.getString(user.getId(), lastMessage_id);
                                Log.d("lastMessage_id", "get: " + lastMessage_id);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            if (!message.getMessage_id().equals(lastMessage_id)) {
                                unreadDialogsPosition.add(position);
                            } else if (unreadDialogsPosition.contains(position)) {
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
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Устанавливаем заголовок и сообщение для AlertDialog
        builder.setTitle("Log out?");

        // Добавляем кнопку "OK"
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserListActivity.this, SignInActivity.class));
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
    public static void showPopupMenu(View view,Integer position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.contact_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteContactButton:
                        showDeleteAlertDialog(view.getContext(),position);
                        return true;
                    case R.id.blockContactButton:
                        showBlockAlertDialog(view.getContext(),position);
                        return true;
                    case R.id.markAsUnreadContactButton:

                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }
    public static void showDeleteAlertDialog(Context context,Integer position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Устанавливаем заголовок и сообщение для AlertDialog
        builder.setTitle("Delete contact?");
                // Добавляем кнопку "OK"
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserContact(position);
                dialog.dismiss(); // Закрываем диалоговое окно после нажатия кнопки "OK"
                Toast.makeText(context, "Contact deleted",
                        Toast.LENGTH_SHORT).show();
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
    public static void showBlockAlertDialog(Context context,Integer position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Устанавливаем заголовок и сообщение для AlertDialog
        builder.setTitle("Block contact?");
        // Добавляем кнопку "OK"
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                blockUserContact(position);
                dialog.dismiss(); // Закрываем диалоговое окно после нажатия кнопки "OK"
                Toast.makeText(context, "Contact blocked",
                        Toast.LENGTH_SHORT).show();
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