package tj.soni.quiz

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import tj.soni.quiz.daily.DailyQuizNotifications
import tj.soni.quiz.daily.DailyQuizScheduler

private val DarkGreen = Color(0xFF102D24)
private val CardGreen = Color(0xFF1C4938)
private val Gold = Color(0xFFE8C875)
private val CorrectGreen = Color(0xFF237D4B)
private val WrongRed = Color(0xFFB83B3B)
private val SoftWhite = Color(0xFFF5F5F0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DailyQuizNotifications.createChannel(this)
        DailyQuizScheduler.scheduleAll(this)
        setContent { IslamicQuizTheme { IslamicQuizApp() } }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun IslamicQuizTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold, secondary = CorrectGreen, background = DarkGreen,
            surface = CardGreen, onPrimary = DarkGreen, onSecondary = SoftWhite,
            onBackground = SoftWhite, onSurface = SoftWhite, error = WrongRed
        ),
        content = content
    )
}

@Composable
private fun IslamicQuizApp() {
    var category by remember { mutableStateOf<QuizCategory?>(null) }
    var index by remember { mutableIntStateOf(0) }
    var answer by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = DarkGreen) {
        when {
            category == null -> CategoryScreen { chosen ->
                category = chosen; index = 0; answer = -1; score = 0; finished = false
            }
            finished -> ResultScreen(score, category!!.questions.size, {
                category = null; index = 0; answer = -1; score = 0; finished = false
            }, {
                index = 0; answer = -1; score = 0; finished = false
            })
            category!!.questions.isEmpty() -> EmptyScreen { category = null }
            else -> {
                val questions = category!!.questions
                val qIndex = index.coerceIn(0, questions.lastIndex)
                val q = questions[qIndex]
                QuizScreen(category!!, q, qIndex, questions.size, answer,
                    onAnswer = { picked ->
                        if (answer == -1) {
                            answer = picked
                            if (picked == q.correct) score++
                        }
                    },
                    onNext = {
                        if (qIndex < questions.lastIndex) { index++; answer = -1 }
                        else finished = true
                    },
                    onBack = { category = null; answer = -1 }
                )
            }
        }
    }
}

@Composable
private fun CategoryScreen(onChoose: (QuizCategory) -> Unit) {
    Column(Modifier.fillMaxSize().background(DarkGreen).padding(20.dp)) {
        Text("Ассалому алайкум", color = Gold, fontSize = 15.sp)
        Spacer(Modifier.height(6.dp))
        Text("Саволҳои Исломӣ", color = SoftWhite, fontSize = 29.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Дониши худро санҷед ва ҳар рӯз чизе омӯзед.", color = SoftWhite.copy(alpha = .75f))
        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardGreen), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("📅 Саволи ҳаррӯза", color = Gold, fontWeight = FontWeight.Bold)
                Text("Огоҳиномаҳо: 06:00 • 13:00 • 19:00", color = SoftWhite, fontSize = 13.sp)
                Text("Ҷавобро аз дохили огоҳинома фиристед.", color = SoftWhite.copy(alpha = .7f), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Мавзӯъҳо", color = Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (categories.isEmpty()) {
            Text("Ҳоло саволҳо илова нашудаанд. Questions.kt-ро санҷед.", color = SoftWhite)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories) { item ->
                    Card(
                        Modifier.fillMaxWidth().clickable { onChoose(item) },
                        colors = CardDefaults.cardColors(containerColor = CardGreen),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.emoji, fontSize = 28.sp)
                            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                                Text(item.name, color = SoftWhite, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Text("${item.questions.size} савол", color = Gold, fontSize = 12.sp)
                            }
                            Text("›", color = Gold, fontSize = 28.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizScreen(
    category: QuizCategory, question: Question, index: Int, total: Int,
    selected: Int, onAnswer: (Int) -> Unit, onNext: () -> Unit, onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(DarkGreen).padding(20.dp)) {
        TextButton(onClick = onBack) { Text("‹ Бозгашт", color = Gold) }
        Text(category.name, color = Gold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Саволи ${index + 1} аз $total", color = SoftWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${(index + 1) * 100 / total}%", color = Gold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (index + 1f) / total },
            modifier = Modifier.fillMaxWidth(),
            color = Gold,
            trackColor = CardGreen
        )
        Spacer(Modifier.height(22.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardGreen), shape = RoundedCornerShape(17.dp)) {
            Text(question.text, Modifier.padding(19.dp), color = SoftWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(15.dp))
        question.answers.forEachIndexed { i, text ->
            val bg = when {
                selected == -1 -> CardGreen
                i == question.correct -> CorrectGreen
                i == selected -> WrongRed
                else -> CardGreen
            }
            Card(
                Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable(enabled = selected == -1) { onAnswer(i) },
                colors = CardDefaults.cardColors(containerColor = bg),
                shape = RoundedCornerShape(13.dp)
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}.", color = Gold, fontWeight = FontWeight.Bold)
                    Text(text, Modifier.padding(start = 12.dp), color = SoftWhite, fontSize = 15.sp)
                }
            }
        }
        if (selected != -1) {
            Spacer(Modifier.height(10.dp))
            Text(
                if (selected == question.correct) "✓ Ҷавоби дуруст!" else "✕ Ҷавоби нодуруст.",
                color = if (selected == question.correct) Color(0xFF8BE0A5) else Color(0xFFFF9999),
                fontWeight = FontWeight.Bold
            )
            if (question.explanation.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(question.explanation, color = SoftWhite.copy(alpha = .8f), fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNext, Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                Text(if (index == total - 1) "Натиҷа" else "Саволи навбатӣ", color = DarkGreen)
            }
        }
    }
}

@Composable
private fun ResultScreen(score: Int, total: Int, onHome: () -> Unit, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", fontSize = 60.sp)
        Spacer(Modifier.height(14.dp))
        Text("Натиҷаи шумо", color = Gold, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("$score / $total", color = SoftWhite, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Text(
            if (total > 0 && score == total) "Офарин! Ҳама ҷавобҳо дурустанд."
            else "Офарин! Омӯзишро идома диҳед.",
            color = SoftWhite, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Button(onClick = onRetry, Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
            Text("Аз нав оғоз кардан", color = DarkGreen)
        }
        TextButton(onClick = onHome) { Text("Ба саҳифаи асосӣ", color = Gold) }
    }
}

@Composable
private fun EmptyScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("Дар ин категория ҳоло савол нест.", color = SoftWhite, textAlign = TextAlign.Center)
        Spacer(Modifier.height(15.dp))
        Button(onClick = onBack) { Text("Бозгашт") }
    }
}
