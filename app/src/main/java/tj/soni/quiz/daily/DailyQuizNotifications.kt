package tj.soni.quiz.daily

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import tj.soni.quiz.Question
import tj.soni.quiz.R

object DailyQuizNotifications {
    const val CHANNEL_ID = "islamic_daily_quiz"
    const val NOTIFICATION_ID = 7001
    const val ACTION_ANSWER = "tj.soni.quiz.daily.ACTION_ANSWER"
    const val EXTRA_QUESTION_ID = "question_id"
    const val REMOTE_INPUT_KEY = "quiz_answer"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Саволҳои ҳаррӯзаи Исломӣ",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Саволҳои ҳаррӯза аз барномаи Саволҳои Исломӣ" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun showQuestion(context: Context, questionId: String, question: Question, extraMessage: String? = null) {
        createChannel(context)
        val intent = Intent(context, DailyQuizReceiver::class.java).apply {
            action = ACTION_ANSWER
            putExtra(EXTRA_QUESTION_ID, questionId)
        }
        val pending = PendingIntent.getBroadcast(
            context, NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val remoteInput = RemoteInput.Builder(REMOTE_INPUT_KEY)
            .setLabel("Рақами ҷавобро нависед (1-${question.answers.size})")
            .build()
        val action = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send, "Ҷавоб додан", pending
        ).addRemoteInput(remoteInput).setAllowGeneratedReplies(false).build()

        val choices = question.answers.mapIndexed { i, answer -> "${i + 1}. $answer" }.joinToString("\n")
        val body = buildString {
            append(question.text); append("\n\n"); append(choices)
            append("\n\nҶавобро бо рақам фиристед.")
            if (!extraMessage.isNullOrBlank()) { append("\n"); append(extraMessage) }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📖 Саволи Исломӣ")
            .setContentText(question.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .addAction(action)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_ID, notification)
    }

    fun showMessage(context: Context, message: String) {
        createChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Саволҳои Исломӣ")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_ID + 1, notification)
    }
}
