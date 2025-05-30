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

    fun sendQuestion(chatId: Int, question: Question): String {
        val variants = question.variants.mapIndexed { index: Int, word: Word ->
            """{
                    "text":"${index + 1} - ${word.translate}",
                    "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${index}"
                }""".trimIndent()
        }.joinToString(",\n")

        val sendQuestionBody = """
            {
                "chat_id": $chatId,
                "text": "${question.correctAnswer.word}",
                "reply_markup": {
                    "inline_keyboard":[
                        [
                            $variants
                        ],
                        [
                            {
                                "text": "Основное меню",
                                "callback_data": "$COMEBACK_CALLBACK"
                            }
                        ]                     
                    ]
                }
            }
        """.trimIndent()
        return sendPostJsonMessage(sendQuestionBody)
    }

    fun sendMenu(chatId: Int): String {
        val urlSendMessage = "$TG_URL$botToken/sendMessage"
        val sendMenuBody = """
            {
              "chat_id": $chatId,
              "text": "Основное меню",
              "reply_markup": {
                "inline_keyboard": [
                  [
                    {
                      "text": "Изучить слова",
                      "callback_data": "$LEARN_WORDS_CALLBACK"
                    },
                    {
                      "text": "Статистика",
                      "callback_data": "$STATISTIC_CALLBACK"
                    }
                  ]
                ]
              }
            }
        """.trimIndent()
        return sendPostJsonMessage(sendMenuBody)
    }
}