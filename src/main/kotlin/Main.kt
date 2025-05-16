package org.example

const val COUNTS_OF_WORDS = 4

data class Word(
    val word: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun Question.asConsoleString(): String {
    val variants =
        this.variants.mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.word}" }.joinToString("\n")
    return this.correctAnswer.translate + "\n" + variants + "\n----------\n" + "0 - Меню"
}

fun main() {

    val trainer = LearnWordsTrainer()

    while (true) {
        println("Меню: \n1 – Учить слова\n2 – Статистика\n0 – Выход")
        val menuAnswer = readln().toIntOrNull() ?: -1
        when (menuAnswer) {
            0 -> break
            1 -> {
                println("Учить слова\n")

                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Все слова выучены")
                        break
                    } else {
                        println(question.asConsoleString())
                        val userAnswerInput = readln().toIntOrNull() ?: -1

                        if (userAnswerInput == 0) {
                            break
                        } else if (trainer.checkAnswer(userAnswerInput.minus(1))) {
                            println("Правильно!")
                        } else {
                            println("\nНеправильно! ${question.correctAnswer.word} – ${(question.correctAnswer.translate)}\n")
                        }
                    }
                }
            }

            2 -> {
                println("Статистика:")
                val statistic = trainer.getStatistics()
                println("Выучено ${statistic.learned} из ${statistic.total} слов | ${statistic.percent}%")
            }

            else -> println("Введите число 1, 2 или 0")
        }

    }
}





