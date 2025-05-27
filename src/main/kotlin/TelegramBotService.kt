package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {
    private val client = HttpClient.newBuilder().build()

    companion object {
        private const val TG_URL = "https://api.telegram.org/bot"
    }

    private fun getResponseFromUrl(url: String): String {
        val requestGet = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString())
        return responseGet.body()
    }

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TG_URL$botToken/getUpdates?offset=$updateId"
        return getResponseFromUrl(urlGetUpdates)
    }

    fun sendMessage(chatId: Int, text: String): String {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val urlSendMessage = "$TG_URL$botToken/sendMessage?text=$encodedText&chat_id=$chatId"
        try {
            return getResponseFromUrl(urlSendMessage)
        } catch (e: Exception) {
            println(e.message)
            return "не удалось отправить сообщение"
        }
    }
}