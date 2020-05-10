package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class SMSActivity extends AppCompatActivity {
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        //Obtener telefono usuario
        user = Preferences.getInstance().getUserPreferences(this);
        Log.i("MY-APP", "usuario: " + user); //genera mensajes de tipo informacion

    }

    public void checkPass(View v){
        //Obtener los campos introducidos por el usuario
        EditText i_pass = (EditText) findViewById(R.id.pass);
        String pass = i_pass.getText().toString();

        //Comprobar que el usuario ha introducido todos los campos
        if(pass.isEmpty()){
            Toast.makeText(SMSActivity.this, "Empty fields", Toast.LENGTH_SHORT).show();
        }else{
            //Conexion con el servidor
            HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/mychat.php");

            try {
                //Parámetros que se pasan a conexion.php
                JSONObject parametrosJSON = new JSONObject();
                parametrosJSON.put("action", "checkPass");
                parametrosJSON.put("phone", user);

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
                        Toast.makeText(SMSActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }else{//Si el login ha sido correcto entonces se abrirá la actividad PicActivity
                        JSONParser parser = new JSONParser();
                        JSONObject json = (JSONObject) parser.parse(result);
                        if(json != null){
                            String clave = (String) json.get("clave");
                            if(clave.equals(pass)){
                                //Guardar el usuario en las preferencias de la aplicación
                                Preferences.getInstance().setCheckUserPreference(this, "done");
                                Intent i = new Intent(this, MyUserListActivity.class);
                                this.startActivity(i);
                            }else{
                                Toast.makeText(SMSActivity.this, "Incorrect", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(this, MainActivity.class);
                                this.startActivity(i);
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
}
