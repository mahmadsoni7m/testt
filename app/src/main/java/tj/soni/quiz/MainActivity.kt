
package tj.soni.quiz

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

private val DarkGreen = Color(0xFF102D24)
private val CardGreen = Color(0xFF1C4938)
private val Gold = Color(0xFFE8C875)
private val White = Color.White
private val CorrectGreen = Color(0xFF237D4B)
private val WrongRed = Color(0xFFB83B3B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ранги қисми болои телефон ва поёни экран
        window.statusBarColor = android.graphics.Color.rgb(16, 45, 36)
        window.navigationBarColor = android.graphics.Color.rgb(16, 45, 36)
        window.decorView.systemUiVisibility = 0

        val prefs = getSharedPreferences(
            "islamic_quiz",
            Context.MODE_PRIVATE
        )

        setContent {
            MaterialTheme {
                IslamicQuizApp(prefs)
            }
        }
    }
}

@Composable
fun IslamicQuizApp(prefs: android.content.SharedPreferences) {

    var bestScore by remember {
        mutableIntStateOf(prefs.getInt("best_score", 0))
    }

    var screen by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }

    val category = categories[selectedCategory]
    val questions = category.questions

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkGreen
    ) {
        when (screen) {

            0 -> HomeScreen(
                bestScore = bestScore,
                onCategorySelected = { selectedCategory = it },
                selectedCategory = selectedCategory,
                onStart = {
                    questionIndex = 0
                    selectedAnswer = -1
                    score = 0
                    screen = 1
                }
            )

            1 -> QuizScreen(
                category = category,
                question = questions[questionIndex],
                questionNumber = questionIndex + 1,
                totalQuestions = questions.size,
                selectedAnswer = selectedAnswer,
                onAnswer = { answer ->
                    if (selectedAnswer == -1) {
                        selectedAnswer = answer

                        if (answer == questions[questionIndex].correct) {
                            score++
                        }
                    }
                },
                onNext = {
                    if (questionIndex < questions.lastIndex) {
                        questionIndex++
                        selectedAnswer = -1
                    } else {
                        if (score > bestScore) {
                            bestScore = score
                            prefs.edit()
                                .putInt("best_score", bestScore)
                                .apply()
                        }
                        screen = 2
                    }
                },
                onBack = { screen = 0 }
            )

            2 -> ResultScreen(
                score = score,
                total = questions.size,
                onHome = { screen = 0 },
                onRetry = {
                    questionIndex = 0
                    selectedAnswer = -1
                    score = 0
                    screen = 1
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    bestScore: Int,
    selectedCategory: Int,
    onCategorySelected: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Text("☪", color = Gold, fontSize = 54.sp)

        Text(
            "Саволҳои Исломӣ",
            color = Gold,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Ассалому алайкум!",
            color = White,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Дониши исломии худро бисанҷед",
            color = White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )

        Spacer(Modifier.height(25.dp))

        Text(
            "Категорияро интихоб кунед",
            color = Gold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories.size) { index ->
                val item = categories[index]
                val selected = selectedCategory == index

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) Gold else CardGreen,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onCategorySelected(index)
                        }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.emoji,
                        fontSize = 25.sp
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        item.name,
                        color = if (selected) DarkGreen else White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        if (selected) "✓" else "›",
                        color = if (selected) DarkGreen else Gold,
                        fontSize = 24.sp
                    )
                }
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = DarkGreen
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "ОҒОЗИ ТЕСТ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            "🏆 Рекорди беҳтарин: $bestScore",
            color = Gold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun QuizScreen(
    category: QuizCategory,
    question: Question,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: Int,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "‹ Бозгашт",
                color = Gold,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onBack() }
            )

            Spacer(Modifier.weight(1f))

            Text(
                category.emoji + " " + category.name,
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Саволи $questionNumber аз $totalQuestions",
            color = Gold,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { questionNumber.toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Gold,
            trackColor = CardGreen
        )

        Spacer(Modifier.height(35.dp))

        Text(
            question.text,
            color = White,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        question.answers.forEachIndexed { index, answer ->

            val backgroundColor = when {
                selectedAnswer == -1 -> CardGreen
                index == question.correct -> CorrectGreen
                index == selectedAnswer -> WrongRed
                else -> CardGreen
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(
                        backgroundColor,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = selectedAnswer == -1) {
                        onAnswer(index)
                    }
                    .padding(17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${index + 1}.",
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    answer,
                    color = White,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )

                if (selectedAnswer != -1 &&
                    index == question.correct
                ) {
                    Text("✓", color = White, fontSize = 22.sp)
                }

                if (selectedAnswer == index &&
                    index != question.correct
                ) {
                    Text("✕", color = White, fontSize = 22.sp)
                }
            }
        }

        if (selectedAnswer != -1) {
            Spacer(Modifier.height(18.dp))

            Text(
                question.explanation,
                color = Gold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = selectedAnswer != -1,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = DarkGreen,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(
                if (questionNumber == totalQuestions)
                    "НАТИҶА"
                else
                    "САВОЛИ НАВБАТӢ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    onHome: () -> Unit,
    onRetry: () -> Unit
) {
    val wrong = total - score

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", fontSize = 65.sp)

        Text(
            "Натиҷаи тест",
            color = Gold,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(25.dp))

        Text(
            "$score / $total",
            color = White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "✓ Ҷавобҳои дуруст: $score",
            color = Color(0xFF65D995),
            fontSize = 19.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "✕ Ҷавобҳои нодуруст: $wrong",
            color = Color(0xFFFF7777),
            fontSize = 19.sp
        )

        Spacer(Modifier.height(35.dp))

        Text(
            when {
                score == total -> "Мошоаллоҳ! Ҳама ҷавобҳо дурустанд!"
                score >= total / 2 -> "Офарин! Кӯшиши хуб!"
                else -> "Боз машқ кун! Ту метавонӣ!"
            },
            color = Gold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(35.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = DarkGreen
            )
        ) {
            Text("АЗ НАВ ОҒОЗ КУН")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Gold
            )
        ) {
            Text("САҲИФАИ АСОСӢ")
        }
    }
}
