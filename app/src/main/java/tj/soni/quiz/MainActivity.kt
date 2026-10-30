package tj.soni.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizApp()
        }
    }
}

data class Question(
    val question: String,
    val answers: List<String>,
    val correct: Int,
    val explanation: String
)

private val questions = listOf(
    Question(
        "Пойтахти Тоҷикистон кадом шаҳр аст?",
        listOf("Хуҷанд", "Душанбе", "Кӯлоб", "Бохтар"),
        1,
        "Душанбе пойтахти Ҷумҳурии Тоҷикистон мебошад."
    ),
    Question(
        "5 × 6 чанд мешавад?",
        listOf("25", "30", "35", "20"),
        1,
        "5 × 6 = 30"
    ),
    Question(
        "Кадом забон барои Android истифода мешавад?",
        listOf("HTML", "CSS", "Kotlin", "SQL"),
        2,
        "Kotlin яке аз забонҳои асосии сохтани Android мебошад."
    ),
    Question(
        "Офтоб чист?",
        listOf("Сайёра", "Ситора", "Моҳ", "Комета"),
        1,
        "Офтоб ситораест, ки ба Замин рӯшноӣ медиҳад."
    ),
    Question(
        "Дар як соат чанд дақиқа ҳаст?",
        listOf("30", "100", "60", "90"),
        2,
        "Як соат 60 дақиқа дорад."
    )
)

@Composable
fun QuizApp() {
    var questionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val background = Color(0xFF090E1A)
    val cardColor = Color(0xFF151E30)
    val purple = Color(0xFF8B5CF6)
    val green = Color(0xFF16A34A)
    val red = Color(0xFFDC2626)

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = purple,
            background = background,
            surface = cardColor
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = "SONI QUIZ",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Дониши худро санҷ!",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(30.dp))

                if (!finished) {
                    Text(
                        text = "САВОЛИ ${questionIndex + 1} / ${questions.size}",
                        color = purple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = (questionIndex + 1).toFloat() /
                                questions.size.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = purple,
                        trackColor = cardColor
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp)
                        ) {
                            Text(
                                text = "Савол",
                                color = purple,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = questions[questionIndex].question,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    questions[questionIndex].answers.forEachIndexed {
                            index, answer ->

                        val buttonColor = when {
                            selectedAnswer == -1 -> cardColor
                            index == questions[questionIndex].correct -> green
                            index == selectedAnswer -> red
                            else -> cardColor
                        }

                        Button(
                            onClick = {
                                if (selectedAnswer == -1) {
                                    selectedAnswer = index

                                    if (index ==
                                        questions[questionIndex].correct
                                    ) {
                                        score++
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .heightIn(min = 55.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                disabledContainerColor = buttonColor
                            ),
                            enabled = selectedAnswer == -1
                        ) {
                            Text(
                                text = "${index + 1}. $answer",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    if (selectedAnswer != -1) {
                        Spacer(modifier = Modifier.height(15.dp))

                        val isCorrect =
                            selectedAnswer == questions[questionIndex].correct

                        Text(
                            text = if (isCorrect)
                                "✓ Офарин! Ҷавоби дуруст."
                            else
                                "✕ Ҷавоби нодуруст.",
                            color = if (isCorrect) green else red,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = questions[questionIndex].explanation,
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (questionIndex < questions.lastIndex) {
                                    questionIndex++
                                    selectedAnswer = -1
                                } else {
                                    finished = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text(
                                text = if (questionIndex == questions.lastIndex)
                                    "Натиҷа"
                                else
                                    "Саволи навбатӣ →",
                                fontSize = 17.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "🏆",
                        fontSize = 65.sp
                    )

                    Text(
                        text = "Табрик!",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$score / ${questions.size}",
                                fontSize = 42.sp,
                                color = purple,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                text = "Ҷавобҳои дуруст",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(25.dp))

                    Button(
                        onClick = {
                            questionIndex = 0
                            selectedAnswer = -1
                            score = 0
                            finished = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    ) {
                        Text("Аз нав оғоз кардан")
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "SONI QUIZ • VERSION 1.0",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
