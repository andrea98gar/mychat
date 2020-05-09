package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Log in");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    /**
     * Al pulsar el botón SIGNUP se abre la actividad que permite al usuario registrarse
     * @param v
     */
    public void signup(View v){
        Intent i = new Intent(this, SignUpActivity.class);
        this.startActivity(i);
    }

    /**
     * Al pulsar el botón LOGIN, se comprueban que los campos introducidos (usuario y contraseña)
     * son correctos con respecto a la bd.
     * @param v
     */
    public void login(View v){
        //Obtener los campos introducidos por el usuario
        EditText i_username = (EditText) findViewById(R.id.username);
        String username = i_username.getText().toString();
        EditText i_pass = (EditText) findViewById(R.id.password);
        String pass = i_pass.getText().toString();

        //Comprobar que el usuario ha introducido todos los campos
        if(username.isEmpty() || pass.isEmpty()){
            Toast.makeText(MainActivity.this, "Empty fields", Toast.LENGTH_SHORT).show();
        }else{
            //Conexion con el servidor
            HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/mychat.php");

            try {
                //Parámetros que se pasan a conexion.php
                JSONObject parametrosJSON = new JSONObject();
                parametrosJSON.put("action", "login");
                parametrosJSON.put("username", username);
                parametrosJSON.put("password", pass);

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                Log.i("MY-APP", "LOGIN PARAMS: " + parametrosJSON); //genera mensajes de tipo informacion

                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametrosJSON.toString());
                out.close();

                int statusCode = urlConnection.getResponseCode();
                Log.i("MY-APP", "STATUS LOGIN: " + statusCode); //genera mensajes de tipo informacion

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

                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(result);
                    String usuario = (String) json.get("usuario");


                    //Se comprueba si se ha obtenido algún usuario o no
                    if(usuario==null){
                        Toast.makeText(MainActivity.this, "Login incorrect, please try again", Toast.LENGTH_SHORT).show();
                    }else{//Si el login ha sido correcto entonces se abrirá la actividad PicActivity
                        Toast.makeText(MainActivity.this, "Login correct", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(this, MyUserListActivity.class);
                        i.putExtra("usuario", usuario);
                        this.startActivity(i);
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
