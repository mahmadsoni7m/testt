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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Канал ва ҷадвали саволҳои ҳаррӯза: 06:00, 13:00, 19:00
        DailyQuizNotifications.createChannel(this)
        DailyQuizScheduler.scheduleAll(this)

        setContent {
            IslamicQuizTheme {
                IslamicQuizApp()
            }
        }

        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }
}

@Composable
private fun IslamicQuizTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            secondary = CorrectGreen,
            background = DarkGreen,
            surface = CardGreen,
            onPrimary = DarkGreen,
            onSecondary = SoftWhite,
            onBackground = SoftWhite,
            onSurface = SoftWhite,
            error = WrongRed
        ),
        content = content
    )
}

@Composable
private fun IslamicQuizApp() {
    var selectedCategory by remember { mutableStateOf<QuizCategory?>(null) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkGreen
    ) {
        when {
            selectedCategory == null -> {
                CategoryScreen { category ->
                    selectedCategory = category
                    questionIndex = 0
                    selectedAnswer = -1
                    score = 0
                    finished = false
                }
            }

            finished -> {
                ResultScreen(
                    score = score,
                    total = selectedCategory!!.questions.size,
                    onHome = {
                        selectedCategory = null
                        questionIndex = 0
                        selectedAnswer = -1
                        score = 0
                        finished = false
                    },
                    onRetry = {
                        questionIndex = 0
                        selectedAnswer = -1
                        score = 0
                        finished = false
                    }
                )
            }

            selectedCategory!!.questions.isEmpty() -> {
                EmptyQuestionsScreen {
                    selectedCategory = null
                }
            }

            else -> {
                val category = selectedCategory!!
                val questions = category.questions
                val safeIndex = questionIndex.coerceIn(0, questions.lastIndex)
                val currentQuestion = questions[safeIndex]

                QuizScreen(
                    category = category,
                    question = currentQuestion,
                    index = safeIndex,
                    total = questions.size,
                    selectedAnswer = selectedAnswer,
                    onAnswer = { answer ->
                        if (selectedAnswer == -1) {
                            selectedAnswer = answer
                            if (answer == currentQuestion.correct) {
                                score += 1
                            }
                        }
                    },
                    onNext = {
                        if (safeIndex < questions.lastIndex) {
                            questionIndex = safeIndex + 1
                            selectedAnswer = -1
                        } else {
                            finished = true
                        }
                    },
                    onBack = {
                        selectedCategory = null
                        selectedAnswer = -1
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryScreen(onChoose: (QuizCategory) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen)
            .padding(20.dp)
    ) {
        Text(
            text = "Ассалому алайкум",
            color = Gold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Саволҳои Исломӣ",
            color = SoftWhite,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Дониши худро санҷед ва ҳар рӯз чизе омӯзед.",
            color = SoftWhite.copy(alpha = 0.75f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📅 Саволи ҳаррӯза",
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Огоҳиномаҳо: 06:00 • 13:00 • 19:00",
                    color = SoftWhite,
                    fontSize = 13.sp
                )
                Text(
                    text = "Ҷавобро аз дохили огоҳинома фиристед.",
                    color = SoftWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Мавзӯъҳо",
            color = Gold,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (categories.isEmpty()) {
            Text(
                text = "Ҳоло саволҳо илова нашудаанд. Questions.kt-ро санҷед.",
                color = SoftWhite
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = categories,
                    key = { category -> category.name }
                ) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChoose(category) },
                        colors = CardDefaults.cardColors(containerColor = CardGreen),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(17.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category.emoji, fontSize = 28.sp)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 13.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    color = SoftWhite,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${category.questions.size} савол",
                                    color = Gold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(text = "›", color = Gold, fontSize = 28.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizScreen(
    category: QuizCategory,
    question: Question,
    index: Int,
    total: Int,
    selectedAnswer: Int,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen)
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(text = "‹ Бозгашт", color = Gold)
        }
        Text(text = category.name, color = Gold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Саволи ${index + 1} аз $total",
                color = SoftWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${(index + 1) * 100 / total}%",
                color = Gold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Истифодаи overload-и Float барои мувофиқат бо версияҳои гуногуни Material3
        LinearProgressIndicator(
            progress = (index + 1f) / total.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            color = Gold,
            trackColor = CardGreen
        )

        Spacer(modifier = Modifier.height(22.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardGreen),
            shape = RoundedCornerShape(17.dp)
        ) {
            Text(
                text = question.text,
                modifier = Modifier.padding(19.dp),
                color = SoftWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(15.dp))
        question.answers.forEachIndexed { answerIndex, answerText ->
            val backgroundColor = when {
                selectedAnswer == -1 -> CardGreen
                answerIndex == question.correct -> CorrectGreen
                answerIndex == selectedAnswer -> WrongRed
                else -> CardGreen
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable(enabled = selectedAnswer == -1) {
                        onAnswer(answerIndex)
                    },
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                shape = RoundedCornerShape(13.dp)
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${answerIndex + 1}.",
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = answerText,
                        modifier = Modifier.padding(start = 12.dp),
                        color = SoftWhite,
                        fontSize = 15.sp
                    )
                }
            }
        }

        if (selectedAnswer != -1) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (selectedAnswer == question.correct) {
                    "✓ Ҷавоби дуруст!"
                } else {
                    "✕ Ҷавоби нодуруст."
                },
                color = if (selectedAnswer == question.correct) {
                    Color(0xFF8BE0A5)
                } else {
                    Color(0xFFFF9999)
                },
                fontWeight = FontWeight.Bold
            )

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = question.explanation,
                    color = SoftWhite.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text(
                    text = if (index == total - 1) "Натиҷа" else "Саволи навбатӣ",
                    color = DarkGreen
                )
            }
        }
    }
}

@Composable
private fun ResultScreen(
    score: Int,
    total: Int,
    onHome: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🏆", fontSize = 60.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Натиҷаи шумо",
            color = Gold,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$score / $total",
            color = SoftWhite,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (total > 0 && score == total) {
                "Офарин! Ҳама ҷавобҳо дурустанд."
            } else {
                "Офарин! Омӯзишро идома диҳед."
            },
            color = SoftWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Gold)
        ) {
            Text(text = "Аз нав оғоз кардан", color = DarkGreen)
        }
        TextButton(onClick = onHome) {
            Text(text = "Ба саҳифаи асосӣ", color = Gold)
        }
    }
}

@Composable
private fun EmptyQuestionsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Дар ин категория ҳоло савол нест.",
            color = SoftWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(onClick = onBack) {
            Text(text = "Бозгашт")
        }
    }
}
