package cs246.sara.caretrackerapp;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPreferences {
    /**
     * Determines the value of a string parameter
     *
     * @param context the application context, required by Android to get the preferences file
     * @param name    name of the parameter to obtain
     * @param def_val default value to return if the parameter doesn't exist (hasn't been saved yet)
     * @return the integer value stored
     */
    public static String getString(Context context, String name, String def_val) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        return prefs.getString(name, def_val);
    }

    /**
     * Saves the value of an String parameter
     *
     * @param context the application context, required by Android to get the preferences file
     * @param name    the name of the parameter to set
     * @param val     the value to store for the parameter
     */
    public static void setString(Context context, String name, String val) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(name, val);
        editor.apply();
    }

    /**
     * Removes the key/value pair from the preferences file
     *
     * @param context the application context, required by Android to get the preferences file
     * @param key    the name of the key to remove
     */
    public static void remove(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Determines the value of an integer parameter
     *
     * @param context the application context, required by Android to get the preferences file
     * @param name    name of the parameter to obtain
     * @param def_val default value to return if the parameter doesn't exist (hasn't been saved yet)
     * @return the integer value stored
     */
    public static int getInt(Context context, String name, int def_val) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        return prefs.getInt(name, def_val);
    }

    /**
     * Saves the value of an integer parameter
     *
     * @param context the application context, required by Android to get the preferences file
     * @param name    the name of the parameter to set
     * @param val     the value to store for the parameter
     */
    public static void setInt(Context context, String name, int val) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(name, val);
        editor.apply();
    }
}
