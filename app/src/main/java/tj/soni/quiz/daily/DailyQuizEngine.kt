package tj.soni.quiz.daily

import android.content.Context
import tj.soni.quiz.Question
import tj.soni.quiz.categories
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DailyQuizEngine {
    private const val DAILY_LIMIT = 5

    private data class Item(val id: String, val question: Question)

    private fun allQuestions(): List<Item> =
        categories.flatMapIndexed { ci, category ->
            category.questions.mapIndexed { qi, question -> Item("$ci:$qi", question) }
        }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun showNext(context: Context) {
        val ctx = context.applicationContext
        val store = DailyQuizStore(ctx)
        store.resetForNewDay(todayKey())
        val all = allQuestions()

        if (all.isEmpty()) {
            DailyQuizNotifications.showMessage(ctx, "Ҳоло саволҳо илова нашудаанд.")
            return
        }

        val mastered = store.getMastered()
        if (mastered.size >= all.size) {
            store.saveMastered(emptySet())
            store.saveAskedToday(emptySet())
            store.setSessionCount(0)
        }

        val pendingId = store.getPendingId()
        if (pendingId != null) {
            val pending = all.firstOrNull { it.id == pendingId }
            if (pending != null) {
                DailyQuizNotifications.showQuestion(ctx, pending.id, pending.question)
                return
            }
            store.clearPending()
        }

        if (store.getSessionCount() >= DAILY_LIMIT) return

        val asked = store.getAskedToday()
        val available = all.filter { it.id !in store.getMastered() && it.id !in asked }
        if (available.isEmpty()) {
            DailyQuizNotifications.showMessage(ctx, "Барои имрӯз саволи нав нест. Пагоҳ идома медиҳем. 📖")
            return
        }

        val selected = available.random()
        store.setPendingId(selected.id)
        store.saveAskedToday(asked.apply { add(selected.id) })
        DailyQuizNotifications.showQuestion(ctx, selected.id, selected.question)
    }

    fun submitAnswer(context: Context, questionId: String, answerText: String) {
        val ctx = context.applicationContext
        val store = DailyQuizStore(ctx)
        if (store.getPendingId() != questionId) return

        val question = allQuestions().firstOrNull { it.id == questionId }?.question ?: return
        val answerIndex = (answerText.trim().toIntOrNull() ?: 0) - 1

        if (answerIndex !in question.answers.indices) {
            DailyQuizNotifications.showQuestion(
                ctx, questionId, question,
                "Рақами ҷавобро аз 1 то ${question.answers.size} нависед."
            )
            return
        }

        if (answerIndex == question.correct) {
            val mastered = store.getMastered()
            mastered.add(questionId)
            store.saveMastered(mastered)
            DailyQuizNotifications.showMessage(ctx, "Офарин! Ҷавоб дуруст аст. ✅")
        } else {
            DailyQuizNotifications.showMessage(ctx, "Ҷавоб нодуруст аст. Ин савол фардо такрор мешавад. 📚")
        }

        store.clearPending()
        store.setSessionCount(store.getSessionCount() + 1)
        // Саволи нав дар вақти огоҳии навбатӣ меояд.
    }
}
