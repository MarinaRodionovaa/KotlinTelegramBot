package org.example

const val STATISTIC_CALLBACK = "statistic_callback"
const val LEARN_WORDS_CALLBACK = "learn_words_callback"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val COMEBACK_CALLBACK = "menu_callback"

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, tgBotService: TelegramBotService, chatId: Int): Word? {
    val question = trainer.getNextQuestion()
    if (question == null) {
        println(tgBotService.sendMessage(chatId, "Все слова выучены"))
        return null
    } else {
        println(tgBotService.sendQuestion(chatId, question))
        return question.correctAnswer
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
    var thisWord: Word? = null

    while (true) {
        Thread.sleep(2000)
        val updates: String = tgBotService.getUpdates(updateId)
        println(updates)

        val updateIdString = updateIdRegex.find(updates)?.groups?.get(1)?.value ?: continue
        updateId = updateIdString.toInt() + 1

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt() ?: continue

        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        when (data) {
            STATISTIC_CALLBACK -> {
                val statistic = trainer.getStatistics()
                val statisticString = "Выучено ${statistic.learned} из ${statistic.total} слов | ${statistic.percent}%"
                println(tgBotService.sendMessage(chatId, statisticString))
            }

            LEARN_WORDS_CALLBACK -> {
                thisWord = checkNextQuestionAndSend(trainer, tgBotService, chatId)
            }

            COMEBACK_CALLBACK -> {
                println(tgBotService.sendMenu(chatId))
            }

            else -> {
                if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && thisWord != null) {
                    if (trainer.checkAnswer(data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt())) {
                        println(tgBotService.sendMessage(chatId, "Верно!"))
                    } else {
                        println(
                            tgBotService.sendMessage(
                                chatId,
                                "Неправильно! ${thisWord.word} -  ${thisWord.translate} "
                            )
                        )
                    }
                    thisWord = checkNextQuestionAndSend(trainer, tgBotService, chatId)
                }
            }
        }

        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: continue

        when (text.lowercase()) {
            "/start" -> println(tgBotService.sendMenu(chatId))
            "hello" -> println(tgBotService.sendMessage(chatId, "Hello"))
            // TODO: Исправить баг, когда бот отправляет апдейт со словом из словаря hello отправляет его же в ответ так же происходит и со словом меню
            // TODO: Наверное стоит либо убрать эти команды либо добавить / перед, либо добавить проверку от кого апдейт
            "menu" -> println(tgBotService.sendMenu(chatId))
        }
    }
}