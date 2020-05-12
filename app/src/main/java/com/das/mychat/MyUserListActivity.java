package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MyUserListActivity extends AppCompatActivity {
    String user;
    ArrayList<String> users = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_user_list);
        setTitle("My Users");

        //Obtener usuario
        user = Preferences.getInstance().getUserPreferences(this);

        //Todos los usuarios
        FloatingActionButton addUserBtn = findViewById(R.id.addUserBtn);
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), UserListActivity.class);
                startActivity(i);
            }
        });

        ListView userListView = (ListView) findViewById(R.id.myUserListView);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("MY-APP", "USER CHAT: " + users.get(position)); //genera mensajes de tipo informacion

                Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                i.putExtra("usuarioChat", users.get(position));
                startActivity(i);
            }
        });

        users.clear();
        try {
            getMyUserList();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, users);
        userListView.setAdapter(arrayAdapter);
    }

    private void getMyUserList() throws ParseException {

        //Parámetros que se pasan a conexion.php
        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("action", "getMyUserList");
        parametrosJSON.put("user", user);

        //Resultados obtenidos
        String result = DBUtilities.getInstance().postDB(this, parametrosJSON);

        //Se comprueba si ha habido algún error
        if(result.contains("Ha habido algún error")){
            Toast.makeText(MyUserListActivity.this, R.string.error_bd, Toast.LENGTH_SHORT).show();
        }else{//Añadir a users todos los usuarios
            //Se guarda en un array los resultados obtenidos
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(result);
            if(array != null){
                for(int i = 0; i<array.size(); i++){
                    JSONObject json = (JSONObject) array.get(i);
                    String userChat = (String) json.get("usuarioChat");
                    String nameChat = (String) json.get("nombreChat");
                    users.add(nameChat+" ("+userChat+")");
                }
            }
        }
    }
}
