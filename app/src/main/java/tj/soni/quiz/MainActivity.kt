package tj.soni.quiz

import android.content.Context
import android.os.Bundle
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
private val Cream = Color(0xFFF7F3E8)
private val White = Color.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("islamic_quiz", Context.MODE_PRIVATE)

        setContent {
            MaterialTheme {
                var bestScore by remember {
                    mutableIntStateOf(prefs.getInt("best_score", 0))
                }
                var selectedCategory by remember { mutableIntStateOf(-1) }
                var showQuiz by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkGreen
                ) {
                    if (showQuiz && selectedCategory >= 0) {
                        val category = categories[selectedCategory]

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "☪ ${category.name}",
                                color = Gold,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(20.dp))
                            Text(
                                "Саволҳо омода мешаванд...",
                                color = White,
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { showQuiz = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold,
                                    contentColor = DarkGreen
                                )
                            ) {
                                Text("Бозгашт")
                            }
                        }
                    } else {
                        HomeScreen(
                            bestScore = bestScore,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it },
                            onStart = { showQuiz = true }
                        )
                    }
                }
            }
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
            .background(DarkGreen)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(25.dp))

        Text("☪", color = Gold, fontSize = 54.sp)

        Text(
            "Саволҳои Исломӣ",
            color = Gold,
            fontSize = 29.sp,
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
            color = Cream,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(28.dp))

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
                val category = categories[index]
                val selected = selectedCategory == index

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) Gold else CardGreen,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onCategorySelected(index) }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        category.name,
                        color = if (selected) DarkGreen else White,
                        fontSize = 17.sp,
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
            enabled = selectedCategory >= 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = DarkGreen,
                disabledContainerColor = Color.Gray
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

        Spacer(Modifier.height(12.dp))
    }
}
