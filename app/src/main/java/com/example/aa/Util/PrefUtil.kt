package com.example.aa.Util

import android.content.Context
import android.preference.PreferenceManager
import com.example.aa.MainActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class PrefUtil {
    companion object {

        fun getTimerLength(context: Context): Int{
            //placeholder
//            return 8
            return 2
        }

        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.resocoder.timer.previous_timer_length_seconds"

        fun getPreviousTimerLengthSeconds(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        fun setPreviousTimerLengthSeconds(seconds: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
            editor.apply()
        }


        private const val TIMER_STATE_ID = "com.resocoder.timer.timer_state"

        fun getTimerState(context: Context): MainActivity.TimerState {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return MainActivity.TimerState.values()[ordinal]
        }

        fun setTimerState(state: MainActivity.TimerState, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
        }


        private const val SECONDS_REMAINING_ID = "com.resocoder.timer.seconds_remaining"

        fun getSecondsRemaining(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID, seconds)
            editor.apply()
        }

        private const val BLINDS_STATE = "com.resocoder.timer.blinds_state"

        fun getBlindsState(context: Context): ArrayDeque<String>?{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val blindsJson = preferences.getString(BLINDS_STATE, "")
            return Gson().fromJson(
                blindsJson,
                object : TypeToken<ArrayDeque<String>>(){}.type)
        }

        fun setBlindsState(blinds: ArrayDeque<String>?, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val blindsJson = Gson().toJson(blinds)
            editor.putString(BLINDS_STATE, blindsJson)
            editor.apply()
        }

        private const val CURRENT_BLIND = "com.resocoder.timer.current_blind"

        fun setCurrentBlind(blind: String?, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putString(CURRENT_BLIND, blind)
            editor.apply()
        }
        fun getCurrentBlind(context: Context): String? {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(CURRENT_BLIND, "")
        }

        private const val ALARM_SET_TIME_ID = "com.resocoder.timer.time_id"

        fun getAlarmSetTime(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(ALARM_SET_TIME_ID, 0L)
        }

        fun setAlarmSetTime(time: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.apply()

        }
    }
}
