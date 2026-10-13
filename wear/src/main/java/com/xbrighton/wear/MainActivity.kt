package com.xbrighton.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        var currentScreen by remember { androidx.compose.runtime.mutableStateOf("home") }

        // Fetch products once when the composable is composed
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
                // When using an Android emulator, use 10.0.2.2 to reach the host machine
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
            Text(
                text = "XBrighton",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
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
            Text(
                text = "★",
                color = Color(0xFFFFD166),
                fontSize = 12.sp
            )
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
        Text("AI Teacher", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        Text("Market", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

// DTOs for backend parsing
data class ProductsWrapper(val products: List<BackendProduct>)

data class BackendProduct(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String
)
