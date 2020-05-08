package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    ArrayList<String> users = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        setTitle("User List");

        ListView userListView = (ListView) findViewById(R.id.userListView);

        users.add("Test");

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, users);

        userListView.setAdapter(arrayAdapter);


    }
}
