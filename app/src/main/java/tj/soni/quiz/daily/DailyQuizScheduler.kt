package tj.soni.quiz.daily

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object DailyQuizScheduler {
    const val ACTION_DAILY_ALARM = "tj.soni.quiz.daily.ACTION_DAILY_ALARM"
    const val EXTRA_ALARM_HOUR = "alarm_hour"
    val HOURS = listOf(6, 13, 19)

    fun scheduleAll(context: Context) {
        HOURS.forEach { scheduleNext(context, it) }
    }

    fun scheduleNext(context: Context, hour: Int) {
        if (hour !in HOURS) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, DailyQuizReceiver::class.java).apply {
            action = ACTION_DAILY_ALARM
            putExtra(EXTRA_ALARM_HOUR, hour)
        }
        val pending = PendingIntent.getBroadcast(
            context, hour, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
        }
    }

    fun cancelAll(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        HOURS.forEach { hour ->
            val intent = Intent(context, DailyQuizReceiver::class.java).apply { action = ACTION_DAILY_ALARM }
            val pending = PendingIntent.getBroadcast(
                context, hour, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) { manager.cancel(pending); pending.cancel() }
        }
    }
}
