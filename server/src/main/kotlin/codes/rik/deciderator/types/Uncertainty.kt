package codes.rik.deciderator.types

import com.fasterxml.jackson.annotation.JsonValue
import java.lang.IllegalStateException
import java.time.Duration
import codes.rik.deciderator.replace

data class Uncertainty(
  val id: UncertaintyId,
  val name: String,
  val rules: UncertaintyRules,
  val options: List<UncertaintyOption>,
  val currentRound: Round,
  val winner: Winner? = null
)

sealed class Round(val type: String) {
  abstract val results: List<FlipResult>

  data class MeaningfulVoteRound(
    val option: OptionName,
    override val results: List<FlipResult> = listOf(),
  ) : Round("MeaningfulVote")

  data class HeadToHeadRound(
    val headsOption: OptionName,
    val tailsOption: OptionName,
    val coinStyle: CoinStyle,
    override val results: List<FlipResult> = listOf(),
  ) : Round("HeadToHead")
}

data class UncertaintyRules(
  val bestOf: Int,
  val startingBestOf: Int = bestOf,
  val finalTwoHeadToHead: Boolean
)

data class UncertaintyOption(
  val name: OptionName,
  val startedLoopEliminated: Boolean = false,
  val eliminated: Boolean = false,
  val coinStyle: CoinStyle,
)

data class Winner(
  val name: OptionName,
  // TODO: add statistics
)

data class FlipResult(
  val result: CoinFace,
  val coinStyle: CoinStyle,
  val flippedBy: Username,
  val waitTime: Duration,
  val flipTime: Duration,
)

data class OptionName(@get:JsonValue val optionName: String)
val Uncertainty.remainingOptions get() = options.filter { !it.eliminated }

fun Collection<UncertaintyOption>.replace(name: OptionName, replacer: (UncertaintyOption) -> UncertaintyOption)
  = replace({ it.name == name }, replacer)
