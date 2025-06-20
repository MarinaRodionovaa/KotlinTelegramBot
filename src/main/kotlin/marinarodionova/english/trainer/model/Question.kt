package marinarodionova.english.trainer.model

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)