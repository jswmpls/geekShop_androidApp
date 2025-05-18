package com.example.geekshop.data.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var userId: Int
        get() = sharedPref.getInt("current_user_id", -1)
        set(value) = sharedPref.edit().putInt("current_user_id", value).apply()

    var userName: String?
        get() = sharedPref.getString("user_name", null)
        set(value) = sharedPref.edit().putString("user_name", value).apply()

    var userLogin: String?
        get() = sharedPref.getString("user_login", null)
        set(value) = sharedPref.edit().putString("user_login", value).apply()

    var userBonus: Int
        get() = sharedPref.getInt("user_bonus", 0)
        set(value) = sharedPref.edit().putInt("user_bonus", value).apply()
}