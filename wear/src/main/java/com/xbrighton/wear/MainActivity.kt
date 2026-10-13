package com.xbrighton.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {
    private val client = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                XBrightonAppNav()
            }
        }
    }

    @Composable
    fun XBrightonAppNav() {
        val productsState = remember { mutableStateListOf<Product>() }
        var currentScreen by remember { mutableStateOf("home") }

        androidx.compose.runtime.LaunchedEffect(Unit) {
            fetchProducts()?.let { remote ->
                productsState.clear()
                productsState.addAll(remote)
            }
        }

        when (currentScreen) {
            "home" -> HomeScreen(productsState, onProductClick = { currentScreen = "detail" })
            "detail" -> DetailScreen(onBack = { currentScreen = "home" }, onAiClick = { currentScreen = "ai" }, onMarketClick = { currentScreen = "market" })
            "ai" -> AiScreen(onBack = { currentScreen = "detail" })
            "market" -> MarketScreen(onBack = { currentScreen = "detail" })
            else -> HomeScreen(productsState, onProductClick = { currentScreen = "detail" })
        }
    }

    private suspend fun fetchProducts(): List<Product>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "http://10.0.2.2:3000/products"
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { resp ->
                    val body = resp.body?.string() ?: return@withContext null
                    val wrapper = gson.fromJson(body, ProductsWrapper::class.java)
                    wrapper.products.map { p ->
                        Product(p.name, p.category, "\$${p.price}", p.description)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

@Composable
fun HomeScreen(products: List<Product>, onProductClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1321))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            BrandHeader(title = "Clothes")
        }
        item {
            Text(
                text = "Shop • AI • Market",
                color = Color(0xFF9EC1FF),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        items(products) { product ->
            ProductCard(product, onProductClick)
        }
    }
}

@Composable
fun BrandHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        XBrightonLogo(modifier = Modifier.size(42.dp))
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = "XBrighton",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = Color(0xFF9EC1FF),
                fontSize = 10.sp
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
fun ProductCard(product: Product, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A2437), RoundedCornerShape(18.dp))
            .padding(12.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.category,
                color = Color(0xFF9EC1FF),
                fontSize = 10.sp
            )
            XBrightonLogo(modifier = Modifier.size(18.dp))
        }
        Text(
            text = product.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = product.description,
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = product.price,
            color = Color(0xFF7AE582),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun DetailScreen(onBack: () -> Unit, onAiClick: () -> Unit, onMarketClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1321))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BrandHeader(title = "Clothes")
        Text("Aurora Street Jacket", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Stone Grey • M • $189.00", color = Color(0xFF9EC1FF), fontSize = 11.sp)
        Text("Premium streetwear blend with a lightweight structure and elevated layered look.", color = Color(0xFFDDE7F7), fontSize = 10.sp)

        Button(onClick = onAiClick, modifier = Modifier.fillMaxWidth()) {
            Text("AI Teacher")
        }

        Button(onClick = onMarketClick, modifier = Modifier.fillMaxWidth()) {
            Text("Market")
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun AiScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BrandHeader(title = "AI")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A2437), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Text("Pair this with the Galaxy Runner X and slim dark layers for a premium street look.", color = Color(0xFFE2E8F0), fontSize = 10.sp)
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun MarketScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BrandHeader(title = "Market")
        Ticker("XBT", "68420.12", "+1.84%")
        Ticker("AAPL", "225.91", "-0.42%")
        Ticker("NVDA", "118.54", "+2.14%")
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun Ticker(symbol: String, price: String, change: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(symbol, color = Color.White, fontSize = 12.sp)
        Text(price, color = Color.White, fontSize = 12.sp)
        Text(change, color = if (change.startsWith("+")) Color(0xFF7AE582) else Color(0xFFFF7B7B), fontSize = 12.sp)
    }
}

data class Product(
    val name: String,
    val category: String,
    val price: String,
    val description: String
)

data class ProductsWrapper(val products: List<BackendProduct>)

data class BackendProduct(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String
)
