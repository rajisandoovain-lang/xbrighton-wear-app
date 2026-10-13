package com.xbrighton.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PhoneCompanionUI()
                }
            }
        }
    }
}

@Composable
fun PhoneCompanionUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        BrandHeader()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(title = "New arrivals", value = "24")
            StatCard(title = "Saved", value = "08")
        }

        Text(
            text = "Featured: Aurora Street Jacket",
            color = Color(0xFF2B2D42),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = "AI suggestion: pair with sleek runners for a premium street look.",
            color = Color(0xFF4A5568),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun BrandHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        XBrightonLogo(modifier = Modifier.size(52.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = "XBrighton",
                color = Color(0xFF0D1321),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fashion + AI + Market",
                color = Color(0xFF5A6B8C),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun XBrightonLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val orange = Color(0xFFF29A1F)
        val white = Color(0xFFFFFFFF)

        val leftWing = Path().apply {
            moveTo(0f, h * 0.70f)
            lineTo(w * 0.10f, h * 0.22f)
            lineTo(w * 0.45f, h * 0.22f)
            lineTo(w * 0.26f, h * 0.70f)
            lineTo(w * 0.12f, h * 0.92f)
            close()
        }

        val rightWing = Path().apply {
            moveTo(w, h * 0.70f)
            lineTo(w * 0.90f, h * 0.22f)
            lineTo(w * 0.55f, h * 0.22f)
            lineTo(w * 0.74f, h * 0.70f)
            lineTo(w * 0.88f, h * 0.92f)
            close()
        }

        val centerTop = Path().apply {
            moveTo(w * 0.50f, 0f)
            lineTo(w * 0.76f, h * 0.28f)
            lineTo(w * 0.50f, h * 0.47f)
            lineTo(w * 0.24f, h * 0.28f)
            close()
        }

        val centerDiamond = Path().apply {
            moveTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.69f, h * 0.38f)
            lineTo(w * 0.50f, h * 0.72f)
            lineTo(w * 0.31f, h * 0.38f)
            close()
        }

        val cutout = Path().apply {
            moveTo(w * 0.50f, h * 0.32f)
            lineTo(w * 0.60f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.58f)
            lineTo(w * 0.40f, h * 0.46f)
            close()
        }

        drawPath(leftWing, color = orange, style = Fill)
        drawPath(rightWing, color = orange, style = Fill)
        drawPath(centerTop, color = orange, style = Fill)
        drawPath(centerDiamond, color = orange, style = Fill)
        drawPath(cutout, color = white, style = Fill)
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFFEFF6FF), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
        Text(text = title, fontSize = 12.sp, color = Color(0xFF4B5563))
    }
}
