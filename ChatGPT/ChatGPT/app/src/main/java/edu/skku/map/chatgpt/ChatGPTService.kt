import okhttp3.*
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ChatGPTService {
    companion object {
        private const val API_KEY = "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"
        private const val API_URL = "https://api.openai.com/v1/engines/davinci-codex/completions"

        interface ChatGPTCallback {
            fun onResponse(response: String)
            fun onFailure(error: String)
        }

        fun generateResponse(prompt: String, callback: ChatGPTCallback) {
            val client = OkHttpClient()

            val input = JsonObject().apply {
                addProperty("prompt", prompt)
                addProperty("max_tokens", 50)
                addProperty("temperature", 0.5)
                addProperty("top_p", 1)
            }

            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            val body = input.toString().toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer $API_KEY")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailure(e.message ?: "Unknown error")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        response.body?.string()?.let { jsonResponse ->
                            val json = Gson().fromJson(jsonResponse, JsonObject::class.java)
                            val chatGPTResponse = json.getAsJsonArray("choices").get(0).asJsonObject["text"].asString
                            callback.onResponse(chatGPTResponse)
                        }
                    } else {
                        callback.onFailure(response.message)
                    }
                }
            })
        }
    }
}