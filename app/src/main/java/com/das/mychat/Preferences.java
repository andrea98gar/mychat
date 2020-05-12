package com.das.mychat;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static Preferences instancia;

    private Preferences(){}

    public static Preferences getInstance(){
        if (instancia==null){
            instancia = new Preferences();
        }
        return instancia;
    }

    public void setUserPreferences(String user, Context ctx){
        //Guardar el usuario en las preferencias de la aplicación
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", user);
        editor.apply();
    }

    public void setCheckUserPreference(Context ctx, String check){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("check", check);
        editor.apply();
    }

    public String getUserPreferences(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("user", "");
    }

    public boolean checkUserPreferences(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("check", "").equals("done")){
            return true;
        }else{
            return false;
        }
    }


}
