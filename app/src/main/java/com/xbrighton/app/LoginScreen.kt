package com.xbrighton.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
                    LoginScreen()
                }
            }
        }
    }
}

private const val BASE_URL = "http://10.0.2.2:3000"

@Composable
fun LoginScreen() {
    val client = remember { OkHttpClient() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Sign in to continue") }
    var signedInUser by remember { mutableStateOf(SecureTokenStore.getEmail(context)) }

    if (signedInUser != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1321))
                .padding(22.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "XBrighton",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Signed in as $signedInUser",
                color = Color(0xFFBEE3FF),
                modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val refreshed = refreshTokenIfNeeded(client, context)
                        status = if (refreshed) {
                            "Session refreshed successfully"
                        } else {
                            "Sign in again"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Session")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = logoutUser(client, context)
                        if (success) {
                            SecureTokenStore.clearSession(context)
                            signedInUser = null
                            status = "Signed out"
                        } else {
                            status = "Unable to sign out"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text("Sign Out")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1321))
            .padding(22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "XBrighton",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Sign in to your account",
            color = Color(0xFFB9C7DE),
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

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

        Text(
            text = status,
            color = Color(0xFFBEE3FF),
            modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    val result = performAuthRequest(client, endpoint = "/auth/login", email, password)
                    if (result == null) {
                        status = "Unable to sign in"
                    } else {
                        SecureTokenStore.saveSession(context, result.email, result.accessToken, result.refreshToken)
                        signedInUser = result.email
                        status = "Signed in as ${result.email}"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
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
                        signedInUser = result.email
                        status = "Account created for ${result.email}"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Text("Create Account")
        }
    }
}

private suspend fun performAuthRequest(
    client: OkHttpClient,
    endpoint: String,
    email: String,
    password: String
): AuthResult? = withContext(Dispatchers.IO) {
    try {
        val json = JSONObject()
            .put("email", email)
            .put("password", password)
            .toString()

        val request = Request.Builder()
            .url(BASE_URL + endpoint)
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: return@withContext null
            if (!response.isSuccessful) {
                return@withContext null
            }

            val obj = JSONObject(body)
            val emailValue = obj.optJSONObject("user")?.optString("email") ?: email
            val accessToken = obj.optString("accessToken")
            val refreshToken = obj.optString("refreshToken")

            if (accessToken.isBlank() || refreshToken.isBlank()) {
                return@withContext null
            }

            AuthResult(email = emailValue, accessToken = accessToken, refreshToken = refreshToken)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun refreshTokenIfNeeded(client: OkHttpClient, context: android.content.Context): Boolean = withContext(Dispatchers.IO) {
    val refreshToken = SecureTokenStore.getRefreshToken(context) ?: return@withContext false
    val json = JSONObject().put("refreshToken", refreshToken).toString()

    try {
        val request = Request.Builder()
            .url(BASE_URL + "/auth/refresh")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext false
            }

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
        e.printStackTrace()
        false
    }
}

private suspend fun logoutUser(client: OkHttpClient, context: android.content.Context): Boolean = withContext(Dispatchers.IO) {
    val accessToken = SecureTokenStore.getAccessToken(context) ?: return@withContext false
    val refreshToken = SecureTokenStore.getRefreshToken(context)

    try {
        val json = JSONObject()
            .put("refreshToken", refreshToken ?: "")
            .toString()

        val request = Request.Builder()
            .url(BASE_URL + "/auth/logout")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

data class AuthResult(
    val email: String,
    val accessToken: String,
    val refreshToken: String
)
