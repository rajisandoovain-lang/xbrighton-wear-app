package com.xbrighton.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppShell()
                }
            }
        }
    }
}

private const val BASE_URL = "http://10.0.2.2:3000"

@Composable
fun AppShell() {
    val context = LocalContext.current
    var signedInUser by remember { mutableStateOf(SecureTokenStore.getEmail(context)) }
    var currentScreen by remember { mutableStateOf(if (signedInUser != null) "dashboard" else "login") }

    when (currentScreen) {
        "login" -> LoginScreen(onSignedIn = {
            signedInUser = SecureTokenStore.getEmail(context)
            currentScreen = "dashboard"
        })
        "dashboard" -> DashboardScreen(
            userEmail = signedInUser ?: "",
            onLogout = {
                SecureTokenStore.clearSession(context)
                signedInUser = null
                currentScreen = "login"
            },
            onOpenCart = { currentScreen = "cart" },
            onOpenProducts = { currentScreen = "products" },
            onOpenProfile = { currentScreen = "profile" }
        )
        "profile" -> ProfileScreen(
            onBack = { currentScreen = "dashboard" },
            onLogout = {
                SecureTokenStore.clearSession(context)
                signedInUser = null
                currentScreen = "login"
            }
        )
        "cart" -> CartScreen(onBack = { currentScreen = "dashboard" })
        "products" -> ProductListScreen(onBack = { currentScreen = "dashboard" })
        else -> LoginScreen(onSignedIn = {
            signedInUser = SecureTokenStore.getEmail(context)
            currentScreen = "dashboard"
        })
    }
}

@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
    val client = remember { OkHttpClient() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Sign in to continue") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1321))
            .padding(22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("XBrighton", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text("Sign in to your account", color = Color(0xFFB9C7DE), modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Text(status, color = Color(0xFFBEE3FF), modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val result = performAuthRequest(client, endpoint = "/auth/login", email, password)
                    if (result == null) {
                        status = "Unable to sign in"
                    } else {
                        SecureTokenStore.saveSession(context, result.email, result.accessToken, result.refreshToken)
                        status = "Signed in as ${result.email}"
                        onSignedIn()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
        ) {
            Text("Sign In")
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    val result = performAuthRequest(client, endpoint = "/auth/register", email, password)
                    if (result == null) {
                        status = "Unable to create account"
                    } else {
                        SecureTokenStore.saveSession(context, result.email, result.accessToken, result.refreshToken)
                        status = "Account created for ${result.email}"
                        onSignedIn()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            Text("Create Account")
        }
    }
}

@Composable
fun DashboardScreen(userEmail: String, onLogout: () -> Unit, onOpenCart: () -> Unit, onOpenProducts: () -> Unit, onOpenProfile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D1321)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("XBrighton", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text("Welcome back, ${userEmail}", color = Color(0xFFBEE3FF))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(title = "Saved", value = "12")
            InfoCard(title = "Cart", value = "3")
        }

        Button(onClick = onOpenProducts, modifier = Modifier.fillMaxWidth()) {
            Text("Browse Products")
        }

        Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
            Text("Profile")
        }

        Button(onClick = onOpenCart, modifier = Modifier.fillMaxWidth()) {
            Text("Cart")
        }

        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Sign Out")
        }
    }
}

@Composable
fun ProductListScreen(onBack: () -> Unit) {
    val client = remember { OkHttpClient() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<Product>>(emptyList()) }
    var status by remember { mutableStateOf("Loading products...") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val list = fetchProducts(client, context)
            items = list
            status = if (list.isEmpty()) "No products available" else "${list.size} products found"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D1321)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Shop", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text(status, color = Color(0xFFBEE3FF))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { product ->
                ProductCard(product = product)
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to dashboard")
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    val context = LocalContext.current
    val client = remember { OkHttpClient() }
    val coroutineScope = rememberCoroutineScope()
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(Color(0xFF1A2437), RoundedCornerShape(14.dp))
            .padding(16.dp)
            .fillMaxWidth()
            .clickable {
                // product detail can open later
            }
    ) {
        Text(product.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
        Text(product.category, color = Color(0xFFBEE3FF))
        Text("${product.description}", color = Color.White, modifier = Modifier.padding(top = 6.dp))
        Text("$${product.price}", color = Color.White, modifier = Modifier.padding(top = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        addToCart(client, context, product)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add to cart")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = saveFavorite(client, context, product.id)
                        saved = success
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (saved) "Saved" else "Save")
            }
        }
    }
}

@Composable
fun ProfileScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val client = remember { OkHttpClient() }
    val coroutineScope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<ProfileResult?>(null) }
    var status by remember { mutableStateOf("Loading profile...") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val result = getProfile(client, context)
            profile = result
            status = if (result != null) "Profile loaded" else "Unable to load profile"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D1321)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Profile", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text(status, color = Color(0xFFBEE3FF))
        if (profile != null) {
            Text("ID: ${profile!!.id}", color = Color.White)
            Text("Email: ${profile!!.email}", color = Color.White)
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        Button(onClick = {
            coroutineScope.launch {
                val success = logoutUser(client, context)
                if (success) {
                    SecureTokenStore.clearSession(context)
                    onLogout()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Sign Out") }
    }
}

@Composable
fun CartScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val client = remember { OkHttpClient() }
    val coroutineScope = rememberCoroutineScope()
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            cartItems = getCart(client, context)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D1321)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Cart", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        if (cartItems.isEmpty()) {
            Text("Your cart is empty.", color = Color(0xFFBEE3FF))
        } else {
            cartItems.forEach { item ->
                Text("${item.name} × ${item.quantity} — $${item.price}", color = Color.White)
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    Column(
        modifier = Modifier.background(Color(0xFF1A2437), RoundedCornerShape(14.dp)).padding(18.dp).padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge)
        Text(title, color = Color(0xFFBEE3FF))
    }
}

private suspend fun fetchProducts(client: OkHttpClient, context: Context): List<Product> = withContext(Dispatchers.IO) {
    try {
        val accessToken = SecureTokenStore.getAccessToken(context)
        val request = Request.Builder().url(BASE_URL + "/products").apply {
            if (accessToken != null) addHeader("Authorization", "Bearer $accessToken")
        }.build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            val body = response.body?.string() ?: return@withContext emptyList()
            val obj = JSONObject(body)
            val arr = obj.optJSONArray("products") ?: return@withContext emptyList()
            val result = mutableListOf<Product>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                result.add(
                    Product(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        category = item.optString("category"),
                        description = item.optString("description"),
                        price = item.optDouble("price", 0.0),
                        color = item.optString("color"),
                        size = item.optString("size")
                    )
                )
            }
            result
        }
    } catch (e: Exception) {
        e.printStackTrace(); emptyList()
    }
}

private suspend fun addToCart(client: OkHttpClient, context: Context, product: Product): Boolean = withContext(Dispatchers.IO) {
    val accessToken = SecureTokenStore.getAccessToken(context) ?: return@withContext false
    val json = JSONObject()
        .put("productId", product.id)
        .put("name", product.name)
        .put("price", product.price)
        .put("quantity", 1)
        .toString()

    try {
        val request = Request.Builder()
            .url(BASE_URL + "/cart")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).execute().use { response -> response.isSuccessful }
    } catch (e: Exception) {
        e.printStackTrace(); false
    }
}

private suspend fun saveFavorite(client: OkHttpClient, context: Context, productId: String): Boolean = withContext(Dispatchers.IO) {
    val accessToken = SecureTokenStore.getAccessToken(context) ?: return@withContext false
    val json = JSONObject().put("productId", productId).toString()

    try {
        val request = Request.Builder()
            .url(BASE_URL + "/favorites")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).execute().use { response -> response.isSuccessful }
    } catch (e: Exception) {
        e.printStackTrace(); false
    }
}

private suspend fun performAuthRequest(client: OkHttpClient, endpoint: String, email: String, password: String): AuthResult? = withContext(Dispatchers.IO) {
    try {
        val json = JSONObject().put("email", email).put("password", password).toString()
        val request = Request.Builder().url(BASE_URL + endpoint).post(json.toRequestBody("application/json; charset=utf-8".toMediaType())).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: return@withContext null
            if (!response.isSuccessful) return@withContext null
            val obj = JSONObject(body)
            val user = obj.optJSONObject("user") ?: return@withContext null
            val accessToken = obj.optString("accessToken")
            val refreshToken = obj.optString("refreshToken")
            if (accessToken.isBlank() || refreshToken.isBlank()) return@withContext null
            AuthResult(user.optString("email", email), accessToken, refreshToken)
        }
    } catch (e: Exception) {
        e.printStackTrace(); null
    }
}

private suspend fun refreshTokenIfNeeded(client: OkHttpClient, context: Context): Boolean = withContext(Dispatchers.IO) {
    val refreshToken = SecureTokenStore.getRefreshToken(context) ?: return@withContext false
    val json = JSONObject().put("refreshToken", refreshToken).toString()
    try {
        val request = Request.Builder().url(BASE_URL + "/auth/refresh").post(json.toRequestBody("application/json; charset=utf-8".toMediaType())).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext false
            val body = response.body?.string() ?: return@withContext false
            val obj = JSONObject(body)
            val newAccess = obj.optString("accessToken")
            val newRefresh = obj.optString("refreshToken")
            val email = obj.optJSONObject("user")?.optString("email") ?: SecureTokenStore.getEmail(context) ?: ""
            if (newAccess.isBlank() || newRefresh.isBlank()) return@withContext false
            SecureTokenStore.saveSession(context, email, newAccess, newRefresh)
            true
        }
    } catch (e: Exception) {
        e.printStackTrace(); false
    }
}

private suspend fun getProfile(client: OkHttpClient, context: Context): ProfileResult? = withContext(Dispatchers.IO) {
    val accessToken = SecureTokenStore.getAccessToken(context) ?: return@withContext null
    val request = Request.Builder().url(BASE_URL + "/profile").addHeader("Authorization", "Bearer $accessToken").build()

    try {
        client.newCall(request).execute().use { response ->
            if (response.code == 401) {
                val refreshed = refreshTokenIfNeeded(client, context)
                if (!refreshed) return@withContext null
                val token = SecureTokenStore.getAccessToken(context) ?: return@withContext null
                val retry = Request.Builder().url(BASE_URL + "/profile").addHeader("Authorization", "Bearer $token").build()
                client.newCall(retry).execute().use { retryResponse ->
                    if (!retryResponse.isSuccessful) return@withContext null
                    val body = retryResponse.body?.string() ?: return@withContext null
                    val obj = JSONObject(body)
                    val user = obj.optJSONObject("user") ?: return@withContext null
                    return@withContext ProfileResult(user.optInt("id", 0), user.optString("email", SecureTokenStore.getEmail(context) ?: ""))
                }
            }
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val obj = JSONObject(body)
            val user = obj.optJSONObject("user") ?: return@withContext null
            ProfileResult(user.optInt("id", 0), user.optString("email", SecureTokenStore.getEmail(context) ?: ""))
        }
    } catch (e: Exception) {
        e.printStackTrace(); null
    }
}

private suspend fun getCart(client: OkHttpClient, context: Context): List<CartItem> = withContext(Dispatchers.IO) {
    val accessToken = SecureTokenStore.getAccessToken(context) ?: return@withContext emptyList()
    val request = Request.Builder().url(BASE_URL + "/cart").addHeader("Authorization", "Bearer $accessToken").build()

    try {
        client.newCall(request).execute().use { response ->
            if (response.code == 401) {
                val refreshed = refreshTokenIfNeeded(client, context)
                if (!refreshed) return@withContext emptyList()
                val newToken = SecureTokenStore.getAccessToken(context) ?: return@withContext emptyList()
                val retry = Request.Builder().url(BASE_URL + "/cart").addHeader("Authorization", "Bearer $newToken").build()
                client.newCall(retry).execute().use { retryResponse ->
                    if (!retryResponse.isSuccessful) return@withContext emptyList()
                    val body = retryResponse.body?.string() ?: return@withContext emptyList()
                    return@withContext parseCartList(body)
                }
            }
            if (!response.isSuccessful) return@withContext emptyList()
            val body = response.body?.string() ?: return@withContext emptyList()
            parseCartList(body)
        }
    } catch (e: Exception) {
        e.printStackTrace(); emptyList()
    }
}

private fun parseCartList(body: String): List<CartItem> {
    val obj = JSONObject(body)
    val arr = obj.optJSONArray("items") ?: return emptyList()
    val output = mutableListOf<CartItem>()
    for (i in 0 until arr.length()) {
        val item = arr.getJSONObject(i)
        output.add(
            CartItem(
                id = item.optInt("id"),
                productId = item.optString("product_id"),
                name = item.optString("name"),
                price = item.optDouble("price", 0.0),
                quantity = item.optInt("quantity", 1)
            )
        )
    }
    return output
}

private suspend fun logoutUser(client: OkHttpClient, context: Context): Boolean = withContext(Dispatchers.IO) {
    val accessToken = SecureTokenStore.getAccessToken(context) ?: return@withContext false
    val refreshToken = SecureTokenStore.getRefreshToken(context)
    try {
        val json = JSONObject().put("refreshToken", refreshToken ?: "").toString()
        val request = Request.Builder().url(BASE_URL + "/auth/logout").post(json.toRequestBody("application/json; charset=utf-8".toMediaType())).addHeader("Authorization", "Bearer $accessToken").build()
        client.newCall(request).execute().use { response -> response.isSuccessful }
    } catch (e: Exception) {
        e.printStackTrace(); false
    }
}

data class AuthResult(val email: String, val accessToken: String, val refreshToken: String)
data class ProfileResult(val id: Int, val email: String)
data class CartItem(val id: Int, val productId: String, val name: String, val price: Double, val quantity: Int)
data class Product(val id: String, val name: String, val category: String, val description: String, val price: Double, val color: String, val size: String)
