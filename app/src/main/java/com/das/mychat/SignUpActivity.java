package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.simple.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ProtocolException;

import javax.net.ssl.HttpsURLConnection;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setTitle("Sign Up");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }


    /**
     * Al pulsar el botón SIGNUP se ejecuta el siguiente método
     * encargado de dar de alta en la base de datos el nuevo usuario
     * @param v
     */
    public void signup(View v){
        //Obtener los campos introducidos por el usuario
        EditText i_name = (EditText) findViewById(R.id.name);
        String name = i_name.getText().toString();
        EditText i_username = (EditText) findViewById(R.id.username);
        String username = i_username.getText().toString();
        EditText i_pass = (EditText) findViewById(R.id.password);
        String pass = i_pass.getText().toString();

        //Comprobar que el usuario ha introducido todos los campos
        if(name.isEmpty() || username.isEmpty() || pass.isEmpty()){
            Toast.makeText(SignUpActivity.this, "Empty fields", Toast.LENGTH_SHORT).show();
        }else{
            //Conexion con el servidor
            HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/conexion.php");

            try {
                //Parámetros que se pasan a conexion.php
                JSONObject parametrosJSON = new JSONObject();
                parametrosJSON.put("action", "signup");
                parametrosJSON.put("username", username);
                parametrosJSON.put("password", pass);
                parametrosJSON.put("name", name);

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
                    //Log.i("MY-APP", "DATA: " + result); //genera mensajes de tipo informacion

                    //Se comprueba si ha habido algún error
                    if(result.contains("Ha habido algún error")){//El usuario ya existe
                        Toast.makeText(SignUpActivity.this, "Username used", Toast.LENGTH_SHORT).show();
                    }else{//Si el registro ha sido correcto se abre la actividad del login (MainActivity)
                        Toast.makeText(SignUpActivity.this, "SignUp succesfull", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(this, MainActivity.class);
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
