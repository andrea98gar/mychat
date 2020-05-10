package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Log in");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //Si el usuario ya está logueado
        if(Preferences.getInstance().getUserPreferences(this)!=null){
            Intent i = new Intent(this, MyUserListActivity.class);
            this.startActivity(i);
        }
    }

    private void sendSMS(String phone, String pin){
        //Permisos para enviar mensajes
        if(checkPermission(Manifest.permission.SEND_SMS)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, "La Clave es: " + pin, null, null);
            Toast.makeText(MainActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();

        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }



    }

    private boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Al pulsar el botón LOGIN, se comprueban que los campos introducidos (usuario y contraseña)
     * son correctos con respecto a la bd.
     * @param v
     */
    public void login(View v){
        //Obtener los campos introducidos por el usuario
        EditText i_phone = (EditText) findViewById(R.id.phone);
        String phone = i_phone.getText().toString();
        EditText i_name = (EditText) findViewById(R.id.name);
        String name = i_name.getText().toString();

        //Generar clave
        Random pinGenerator = new Random();
        String pin = String.valueOf(pinGenerator.nextInt(999999-0+1));
        Log.i("MY-APP", "PIN: " + pin); //genera mensajes de tipo informacion

        //Comprobar que el usuario ha introducido todos los campos
        if(name.isEmpty() || phone.isEmpty()){
            Toast.makeText(MainActivity.this, "Empty fields", Toast.LENGTH_SHORT).show();
        }else{
            //Conexion con el servidor
            HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/mychat.php");

            try {
                //Parámetros que se pasan a conexion.php
                JSONObject parametrosJSON = new JSONObject();
                parametrosJSON.put("action", "login");
                parametrosJSON.put("phone", phone);
                parametrosJSON.put("name", name);
                parametrosJSON.put("pin", pin);

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
                    Log.i("MY-APP", "LOGIN DATA: " + result); //genera mensajes de tipo informacion



                    //Se comprueba si se ha ocurrido algún error
                    if(result.contains("Ha habido algún error")){//El usuario ya existe
                        Toast.makeText(MainActivity.this, "Login incorrect, please try again", Toast.LENGTH_SHORT).show();
                    }else{//Si el login ha sido correcto entonces se abrirá la actividad PicActivity
                        //Guardar el usuario en las preferencias de la aplicación
                        Preferences.getInstance().setUserPreferences(phone, this);

                        //Enviar SMS
                        sendSMS(phone, pin);

                        //Comprobar clave usuario
                        Intent i = new Intent(this, SMSActivity.class);
                        this.startActivity(i);
                    }
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
