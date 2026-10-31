package tj.soni.quiz.daily

import android.content.Context

class DailyQuizStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("daily_islamic_quiz", Context.MODE_PRIVATE)

    fun getMastered(): MutableSet<String> = prefs.getStringSet("mastered", emptySet())?.toMutableSet() ?: mutableSetOf()
    fun saveMastered(ids: Set<String>) { prefs.edit().putStringSet("mastered", ids.toSet()).apply() }

    fun getAskedToday(): MutableSet<String> = prefs.getStringSet("asked_today", emptySet())?.toMutableSet() ?: mutableSetOf()
    fun saveAskedToday(ids: Set<String>) { prefs.edit().putStringSet("asked_today", ids.toSet()).apply() }

    fun getPendingId(): String? = prefs.getString("pending_id", null)
    fun setPendingId(id: String?) { prefs.edit().putString("pending_id", id).apply() }

    fun getSavedDate(): String? = prefs.getString("saved_date", null)
    fun saveDate(date: String) { prefs.edit().putString("saved_date", date).apply() }

    fun resetForNewDay(today: String) {
        if (getSavedDate() != today) {
            saveAskedToday(emptySet())
            saveDate(today)
        }
    }

    fun getSessionCount(): Int = prefs.getInt("session_count", 0)
    fun setSessionCount(count: Int) { prefs.edit().putInt("session_count", count.coerceAtLeast(0)).apply() }
    fun clearPending() = setPendingId(null)
}
