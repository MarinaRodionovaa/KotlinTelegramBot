package marinarodionova.english.trainer.model

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val word: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)