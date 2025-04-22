package org.example

import java.io.File

data class Word(
    val word: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun main() {
    val dictionary = loadDictionary()
    while (true) {
        println("Меню: \n1 – Учить слова\n2 – Статистика\n0 – Выход")
        val menuAnswer = readln().toIntOrNull() ?: -1
        when (menuAnswer) {
            0 -> break
            1 -> {
                println("Учить слова")
                var notLearnedList = getNotLearnedList(dictionary)
                var isContinueLearn = notLearnedList.isNotEmpty()

                while (isContinueLearn) {
                    for (notLearnedWord in notLearnedList) {
                        println("\n${notLearnedWord.word}:")
                        val questionWords =
                            notLearnedList.filter { it.translate != notLearnedWord.translate }.take(3)
                                .map { it.translate }
                                .toMutableList()
                        questionWords.add(notLearnedWord.translate)
                        questionWords.shuffle()

                        for (i in 0..<questionWords.size) {
                            println("${i + 1}) - ${questionWords[i]}")
                        }
                        println("----------\n" + "0 - Меню")

                        val userAnswerInput = readln().toIntOrNull() ?: -1
                        val correctAnswerId = questionWords.indexOf(notLearnedWord.translate)

                        if (userAnswerInput == 0) {
                            isContinueLearn = false
                            break
                        } else if (userAnswerInput - 1 == correctAnswerId) {
                            println("Правильно!")
                            dictionary.filter { it.word == notLearnedWord.word }.forEach { it.correctAnswersCount += 1 }
                            saveDictionary(dictionary)
                            notLearnedList = getNotLearnedList(dictionary)
                        } else {
                            println("Неправильно! ${notLearnedWord.word} – ${notLearnedWord.translate}")
                        }

                    }

                }
            }

            2 -> {
                println("Статистика")
                val totalCount = dictionary.size
                val learnedCount = getLearnedCount(dictionary)
                val percent = learnedCount / totalCount * 100
                println("Выучено $learnedCount из $totalCount слов | $percent%")
            }

            else -> println("Введите число 1, 2 или 0")
        }

    }
}

fun loadDictionary(): List<Word> {
    val wordsFile: File = File("words.txt")
    val dictionary: MutableList<Word> = mutableListOf()

    for (line in wordsFile.readLines()) {
        val lineList = line.split("|")
        dictionary.add(
            Word(lineList[0], lineList[1], lineList.getOrNull(2)?.toIntOrNull() ?: 0)
        )

    }
    return dictionary.toList()

}

fun getLearnedCount(dictionary: List<Word>): Int {
    return dictionary.filter { word -> word.correctAnswersCount >= 3 }.count()
}

fun getNotLearnedList(dictionary: List<Word>): MutableList<Word> {
    val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }.toMutableList()
    if (notLearnedList.isEmpty()) {
        println("Все слова выучены")
    }
    return notLearnedList
}

fun saveDictionary(dictionary: List<Word>) {
    File("words.txt").writeText("")
    dictionary.forEach {
        File("words.txt").appendText("${it.word}|${it.translate}|${it.correctAnswersCount}\n")
    }

}