package org.example

import java.io.File

fun main() {
    val wordsFile: File = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.writeText(
        "hello привет\n" +
                "dog собака\n" +
                "cat кошка"
    )
    for (line in wordsFile.readLines()) {
        println(line)
    }
}