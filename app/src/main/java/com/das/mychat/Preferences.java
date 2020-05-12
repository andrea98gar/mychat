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

    /**
     * Guarda el usuario actual en las preferencias
     * @param user
     * @param ctx
     */
    public void setUserPreferences(String user, Context ctx){
        //Guardar el usuario en las preferencias de la aplicación
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", user);
        editor.apply();
    }

    /**
     * Si la confirmación del código ha ido bien guarda "done" y sino "error".
     * Es útil para saber si el registro se ha completado
     * @param ctx
     * @param check
     */
    public void setCheckUserPreference(Context ctx, String check){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("check", check);
        editor.apply();
    }

    /**
     * Recupera el usuario logueado de las preferencias
     * @param ctx
     * @return
     */
    public String getUserPreferences(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("user", "");
    }

    /**
     * Comprueba si la comprobación del código ha ido bien
     * @param ctx
     * @return
     */
    public boolean checkUserPreferences(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("check", "").equals("done")){
            return true;
        }else{
            return false;
        }
    }


}
