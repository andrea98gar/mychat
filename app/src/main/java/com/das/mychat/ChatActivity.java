package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.lang.reflect.Array;
import java.net.ProtocolException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class ChatActivity extends AppCompatActivity {
    String currentUser;
    String tlfChatUser, nameChatUser;
    ArrayList<String> messages = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Usuario actual
        Intent i = getIntent();
        currentUser = Preferences.getInstance().getUserPreferences(this);

        //Usuario chat
        String info_chatUser = i.getStringExtra("usuarioChat");
        tlfChatUser = info_chatUser.substring(info_chatUser.indexOf("(")+1, info_chatUser.indexOf(")"));
        nameChatUser = info_chatUser.substring(0, info_chatUser.indexOf("(")-1);
        setTitle("Chat with " + nameChatUser);

        Log.i("MY-APP", "TLF CHAT: " + tlfChatUser); //genera mensajes de tipo informacion
        Log.i("MY-APP", "NAME CHAT: " + nameChatUser); //genera mensajes de tipo informacion


        //Mensajes
        ListView chatListView = (ListView) findViewById(R.id.chatListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);
        chatListView.setAdapter(arrayAdapter);
        getMessages();

    }

    public void sendChat (View v){
        EditText chatEditText = (EditText) findViewById(R.id.chatEditText);

        if(chatEditText.getText().toString().isEmpty()){
            Toast.makeText(ChatActivity.this, "Message empty", Toast.LENGTH_SHORT).show();

        }else{
            //Fecha
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/mychat.php");

            try {
                //Parámetros que se pasan a conexion.php
                JSONObject parametrosJSON = new JSONObject();
                parametrosJSON.put("action", "sendMessage");
                parametrosJSON.put("currentUser", currentUser);
                parametrosJSON.put("chatUser", tlfChatUser);
                parametrosJSON.put("message", chatEditText.getText().toString());
                parametrosJSON.put("time", timeStamp);


                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametrosJSON.toString());
                out.close();

                int statusCode = urlConnection.getResponseCode();

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
                    //Log.i("MY-APP", "DATA: " + result); //genera mensajes de tipo informacion

                    //Se comprueba si ha habido algún error
                    if(result.contains("Ha habido algún error")){//El usuario ya existe
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }else{//Si el registro ha sido correcto se abre la actividad del login (MainActivity)
                        Toast.makeText(ChatActivity.this, "Message send", Toast.LENGTH_SHORT).show();
                        getMessages();
                        arrayAdapter.notifyDataSetChanged();
                        /*Intent i = new Intent(this, ChatActivity.class);
                        this.startActivity(i);*/
                    }
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getMessages(){
        HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/mychat.php");

        try {
            //Parámetros que se pasan a conexion.php
            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("action", "getMessages");
            parametrosJSON.put("currentUser", "699191262");
            parametrosJSON.put("chatUser", "987654321");


            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametrosJSON.toString());
            out.close();

            int statusCode = urlConnection.getResponseCode();

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


                //Se comprueba si ha habido algún error
                if(result.contains("Ha habido algún error")){//No se ha podido recuperar la lista de usuarios
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }else{//Añadir a users todos los usuarios

                    //Se guarda en un array los resultados obtenidos
                    JSONParser parser = new JSONParser();
                    JSONArray array = (JSONArray) parser.parse(result);
                    Log.i("MY-APP", "DATA CHAT: " + result); //genera mensajes de tipo informacion

                    if(array != null){
                        messages.clear();
                        for(int i = 0; i<array.size(); i++){
                            JSONObject json = (JSONObject) array.get(i);
                            String message = (String) json.get("mensaje");
                            String remitente = (String) json.get("remitente");
                            Log.i("MY-APP", "mensaje CHAT: " + message); //genera mensajes de tipo informacion
                            Log.i("MY-APP", "remitente CHAT: " + remitente); //genera mensajes de tipo informacion

                            if(remitente.equals(tlfChatUser)){
                                messages.add("> "+message);
                            }else{
                                messages.add(message);
                            }
                        }
                        arrayAdapter.notifyDataSetChanged();

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
