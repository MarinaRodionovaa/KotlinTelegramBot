package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val STATISTIC_CALLBACK = "statistic_callback"
const val LEARN_WORDS_CALLBACK = "learn_words_callback"
const val COMEBACK_CALLBACK = "menu_callback"
const val RESET_CALLBACK = "reset_progress_callback"

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

const val TIMER_SLEEP_SECONDS: Long = 2000

const val START_COMMAND = "/start"
const val MENU_COMMAND = "menu"

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun checkNextQuestionAndSend(json: Json, trainer: LearnWordsTrainer, tgBotService: TelegramBotService, chatId: Long) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        println(tgBotService.sendMessage(chatId, "Все слова выучены"))
    } else {
        println(tgBotService.sendQuestion(chatId, question))
    }
}

fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val tgBotService = TelegramBotService(botToken, json)
    val trainer = LearnWordsTrainer()
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(TIMER_SLEEP_SECONDS)

        val responseString: String = tgBotService.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)

        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach {
            handleUpdate(it, tgBotService, json, trainers)
        }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    tgBotService: TelegramBotService,
    json: Json,
    trainers: HashMap<Long, LearnWordsTrainer>
) {

    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    when {
        data == STATISTIC_CALLBACK -> {
            val statistic = trainer.getStatistics()
            val statisticString = "Выучено ${statistic.learned} из ${statistic.total} слов | ${statistic.percent}%"
            println(tgBotService.sendMessage(chatId, statisticString))
        }

        data == LEARN_WORDS_CALLBACK -> {
            checkNextQuestionAndSend(json, trainer, tgBotService, chatId)
        }

        data == COMEBACK_CALLBACK -> {
            println(tgBotService.sendMenu(chatId))
        }

        data == RESET_CALLBACK -> {
            trainer.resetProgress()
            println(tgBotService.sendMessage(chatId, "Прогресс сброшен"))
        }

        data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && trainer.currentQuestion != null -> {
            if (trainer.checkAnswer(data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt())) {
                println(tgBotService.sendMessage(chatId, "Верно!"))
            } else {
                println(
                    tgBotService.sendMessage(
                        chatId,
                        "Неправильно! ${trainer.currentQuestion!!.correctAnswer.word} -  ${trainer.currentQuestion!!.correctAnswer.translate} "
                    )
                )
            }
            checkNextQuestionAndSend(json, trainer, tgBotService, chatId)
        }
    }

    val message = update.message?.text ?: return

    when (message.lowercase()) {
        START_COMMAND -> println(tgBotService.sendMenu(chatId))
        MENU_COMMAND -> println(tgBotService.sendMenu(chatId))
    }

}
