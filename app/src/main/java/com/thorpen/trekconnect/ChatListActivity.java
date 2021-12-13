package com.thorpen.trekconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class ChatListActivity extends AppCompatActivity {

    CustomAdapter adapter;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        editText = findViewById(R.id.messageEditText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //set message text
            }

            @Override
            public void afterTextChanged(Editable s) {
                //set message text
            }
        });

        RecyclerView recyclerView = findViewById(R.id.messageRecyclerView);
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);


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
//                text1 = itemView.findViewById(android.R.id.text1);
            }

            public void updateView(ChatMessage c) {
//                text1.setText(c.toString());
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
//            holder.updateView(chatMessageList.get(position));
        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

}