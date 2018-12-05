package pt.uc.cm.daylistudent.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.widget.TextView
import pt.uc.cm.daylistudent.R

object SharedPreferencesUtils {

    private val TAG = SharedPreferencesUtils::class.java.simpleName

    private var sharedPreferences: SharedPreferences? = null

    private val NOME = "nameKey"
    private val EMAIL = "emailKey"
    private val GROUP_ID = "groupIdKey"

    val NONE_GROUP_ID = -1

    fun readInfoUser(context: Context, tvUserName: TextView, tvEmail: TextView) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        tvUserName.text = sharedPreferences!!.getString(NOME, "DEFAULT")
        tvEmail.text = sharedPreferences!!.getString(EMAIL, "DEFAULT")
    }

    fun readPreferencesUser(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        Log.d(TAG, "selected theme ${sharedPreferences!!.getString("themeKey", "THEMERED")!!}")
        when (sharedPreferences!!.getString("themeKey", "THEMEYELLOW")) {
            "RedTheme" -> context.setTheme(R.style.RedTheme)
            "YellowTheme" -> context.setTheme(R.style.YellowTheme)
            "GreenTheme" -> context.setTheme(R.style.GreenTheme)
        }
    }

    fun readTheme(context: Context): Int {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        Log.d(TAG, "selected theme ${sharedPreferences!!.getString("themeKey", "THEMERED")!!}")
        when (sharedPreferences!!.getString("themeKey", "THEMEYELLOW")) {
            "RedTheme" -> return R.style.RedTheme
            "YellowTheme" -> return R.style.YellowTheme
            "GreenTheme" -> return R.style.GreenTheme
        }
        return R.style.DayliTheme
    }

    fun readUserName(): String {
        return sharedPreferences!!.getString(NOME, "Default")!!
    }

    fun readSelectedGroupId(): Int {
        return sharedPreferences!!.getString(GROUP_ID, NONE_GROUP_ID.toString())!!.toInt()
    }

    fun writeSelectedGroupId(context: Context, id: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(GROUP_ID, id.toString())
        sharedPreferencesEditor.apply()
    }
}


