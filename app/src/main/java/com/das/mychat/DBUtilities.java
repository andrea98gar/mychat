package com.das.mychat;

import android.content.Context;

import org.json.simple.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;

import javax.net.ssl.HttpsURLConnection;

public class DBUtilities {
    private static DBUtilities instancia;

    private DBUtilities(){}

    public static DBUtilities getInstance(){
        if (instancia==null){
            instancia = new DBUtilities();
        }
        return instancia;
    }

    public String postDB(Context ctx, JSONObject parametrosJSON) {
        String result = "";
        //Conexion con el servidor
        HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance().crearConexionSegura(ctx, "https://134.209.235.115/agarcia683/WEB/mychat.php");

        try {
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
                String line= "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                inputStream.close();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
