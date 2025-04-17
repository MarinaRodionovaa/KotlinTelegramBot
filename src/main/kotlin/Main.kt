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
            1 -> println("Учить слова")
            2 -> println("Статистика")
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