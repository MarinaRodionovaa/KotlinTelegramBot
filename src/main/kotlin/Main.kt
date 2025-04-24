package org.example

import java.io.File

const val COUNTS_OF_WORDS = 4

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

                while (getNotLearnedList(dictionary).isNotEmpty()) {
                    val listToLearn: List<Word>
                    if (getNotLearnedList(dictionary).size >= COUNTS_OF_WORDS) {
                        listToLearn = getNotLearnedList(dictionary).shuffled().take(COUNTS_OF_WORDS)

                    } else {
                        listToLearn = getNotLearnedList(dictionary).shuffled()
                            .take(COUNTS_OF_WORDS) + getLearnedList(dictionary).shuffled().take(
                            4 - getNotLearnedList(dictionary).size
                        )
                    }

                    val questionWords = listToLearn.map { it.translate }
                    val (questionWord, translate) = listToLearn.random()

                    println("$questionWord:")
                    questionWords.forEach { println("${questionWords.indexOf(it) + 1} - $it") }
                    println("----------\n" + "0 - Меню")
                    val correctAnswerId = questionWords.indexOf(translate)

                    val userAnswerInput = readln().toIntOrNull() ?: -1
                    if (userAnswerInput == 0) {
                        break
                    } else if (userAnswerInput - 1 == correctAnswerId) {
                        println("Правильно!")
                        dictionary.filter { it.word == questionWord }.forEach { it.correctAnswersCount += 1 }
                        saveDictionary(dictionary)
                    } else {
                        println("Неправильно! ${questionWord} – ${(questionWords[correctAnswerId])}")
                    }

                }
                if (getNotLearnedList(dictionary).isEmpty()) println("Все слова выучены")
            }

            2 -> {
                println("Статистика")
                val totalCount = dictionary.size
                val learnedCount = getLearnedList(dictionary).size
                val percent = (learnedCount.toFloat() / totalCount.toFloat() * 100).toInt()
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

fun getLearnedList(dictionary: List<Word>): List<Word> {
    return dictionary.filter { word -> word.correctAnswersCount >= 3 }
}

fun getNotLearnedList(dictionary: List<Word>): MutableList<Word> {
    val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }.toMutableList()
    return notLearnedList
}

fun saveDictionary(dictionary: List<Word>) {
    File("words.txt").writeText("")
    dictionary.forEach {
        File("words.txt").appendText("${it.word}|${it.translate}|${it.correctAnswersCount}\n")
    }

}