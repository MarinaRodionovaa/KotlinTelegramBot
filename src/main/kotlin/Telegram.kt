package org.example

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId: Int = 0

    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = """"chat"\s*:\s*\{\s*"id"\s*:\s*(\d+)""".toRegex()

    val tgBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)
        val updates: String = tgBotService.getUpdates(updateId)
        println(updates)

        val updateIdString = updateIdRegex.find(updates)?.groups?.get(1)?.value ?: continue
        updateId = updateIdString.toInt() + 1

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt() ?: continue
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: continue

        if (text.lowercase() == "hello") {
            println(tgBotService.sendMessage(chatId, "Hello"))
        }
    }
}