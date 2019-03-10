package com.example.aa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.aa.Util.PrefUtil

class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

//        val notif = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
//            .setSmallIcon(R.drawable.icon_time_end_ios)
//            .setAutoCancel(true)
//            .setContentTitle("PRIVETIK")
//            .setContentText("ETO YA")
//            .build()
//        val channelId = 2
//        NotificationManagerCompat.from(context).notify(channelId, notif)
        PrefUtil.setAlarmSetTime(0, context)
        val blinds = PrefUtil.getBlindsState(context)
        blinds?.pop()
        PrefUtil.setBlindsState(blinds, context)

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("TAG_FROM_TIMER_RECEIVER", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
