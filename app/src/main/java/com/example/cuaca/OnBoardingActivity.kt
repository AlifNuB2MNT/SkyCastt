package com.example.cuaca

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnboardingScreen(
                onFinish = {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right) // efek slide
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(R.drawable.sun, "Selamat Datang", "Aplikasi Cuaca dan Forecast Cuaca!")
                1 -> OnboardingPage(R.drawable.cloud, "Lihat Cuaca", "Lihat bagaimana kondisi cuaca di beberapa kota")
                2 -> OnboardingPage(R.drawable.snow, "Forecast Cuaca", "Kamu juga bisa prediksi cuaca beberapa hari kedepan")
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage < 2) {
                Button(
                    onClick = { scope.launch { pagerState.scrollToPage(2) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF74C476))
                ) {
                    Text("Skip", color = Color.White)
                }
            }
            Button(
                onClick = {
                    if (pagerState.currentPage == 2) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF74C476))
            ) {
                Text(if (pagerState.currentPage == 2) "Mulai" else "Next", color = Color.White)
            }
        }
    }
}

@Composable
fun OnboardingPage(imageRes: Int, title: String, desc: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title)
        Spacer(modifier = Modifier.height(8.dp))
        Text(desc)
    }
}
