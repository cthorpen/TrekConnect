package com.thorpen.trekconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView name = findViewById(R.id.nameTextView);
        TextView email = findViewById(R.id.emailTextView);

        // grab intent
        Intent intent = getIntent();
        if (intent != null) {
            name.setText(new StringBuilder().append("NAME: ").
                    append(intent.getStringExtra("name")).toString());
            email.setText(new StringBuilder().append("EMAIL: ").
                    append(intent.getStringExtra("email")).toString());
        }
    }

    // go back to main screen
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}