package com.obari.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String nom, String telephone, String role, double solde) {
        prefs.edit()
                .putString(Constants.PREF_NOM, nom)
                .putString(Constants.PREF_TELEPHONE, telephone)
                .putString(Constants.PREF_ROLE, role)
                .putFloat(Constants.PREF_SOLDE, (float) solde)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(Constants.PREF_TELEPHONE);
    }

    public String getNom()       { return prefs.getString(Constants.PREF_NOM, ""); }
    public String getTelephone() { return prefs.getString(Constants.PREF_TELEPHONE, ""); }
    public String getRole()      { return prefs.getString(Constants.PREF_ROLE, Constants.ROLE_CLIENT); }
    public double getSolde()     { return prefs.getFloat(Constants.PREF_SOLDE, 0f); }

    public void saveSolde(double solde) {
        prefs.edit().putFloat(Constants.PREF_SOLDE, (float) solde).apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
