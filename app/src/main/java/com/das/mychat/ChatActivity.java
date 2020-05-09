package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ChatActivity extends AppCompatActivity {
    String currentUser;
    String chatUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Usuario actual
        Intent i = getIntent();
        currentUser = i.getStringExtra("usuario");
        chatUser = i.getStringExtra("usuarioChat");

        setTitle("Chat with " + chatUser);


        Log.i("MY-APP", "ACTIVE USER: " + chatUser); //genera mensajes de tipo informacion


    }

    public void sendChat (View v){
        EditText chatEditText = (EditText) findViewById(R.id.chatEditText);

    }
}
