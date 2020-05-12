package com.das.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class CodeActivity extends AppCompatActivity {
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        //Obtener usuario
        user = Preferences.getInstance().getUserPreferences(this);

        Log.i("MY-APP", "USUARIO REGISTRADO: " + user);

    }

    /**
     * Comprobar código
     * @param v
     */
    public void checkCode(View v) throws ParseException {
        //Obtener el código
        EditText i_code = (EditText) findViewById(R.id.code);
        String code = i_code.getText().toString();

        //Comprobar que el usuario ha introducido todos los campos
        if(code.isEmpty()){
            Toast.makeText(CodeActivity.this, R.string.error_empty, Toast.LENGTH_SHORT).show();
        }else{
            //Parámetros que se pasan a conexion.php
            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("action", "checkPass");
            parametrosJSON.put("user", user);

            //Recuperar el código de la bd
            String result = DBUtilities.getInstance().postDB(this, parametrosJSON);

            //Se comprueba si se ha ocurrido algún error
            if(result.contains("Ha habido algún error")){
                Toast.makeText(CodeActivity.this, R.string.error_login, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MainActivity.class);
                this.startActivity(i);

            }else{
                //Obtener el código
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(result);
                if(json != null){
                    String clave = (String) json.get("clave");
                    //Comprobar que el código es correcto
                    if(clave.equals(code)){
                        //Guardar el usuario en las preferencias de la aplicación
                        Preferences.getInstance().setCheckUserPreference(this, "done");
                        //Ir a la pantalla principal de la aplicación
                        Intent i = new Intent(this, MyUserListActivity.class);
                        this.startActivity(i);
                    }else{
                        Toast.makeText(CodeActivity.this, R.string.error_code, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(this, MainActivity.class);
                        this.startActivity(i);
                    }
                }
            }
        }
    }
}
