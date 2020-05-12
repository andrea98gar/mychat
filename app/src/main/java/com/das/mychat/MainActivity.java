package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.util.Random;
import java.util.regex.Pattern;

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


        //Si el usuario ya está logueado
        if(Preferences.getInstance().getUserPreferences(this)!=null){
            Intent i = new Intent(this, MyUserListActivity.class);
            this.startActivity(i);
        }
    }

    /**
     * Ejecuta el php que se encarga de enviar un mensaje con el código.
     * @param email
     * @param pin
     */
    private void sendEmail(String email, String pin){
        //Conexion con el servidor
        HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(this, "https://134.209.235.115/agarcia683/WEB/sendEmail.php");

        try {
            //Parámetros que se pasan a sendEmail.php
            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("email", email);
            parametrosJSON.put("pin", pin);

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametrosJSON.toString());
            out.close();

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                Toast.makeText(MainActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Comprueba que el formato de email sea correcto
     * @param email
     * @return
     */
    private boolean checkEmailFormat(String email){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    /**
     * Al pulsar el botón NEXT, se añade a la bd el nuevo usuario.
     * En caso de que ya exista se modificará el nombre y el código.
     * @param v
     */
    public void login(View v){
        //Obtener los campos introducidos por el usuario
        EditText i_email = (EditText) findViewById(R.id.email);
        String email = i_email.getText().toString();
        EditText i_name = (EditText) findViewById(R.id.name);
        String name = i_name.getText().toString();

        //Comprobar que el usuario ha introducido todos los campos
        if(name.isEmpty() || email.isEmpty()){
            Toast.makeText(MainActivity.this, R.string.error_empty, Toast.LENGTH_SHORT).show();
        }else{
            //Comprobar formato email
            if(checkEmailFormat(email)){
                //Generar clave
                Random pinGenerator = new Random();
                String pin = String.valueOf(pinGenerator.nextInt(999999-0+1));

                //Parámetros que se pasan a mychat.php
                JSONObject parametrosJSON = new JSONObject();
                parametrosJSON.put("action", "login");
                parametrosJSON.put("user", email);
                parametrosJSON.put("name", name);
                parametrosJSON.put("pin", pin);

                //Post en base de datos
                String result = DBUtilities.getInstance().postDB(this, parametrosJSON);

                //Se comprueba si se ha ocurrido algún error
                if(result.contains("Ha habido algún error")){
                    Toast.makeText(MainActivity.this, R.string.error_login, Toast.LENGTH_SHORT).show();
                }else{
                    //Guardar el usuario en las preferencias de la aplicación
                    Preferences.getInstance().setUserPreferences(email, this);

                    //Enviar email
                    sendEmail(email, pin);

                    //Siguiente actividad
                    Intent i = new Intent(this, CodeActivity.class);
                    this.startActivity(i);
                }
            }else{
                Toast.makeText(MainActivity.this, R.string.error_email_format, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
