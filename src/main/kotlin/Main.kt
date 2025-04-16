package org.example

import java.io.File

data class Word(
    val word: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun main() {
    val wordsFile: File = File("words.txt")
    val dictionary: MutableList<Word> = mutableListOf()

    for (line in wordsFile.readLines()) {
        val lineList = line.split("|")
        dictionary.add(
            Word(lineList[0], lineList[1], lineList.getOrNull(2)?.toIntOrNull() ?: 0)
        )

    }
    dictionary.forEach { println(it) }
}