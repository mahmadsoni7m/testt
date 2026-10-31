package tj.soni.quiz.daily

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput

class DailyQuizReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DailyQuizScheduler.ACTION_DAILY_ALARM -> {
                val hour = intent.getIntExtra(DailyQuizScheduler.EXTRA_ALARM_HOUR, -1)
                if (hour in DailyQuizScheduler.HOURS) {
                    DailyQuizScheduler.scheduleNext(context, hour)
                    DailyQuizEngine.showNext(context)
                }
            }
            DailyQuizNotifications.ACTION_ANSWER -> {
                val answer = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(DailyQuizNotifications.REMOTE_INPUT_KEY)?.toString()
                val id = intent.getStringExtra(DailyQuizNotifications.EXTRA_QUESTION_ID)
                if (!answer.isNullOrBlank() && !id.isNullOrBlank()) {
                    DailyQuizEngine.submitAnswer(context, id, answer)
                }
            }
        }
    }
}
