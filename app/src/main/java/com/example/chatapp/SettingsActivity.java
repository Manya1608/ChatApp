package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    EditText username;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        username = findViewById(R.id.username);
        saveButton = findViewById(R.id.save_button);

        //shared pref
        SharedPreferences sharedPreferences = getSharedPreferences("userPref", MODE_PRIVATE);
        String user = sharedPreferences.getString("username", "Server");
        username.setText(user);

        saveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                //shared pref
                SharedPreferences sharedPreferences = getSharedPreferences("userPref", MODE_PRIVATE);
                String user = sharedPreferences.getString("username", "Server");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //Toast.makeText(SettingsActivity.this, user + " " + username.getText().toString(), Toast.LENGTH_SHORT).show();
                if(!user.equals(username.getText().toString().trim())) {
                    editor.putString("username", username.getText().toString().trim());
                    editor.apply();
                    Toast.makeText(SettingsActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
