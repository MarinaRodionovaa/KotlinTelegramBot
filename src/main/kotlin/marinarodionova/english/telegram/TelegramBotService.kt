package marinarodionova.english.telegram

import kotlinx.serialization.json.Json
import marinarodionova.english.telegram.entities.InlineKeyboard
import marinarodionova.english.telegram.entities.ReplyMarkup
import marinarodionova.english.telegram.entities.Response
import marinarodionova.english.telegram.entities.SendMessageRequest
import marinarodionova.english.trainer.model.Question
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val botToken: String,
    val json: Json,
) {
    private val client = HttpClient.newBuilder().build()

    companion object {
        private const val TG_URL = "https://api.telegram.org/bot"
    }

    private fun getResponseFromUrl(url: String): String? {
        val requestGet = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val responseResult = kotlin.runCatching {
            client.send(requestGet, HttpResponse.BodyHandlers.ofString())
        }
        return responseResult.getOrNull()?.body()
    }

    private fun sendPostJsonMessage(messageBody: String): String {
        val urlSendMessage = "$TG_URL$botToken/sendMessage"
        try {
            val requestGet = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(messageBody))
                .build()
            val responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString())
            return responseGet.body()
        } catch (e: Exception) {
            println(e.message)
            return "не удалось отправить сообщение"
        }
    }

    fun getUpdates(updateId: Long): Response? {
        val urlGetUpdates = "$TG_URL$botToken/getUpdates?offset=$updateId"
        val responseString = getResponseFromUrl(urlGetUpdates)
        val response: Response? = responseString?.let { json.decodeFromString(it) }
        return response
    }

    fun sendMessage(chatId: Long, text: String): String {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text
        )
        val requestBodyString = json.encodeToString(requestBody)
        return sendPostJsonMessage(requestBodyString)
    }

    fun sendQuestion(chatId: Long, question: Question): String {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.word,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                })
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        return sendPostJsonMessage(requestBodyString)
    }

    fun sendMenu(chatId: Long): String {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(LEARN_WORDS_CALLBACK, "Изучить слова"),
                        InlineKeyboard(STATISTIC_CALLBACK, "Статистика"),
                    ),
                    listOf(
                        InlineKeyboard(RESET_CALLBACK, "Сбросить прогресс"),
                    )


                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        return sendPostJsonMessage(requestBodyString)
    }
}