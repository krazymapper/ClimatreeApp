package com.example.touuri.data

import android.content.Context
import android.content.SharedPreferences
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class OsmAuthService(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("osm_auth", Context.MODE_PRIVATE)
    private val client = OkHttpClient()
    private val baseUrl = "https://www.openstreetmap.org"

    fun isAuthenticated(): Boolean {
        return prefs.getString("oauth_token", null) != null
    }

    fun getOAuthToken(): String? {
        return prefs.getString("oauth_token", null)
    }

    fun login(username: String, password: String, callback: (Boolean, String?) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("$baseUrl/oauth2/token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "{}")
                    val token = json.optString("access_token")
                    if (token.isNotEmpty()) {
                        prefs.edit().putString("oauth_token", token).apply()
                        callback(true, null)
                    } else {
                        callback(false, "Token not found in response")
                    }
                } else {
                    callback(false, "Authentication failed: ${response.code}")
                }
            }
        })
    }

    fun logout() {
        prefs.edit().remove("oauth_token").apply()
    }

    fun uploadTreeData(treeData: TreeData, callback: (Boolean, String?) -> Unit) {
        val token = getOAuthToken() ?: run {
            callback(false, "Not authenticated")
            return
        }

        val changeset = JSONObject().apply {
            put("type", "node")
            put("lat", treeData.latitude)
            put("lon", treeData.longitude)
            put("tags", JSONObject().apply {
                put("natural", "tree")
                put("species", treeData.species)
                treeData.height?.let { put("height", it) }
                treeData.diameter?.let { put("diameter", it) }
                put("health_status", treeData.healthStatus.name)
                treeData.age?.let { put("age", it) }
                if (treeData.notes.isNotEmpty()) put("note", treeData.notes)
                treeData.imageUrl?.let { put("image", it) }
            })
        }

        val requestBody = changeset.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/api/0.6/changeset/create")
            .header("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "{}")
                    val osmId = json.optString("id")
                    callback(true, osmId)
                } else {
                    callback(false, "Upload failed: ${response.code}")
                }
            }
        })
    }
} 