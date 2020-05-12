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

public class UserListActivity extends AppCompatActivity {

    ArrayList<String> users = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        setTitle("User List");

        //Usuario actual
        user = Preferences.getInstance().getUserPreferences(this);

        //Usuario seleccionado
        ListView userListView = (ListView) findViewById(R.id.userListView);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addUserToMyList(users.get(position));
            }
        });

        users.clear();
        try {
            getUserList();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, users);
        userListView.setAdapter(arrayAdapter);
    }

    /**
     * Recupera la lista de todos los usuarios registrados en la aplicación
     */
    private void getUserList() throws ParseException {
        //Parámetros que se pasan a mychat.php
        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("action", "getUserList");
        parametrosJSON.put("user", user);

        //Post en base de datos
        String result = DBUtilities.getInstance().postDB(this, parametrosJSON);

        //Se comprueba si ha habido algún error
        if(result.contains("Ha habido algún error")){//No se ha podido recuperar la lista de usuarios
            Toast.makeText(UserListActivity.this, R.string.error_bd, Toast.LENGTH_SHORT).show();
        }else{//Añadir a users todos los usuarios

            //Se guarda en un array los resultados obtenidos
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(result);
            if(array != null){
                for(int i = 0; i<array.size(); i++){
                    JSONObject json = (JSONObject) array.get(i);
                    String usuario = (String) json.get("usuario");
                    String name = (String) json.get("nombre");
                    if(!usuario.equals("bot")){
                        users.add(name+" ("+usuario+")");
                    }
                }
            }
        }
    }


    /**
     * Añade un nuevo usuario a la lista de chats del usuario actual
     * @param usuarioChat
     */
    private void addUserToMyList(String usuarioChat){
        //Parámetros que se pasan a mychat.php
        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("action", "addUser");
        parametrosJSON.put("currentUser", user);
        parametrosJSON.put("chatUser", usuarioChat.substring(usuarioChat.indexOf("(")+1, usuarioChat.indexOf(")")));

        //Post en base de datos
        String result = DBUtilities.getInstance().postDB(this, parametrosJSON);

        if(result.contains("Ha habido algún error")){
            Toast.makeText(UserListActivity.this, R.string.error_bd, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(UserListActivity.this, R.string.success_user_add, Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, MyUserListActivity.class);
            this.startActivity(i);
        }
    }
}

