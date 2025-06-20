package marinarodionova.english.telegram

import kotlinx.serialization.json.Json
import marinarodionova.english.telegram.entities.Response
import marinarodionova.english.telegram.entities.Update
import marinarodionova.english.trainer.LearnWordsTrainer
import marinarodionova.english.trainer.model.Question

const val STATISTIC_CALLBACK = "statistic_callback"
const val LEARN_WORDS_CALLBACK = "learn_words_callback"
const val COMEBACK_CALLBACK = "menu_callback"
const val RESET_CALLBACK = "reset_progress_callback"

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

const val TIMER_SLEEP_SECONDS: Long = 2000

const val START_COMMAND = "/start"
const val MENU_COMMAND = "menu"

fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val tgBotService = TelegramBotService(botToken, json)
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(TIMER_SLEEP_SECONDS)

        val response: Response = tgBotService.getUpdates(lastUpdateId) ?: continue
        println(response)

        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach {
            handleUpdate(it, tgBotService, trainers)
        }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    tgBotService: TelegramBotService,
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
            checkNextQuestionAndSend(trainer, tgBotService, chatId)
        }

        data == COMEBACK_CALLBACK -> {
            println(tgBotService.sendMenu(chatId))
        }

        data == RESET_CALLBACK -> {
            trainer.resetProgress()
            println(tgBotService.sendMessage(chatId, "Прогресс сброшен"))
        }

        data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && trainer.currentQuestion != null -> {
            val currentQuestion: Question = trainer.currentQuestion ?: return
            if (trainer.checkAnswer(data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt())) {
                println(tgBotService.sendMessage(chatId, "Верно!"))
            } else {
                println(
                    tgBotService.sendMessage(
                        chatId,
                        "Неправильно! ${currentQuestion.correctAnswer.word} -  " +
                                "${currentQuestion.correctAnswer.translate} "
                    )
                )
            }
            checkNextQuestionAndSend(trainer, tgBotService, chatId)
        }
    }

    val message = update.message?.text ?: return

    when (message.lowercase()) {
        START_COMMAND -> println(tgBotService.sendMenu(chatId))
        MENU_COMMAND -> println(tgBotService.sendMenu(chatId))
    }
}

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, tgBotService: TelegramBotService, chatId: Long) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        println(tgBotService.sendMessage(chatId, "Все слова выучены"))
    } else {
        println(tgBotService.sendQuestion(chatId, question))
    }
}