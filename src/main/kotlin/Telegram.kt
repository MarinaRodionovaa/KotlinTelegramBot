package org.example

const val STATISTIC_CALLBACK = "statistic_callback"
const val LEARN_WORDS_CALLBACK = "learn_words_callback"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val COMEBACK_CALLBACK = "menu_callback"
const val TIMER_SLEEP_SECONDS: Long = 2000

const val START_COMMAND = "/start"
const val MENU_COMMAND = "menu"

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, tgBotService: TelegramBotService, chatId: Int) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        println(tgBotService.sendMessage(chatId, "Все слова выучены"))
    } else {
        println(tgBotService.sendQuestion(chatId, question))
    }
}

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId: Int = 0

    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = """"chat"\s*:\s*\{\s*"id"\s*:\s*(\d+)""".toRegex()
    val dataRegex: Regex = "\"data\":\\s*\"([^\"]+)\"".toRegex()

    val tgBotService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(TIMER_SLEEP_SECONDS)
        val updates: String = tgBotService.getUpdates(updateId)
        println(updates)

        val updateIdString = updateIdRegex.find(updates)?.groups?.get(1)?.value ?: continue
        updateId = updateIdString.toInt() + 1

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt() ?: continue

        val data = dataRegex.find(updates)?.groups?.get(1)?.value

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
                checkNextQuestionAndSend(trainer, tgBotService, chatId)
            }

        }


        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: continue

        when (text.lowercase()) {
            START_COMMAND -> println(tgBotService.sendMenu(chatId))
            // TODO: Исправить баг, когда бот отправляет апдейт со словом из словаря hello отправляет его же в ответ так же происходит и со словом меню
            // TODO: Наверное стоит либо убрать эти команды либо добавить / перед, либо добавить проверку от кого апдейт
            MENU_COMMAND -> println(tgBotService.sendMenu(chatId))
        }
    }
}