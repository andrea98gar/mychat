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
        getMyUserList();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, users);
        userListView.setAdapter(arrayAdapter);
    }

    private void getMyUserList(){
        HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/mychat.php");

        try {
            //Parámetros que se pasan a conexion.php
            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("action", "getMyUserList");
            parametrosJSON.put("phone", user);


            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametrosJSON.toString());
            out.close();

            int statusCode = urlConnection.getResponseCode();
            //Log.i("MY-APP", "STATUS: " + statusCode); //genera mensajes de tipo informacion

            //Si la transaccion se ha realizado
            if(statusCode == 200){
                //Se obtienen los resultados
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line, result="";
                while((line = bufferedReader.readLine()) != null){
                    result += line;
                }
                inputStream.close();
                Log.i("MY-APP", "DATA: " + result); //genera mensajes de tipo informacion


                //Se comprueba si ha habido algún error
                if(result.contains("Ha habido algún error")){//No se ha podido recuperar la lista de usuarios
                    Toast.makeText(MyUserListActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }else{//Añadir a users todos los usuarios

                    //Se guarda en un array los resultados obtenidos
                    JSONParser parser = new JSONParser();
                    JSONArray array = (JSONArray) parser.parse(result);
                    if(array != null){
                        for(int i = 0; i<array.size(); i++){
                            JSONObject json = (JSONObject) array.get(i);
                            String user = (String) json.get("tlfChat");
                            String name = (String) json.get("nombreChat");
                            users.add(name+" ("+user+")");
                        }
                    }
                }
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
