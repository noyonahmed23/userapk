package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class StateLoadResult(val successful: Boolean, val state: RemoteAppState?)

class ApiStateClient(
    private val baseUrl: String = BuildConfig.KHELO_API_BASE_URL
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(RemoteAppState::class.java)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun loadResult(): StateLoadResult {
        return runCatching {
            val request = Request.Builder()
                .url("${baseUrl.trimEnd('/')}/api.php?action=state")
                .header("X-Khelo-App-Key", BuildConfig.KHELO_API_KEY)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    StateLoadResult(successful = false, state = null)
                } else {
                    val body = response.body?.string()
                    if (body.isNullOrBlank()) {
                        StateLoadResult(successful = false, state = null)
                    } else {
                        val envelope = moshi.adapter(StateEnvelope::class.java).fromJson(body)
                        if (envelope?.ok == true) {
                            StateLoadResult(successful = true, state = envelope.state)
                        } else {
                            StateLoadResult(successful = false, state = null)
                        }
                    }
                }
            }
        }.getOrElse { StateLoadResult(successful = false, state = null) }
    }

    fun save(state: RemoteAppState): Boolean {
        return runCatching {
            val json = adapter.toJson(state)
            val request = Request.Builder()
                .url("${baseUrl.trimEnd('/')}/api.php?action=state")
                .header("X-Khelo-App-Key", BuildConfig.KHELO_API_KEY)
                .post(json.toRequestBody(mediaType))
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }

    private data class StateEnvelope(
        val ok: Boolean = false,
        val state: RemoteAppState? = null,
        val error: String? = null
    )
}
