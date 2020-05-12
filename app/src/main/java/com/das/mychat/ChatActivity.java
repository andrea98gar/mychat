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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
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
    String userChat, nameChat;
    ArrayList<String> messages = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    // Bot
    AIService aiService;
    Button btVoice;
    TextToSpeech mTextToSpeech;

    EditText chatEditText;

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
        userChat = info_chatUser.substring(info_chatUser.indexOf("(") + 1, info_chatUser.indexOf(")"));
        nameChat = info_chatUser.substring(0, info_chatUser.indexOf("(") - 1);
        setTitle("Chat with " + nameChat);

        //Mensajes
        ListView chatListView = (ListView) findViewById(R.id.chatListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);
        chatListView.setAdapter(arrayAdapter);
        try {
            getMessages();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final AIConfiguration configuration =
                new AIConfiguration("f25a4bdddd2e42afa5b3c7727dbc6104",
                        AIConfiguration.SupportedLanguages.Spanish,
                        AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, configuration);
        aiService.setListener(this);

        btVoice = findViewById(R.id.btVoice);
        if(userChat.equals("bot")){
            btVoice.setVisibility(View.VISIBLE);

        }else{
            btVoice.setVisibility(View.INVISIBLE);
        }

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });

        chatEditText = findViewById(R.id.chatEditText);
    }

    public void sendChat(View v) throws ParseException {
        String input = chatEditText.getText().toString();
        if (input.isEmpty()) {
            Toast.makeText(ChatActivity.this, R.string.error_message_empty, Toast.LENGTH_SHORT).show();

        } else {
            //Fecha
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            //Parámetros que se pasan a conexion.php
            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("action", "sendMessage");
            parametrosJSON.put("currentUser", currentUser);
            parametrosJSON.put("chatUser", userChat);
            parametrosJSON.put("message", chatEditText.getText().toString());
            parametrosJSON.put("time", timeStamp);

            String result = DBUtilities.getInstance().postDB(this, parametrosJSON);

            //Se comprueba si ha habido algún error
            if (result.contains("Ha habido algún error")) {//El usuario ya existe
                Toast.makeText(ChatActivity.this, R.string.error_bd, Toast.LENGTH_SHORT).show();
            } else {//Si el registro ha sido correcto se abre la actividad del login (MainActivity)
                Toast.makeText(ChatActivity.this, R.string.success_message_send, Toast.LENGTH_SHORT).show();
                EditText sendEditText = (EditText) findViewById(R.id.chatEditText);
                sendEditText.setText("");
                getMessages();
                arrayAdapter.notifyDataSetChanged();
            }

            if(userChat.equals("bot")){
                // hablar con bot
                getResponseBot(input);
            }
        }
    }

    private void getResponseBot(String input) {
        String urlAssistant = "https://api.eu-gb.assistant.watson.cloud.ibm.com/instances/a01f1085-8568-4032-af58-e48a2b08b0d0/v1/workspaces/c2bcf23e-af1c-40d1-a2d5-32abe694db8c/message?version=2020-04-01";
        String auth = "YXBpa2V5Omp5NHpEeGtOaE00WEhJcEV5NzVaQzB4dlFIQXc3cG5kZVJlRExBbGxDdl9T";

        // mensaje Json
        org.json.JSONObject inputJsonObject = new org.json.JSONObject();
        try {
            inputJsonObject.put("text", input);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("input", inputJsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // llamada http
        AndroidNetworking.post(urlAssistant)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "Basic " + auth)
                .addJSONObjectBody(jsonBody)
                .setPriority(Priority.HIGH)
                .setTag(getString(R.string.app_name))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(org.json.JSONObject response) {


                        // parseo la respuesta del json
                        try {
                            String outputJsonObject = response.getJSONObject("output").getJSONArray("text").getString(0);
                            //Fecha
                            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                            //Parámetros que se pasan a conexion.php
                            JSONObject parametrosJSON = new JSONObject();
                            parametrosJSON.put("action", "sendMessage");
                            parametrosJSON.put("currentUser", "bot");
                            parametrosJSON.put("chatUser", currentUser);
                            parametrosJSON.put("message", outputJsonObject);
                            parametrosJSON.put("time", timeStamp);

                            String result = DBUtilities.getInstance().postDB(getApplicationContext(), parametrosJSON);

                            //Se comprueba si ha habido algún error
                            if (result.contains("Ha habido algún error")) {//El usuario ya existe
                                Toast.makeText(ChatActivity.this, R.string.error_message_bot, Toast.LENGTH_SHORT).show();
                            } else {//Si el registro ha sido correcto se abre la actividad del login (MainActivity)
                                EditText sendEditText = (EditText) findViewById(R.id.chatEditText);
                                sendEditText.setText("");
                                getMessages();
                                arrayAdapter.notifyDataSetChanged();
                            }

                            Log.i("MY-APP", "BOT: " + outputJsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void getMessages() throws ParseException {
        //Parámetros que se pasan a conexion.php
        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("action", "getMessages");
        parametrosJSON.put("currentUser", currentUser);
        parametrosJSON.put("chatUser", userChat);

        String result = DBUtilities.getInstance().postDB(this, parametrosJSON);

        //Se comprueba si ha habido algún error
        if (result.contains("Ha habido algún error")) {//No se ha podido recuperar la lista de usuarios
            Toast.makeText(ChatActivity.this, R.string.error_bd, Toast.LENGTH_SHORT).show();
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
                    if (remitente.equals(userChat)) {
                        messages.add("> " + message);
                    } else {
                        messages.add(message);
                    }
                }
                arrayAdapter.notifyDataSetChanged();
            }
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
