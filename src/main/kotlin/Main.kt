package org.example

import java.io.File

data class Word(val word: String, val translate: String, var correctAnswersCount: Int = 0)

fun main() {
    val wordsFile: File = File("words.txt")
    val dictionary: MutableList<Word> = mutableListOf()

    for (line in wordsFile.readLines()) {
        val lineList = line.split("|")
        val answersNullPlug = { if (lineList.size == 2) 0 else lineList[2].toInt() }
        dictionary.add(
            Word(lineList[0], lineList[1], answersNullPlug())
        )

    }
    dictionary.forEach { println(it) }
}