package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonElement;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;


public class ChatActivity extends AppCompatActivity implements AIListener {
    private static final int REQUEST_INTERNET = 200;


    String currentUser;
    String tlfChatUser, nameChatUser;
    ArrayList<String> messages = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    // Bot
    AIService aiService;
    Button btVoice;
    TextToSpeech mTextToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        validateOS();

        //Usuario actual
        Intent i = getIntent();
        currentUser = Preferences.getInstance().getUserPreferences(this);

        //Usuario chat
        String info_chatUser = i.getStringExtra("usuarioChat");
        tlfChatUser = info_chatUser.substring(info_chatUser.indexOf("(") + 1, info_chatUser.indexOf(")"));
        nameChatUser = info_chatUser.substring(0, info_chatUser.indexOf("(") - 1);
        setTitle("Chat with " + nameChatUser);

        //Mensajes
        ListView chatListView = (ListView) findViewById(R.id.chatListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);
        chatListView.setAdapter(arrayAdapter);
        getMessages();

        final AIConfiguration configuration =
                new AIConfiguration("f25a4bdddd2e42afa5b3c7727dbc6104",
                        AIConfiguration.SupportedLanguages.Spanish,
                        AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, configuration);
        aiService.setListener(this);

        btVoice = findViewById(R.id.btVoice);

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });
    }

    public void sendChat(View v) {
        EditText chatEditText = (EditText) findViewById(R.id.chatEditText);

        if (chatEditText.getText().toString().isEmpty()) {
            Toast.makeText(ChatActivity.this, "Message empty", Toast.LENGTH_SHORT).show();

        } else {
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
                if (statusCode == 200) {
                    //Se obtienen los resultados
                    BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String line, result = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    inputStream.close();
                    //Log.i("MY-APP", "DATA: " + result); //genera mensajes de tipo informacion

                    //Se comprueba si ha habido algún error
                    if (result.contains("Ha habido algún error")) {//El usuario ya existe
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    } else {//Si el registro ha sido correcto se abre la actividad del login (MainActivity)
                        Toast.makeText(ChatActivity.this, "Message send", Toast.LENGTH_SHORT).show();
                        EditText sendEditText = (EditText) findViewById(R.id.chatEditText);
                        sendEditText.setText("");
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

    private void getMessages() {
        HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/mychat.php");

        try {
            //Parámetros que se pasan a conexion.php
            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("action", "getMessages");
            parametrosJSON.put("currentUser", currentUser);
            parametrosJSON.put("chatUser", tlfChatUser);


            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametrosJSON.toString());
            out.close();

            int statusCode = urlConnection.getResponseCode();

            //Si la transaccion se ha realizado
            if (statusCode == 200) {
                //Se obtienen los resultados
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line, result = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                inputStream.close();


                //Se comprueba si ha habido algún error
                if (result.contains("Ha habido algún error")) {//No se ha podido recuperar la lista de usuarios
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                } else {//Añadir a users todos los usuarios

                    //Se guarda en un array los resultados obtenidos
                    JSONParser parser = new JSONParser();
                    JSONArray array = (JSONArray) parser.parse(result);
                    Log.i("MY-APP", "DATA CHAT: " + result); //genera mensajes de tipo informacion

                    if (array != null) {
                        messages.clear();
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject json = (JSONObject) array.get(i);
                            String message = (String) json.get("mensaje");
                            String remitente = (String) json.get("remitente");
                            if (remitente.equals(tlfChatUser)) {
                                messages.add("> " + message);
                            } else {
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

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();

        mTextToSpeech.speak(result.getFulfillment().getSpeech(),
                TextToSpeech.QUEUE_FLUSH, null, null);
        // mostrar resultado

//        System.out.println(result);
//        String tmp = "Query: " + result.getResolvedQuery() +
//                "\nAction: " + result.getAction();
//        Toast toast1 =
//                Toast.makeText(getApplicationContext(),
//                        tmp, Toast.LENGTH_SHORT);

//        toast1.show();

    }

    @Override
    public void onError(AIError error) {
        Toast toast1 =
                Toast.makeText(getApplicationContext(),
                        error.toString(), Toast.LENGTH_SHORT);

        toast1.show();
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    public void chatBot(View v) {
        aiService.startListening();
    }

    private void validateOS() {
        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_INTERNET);
        }
    }


}
