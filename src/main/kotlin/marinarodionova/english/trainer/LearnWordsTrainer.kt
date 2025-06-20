package marinarodionova.english.trainer

import marinarodionova.english.trainer.model.Question
import marinarodionova.english.trainer.model.Statistics
import marinarodionova.english.trainer.model.Word
import java.io.File

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val maxCorrectAnswersCount: Int = 2,
    private val countOfWords: Int = 3,

    ) {
    var currentQuestion: Question? = null
    private val dictionary = loadDictionary()

    fun getNextQuestion(): Question? {
        val notLearnedList = getNotLearnedList()
        if (notLearnedList.isNotEmpty()) {
            var listToLearn = notLearnedList.shuffled().take(countOfWords)
            val questionWord = listToLearn.random()

            listToLearn += getLearnedList().shuffled()
                .take((countOfWords - notLearnedList.size).coerceAtLeast(0))
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
        val wordsFile = File(fileName)
        if (!wordsFile.exists()) {
            File("words.txt").copyTo(wordsFile)
        }

        return try {
            wordsFile.readLines()
                .mapNotNull { line ->
                    val lineList = line.split("|")
                    if (lineList.size < maxCorrectAnswersCount) null
                    else
                        Word(
                            word = lineList[0],
                            translate = lineList[1],
                            correctAnswersCount = lineList.getOrNull(2)?.toIntOrNull() ?: 0,
                        )
                }
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл", e)
        }
    }

    private fun saveDictionary() {
        val file = File(fileName)
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

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }
}