package com.example.easyvet.data.local

import android.content.Context
import android.content.SharedPreferences

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("easyvet_user_session", Context.MODE_PRIVATE)

    fun saveUserEmail(email: String) {
        prefs.edit().putString("saved_user_email", email.trim()).apply()
    }

    fun getSavedUserEmail(): String? {
        return prefs.getString("saved_user_email", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
