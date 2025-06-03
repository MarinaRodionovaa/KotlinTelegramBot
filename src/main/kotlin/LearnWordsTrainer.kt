package org.example

import java.io.File

data class Word(
    val word: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

data class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer() {

    var currentQuestion: Question? = null
    private val dictionary = loadDictionary()
    private val maxCorrectAnswersCount = 3

    fun getNextQuestion(): Question? {
        val notLearnedList = getNotLearnedList()
        if (notLearnedList.isNotEmpty()) {
            var listToLearn = notLearnedList.shuffled().take(COUNTS_OF_WORDS)
            val questionWord = listToLearn.random()

            listToLearn += getLearnedList().shuffled()
                .take((COUNTS_OF_WORDS - notLearnedList.size).coerceAtLeast(0))
            currentQuestion = Question(listToLearn.shuffled(), questionWord)
            return currentQuestion
        } else {
            return null
        }
    }

    fun checkAnswer(userAnswer: Int): Boolean {
        return currentQuestion?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)

            if (userAnswer == correctAnswerId) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    fun getStatistics(): Statistics {
        val learned = dictionary.filter { word -> word.correctAnswersCount >= maxCorrectAnswersCount }.size
        val total = dictionary.size
        val percent = learned * 100 / total

        return Statistics(learned, total, percent)
    }

    private fun loadDictionary(): List<Word> {
        val wordsFile: File = File("words.txt")
        val dictionary: MutableList<Word> = mutableListOf()

        try {
            for (line in wordsFile.readLines()) {
                val lineList = line.split("|")
                dictionary.add(
                    Word(lineList[0], lineList[1], lineList.getOrNull(2)?.toIntOrNull() ?: 0)
                )

            }
            return dictionary.toList()
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }

    private fun saveDictionary() {
        val file = File("words.txt")
        file.writeText("")
        dictionary.forEach {
            file.appendText("${it.word}|${it.translate}|${it.correctAnswersCount}\n")
        }

    }

    private fun getNotLearnedList(): MutableList<Word> {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < maxCorrectAnswersCount }.toMutableList()
        return notLearnedList
    }

    private fun getLearnedList(): List<Word> {
        return dictionary.filter { word -> word.correctAnswersCount >= maxCorrectAnswersCount }
    }

}