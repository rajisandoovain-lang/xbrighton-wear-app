package com.xbrighton.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        Text(
            text = "XBrighton",
            color = Color(0xFF0D1321),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Fashion + AI + Market",
            color = Color(0xFF5A6B8C),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

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
fun StatCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFFEFF6FF), androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
        Text(text = title, fontSize = 12.sp, color = Color(0xFF4B5563))
    }
}
