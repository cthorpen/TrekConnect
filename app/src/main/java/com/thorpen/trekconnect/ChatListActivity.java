package com.thorpen.trekconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    static CustomAdapter adapter;
    Button sendButton;

    // firebase fields
    FirebaseDatabase mFirebaseDatabase;
    // we are going to add an object called messages
    DatabaseReference mMessagesDatabaseReference;
    ChildEventListener mMessagesChildEventListener;

    String userName = "Anonymous";

    public static List<ChatMessage> chatMessageList = new ArrayList<>();

    String TAG = "ChatListActivityTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

//        chatMessageList = new ArrayList<>();

        Intent intent = getIntent();

        if (intent != null) {
            userName = intent.getStringExtra("userName");
        }


        // handle button click actions
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "sending message...");
                EditText editText = (EditText) findViewById(R.id.editText);
                String currText = editText.getText().toString();

                if (currText.isEmpty()) {
                    Toast.makeText(ChatListActivity.this,
                            "enter a message before sending", Toast.LENGTH_SHORT).show();
                } else {
                    ChatMessage chatMessage = new ChatMessage(userName, currText);
                    mMessagesDatabaseReference.push().setValue(chatMessage);
                    // warmup task #1
                    editText.setText("");
                }

            }

        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatListActivity.this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);

        setupFirebase();
    }

    private void setupFirebase() {
        // initialize the firebase references
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase =
                FirebaseDatabase.getInstance();
        mMessagesDatabaseReference =
                mFirebaseDatabase.getReference()
                        .child("messages");
        mMessagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // called for each message already in our db
                // called for each new message add to our db
                // dataSnapshot stores the ChatMessage
                Log.d(TAG, "onChildAdded: " + s);
                ChatMessage chatMessage =
                        dataSnapshot.getValue(ChatMessage.class);
                // add it to our list and notify our adapter
                chatMessageList.add(chatMessage);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mMessagesDatabaseReference.addChildEventListener(mMessagesChildEventListener);
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

        class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView text1;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
            }

            public void updateView(ChatMessage c) {
                text1.setText(c.toString());
            }
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ChatListActivity.this)
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
            holder.updateView(chatMessageList.get(position));
        }

        @Override
        public int getItemCount() {
            return chatMessageList.size();
        }
    }


    /* notifications! */



}