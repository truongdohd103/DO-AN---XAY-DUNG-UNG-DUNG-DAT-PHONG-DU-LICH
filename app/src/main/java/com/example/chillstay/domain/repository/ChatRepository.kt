package com.example.chillstay.domain.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ChatRequest(
    val message: String,
    val session_id: String
)

@Serializable
data class ChatResponse(
    val response: String,
    val session_id: String,
    val status: String
)

@Serializable
data class ClearChatRequest(
    val session_id: String
)

@Serializable
data class ClearChatResponse(
    val message: String,
    val status: String
)

class ChatRepository(
    private val httpClient: HttpClient
) {
    //Url ngrok sẽ thay đổi mỗi khi restart ngrok, cần cập nhật lại cho đúng
    private val baseUrl = "https://noncosmic-pablo-unhumiliated.ngrok-free.dev/api"

    // Session ID duy nhất cho mỗi user
    private val sessionId: String = UUID.randomUUID().toString()

    suspend fun sendMessage(message: String): Result<String> {
        return try {
            // Dùng endpoint /chat/fast thay vì /chat để có tốc độ nhanh hơn (RAG trực tiếp, không qua agent)
            val httpResponse: HttpResponse = httpClient.post("$baseUrl/chat") {
                contentType(ContentType.Application.Json)
                setBody(ChatRequest(
                    message = message,
                    session_id = sessionId
                ))
            }
            
            // Kiểm tra status code
            if (httpResponse.status.isSuccess()) {
                val response: ChatResponse = httpResponse.body()
                if (response.status == "success") {
                    Result.success(response.response)
                } else {
                    Result.failure(Exception("Server trả về status: ${response.status}"))
                }
            } else {
                val errorText = httpResponse.bodyAsText()
                Result.failure(Exception("Server error (${httpResponse.status.value}): $errorText"))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng và địa chỉ server."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Kết nối timeout. Server có thể đang xử lý, vui lòng thử lại sau."))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("Không thể kết nối đến server tại $baseUrl. Vui lòng kiểm tra server có đang chạy không."))
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi mạng: ${e.message ?: e.javaClass.simpleName}"))
        }
    }

    suspend fun clearChatHistory(): Result<Unit> {
        return try {
            val httpResponse: HttpResponse = httpClient.post("$baseUrl/chat/clear") {
                contentType(ContentType.Application.Json)
                setBody(ClearChatRequest(session_id = sessionId))
            }
            
            if (httpResponse.status.isSuccess()) {
                val response: ClearChatResponse = httpResponse.body()
                if (response.status == "success") {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to clear chat: ${response.status}"))
                }
            } else {
                Result.failure(Exception("Server error (${httpResponse.status.value})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message ?: e.javaClass.simpleName}"))
        }
    }
}