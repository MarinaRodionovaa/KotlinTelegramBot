package org.example

const val STATISTIC_CALLBACK = "statistic_callback"
const val LEARN_WORDS_CALLBACK = "learn_words_callback"

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
        Thread.sleep(2000)
        val updates: String = tgBotService.getUpdates(updateId)
        println(updates)

        val updateIdString = updateIdRegex.find(updates)?.groups?.get(1)?.value ?: continue
        updateId = updateIdString.toInt() + 1

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt() ?: continue

        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (data == STATISTIC_CALLBACK) {
            val statistic = trainer.getStatistics()
            val statisticString = "Выучено ${statistic.learned} из ${statistic.total} слов | ${statistic.percent}%"
            println(tgBotService.sendMessage(chatId, statisticString))
        }

        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: continue

        when (text.lowercase()) {
            "/start" -> println(tgBotService.sendMenu(chatId))
            "hello" -> println(tgBotService.sendMessage(chatId, "Hello"))
            "menu" -> println(tgBotService.sendMenu(chatId))
        }

    }
}