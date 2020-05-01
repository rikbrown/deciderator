package codes.rik.deciderator.types

import com.fasterxml.jackson.annotation.JsonValue
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

data class Round(
  val data: RoundData,
  val coinStyle: CoinStyle,
  val results: List<FlipResult> = listOf(),
  val winningFace: CoinFace? = null,
) {
  sealed class RoundData {
    data class MeaningfulVoteRound(
      val option: OptionName,
      val customRules: UncertaintyRules? = null
    ) : RoundData()

    data class HeadToHeadRound(
      val headsOption: OptionName,
      val tailsOption: OptionName,
    ) : RoundData()
  }
}

data class UncertaintyRules(
  val bestOf: Int,
  val startingBestOf: Int = bestOf,
  val finalTwoHeadToHead: Boolean
)

data class UncertaintyOption(
  val name: OptionName,
  val coinStyle: CoinStyle,
  val eliminated: Boolean = false,
  val startedLoopEliminated: Boolean = false,
)

data class Winner(
  val name: OptionName,
  val face: CoinFace,
  val coinStyle: CoinStyle,
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
val Uncertainty.currentRules get() = (currentRound.data as? Round.RoundData.MeaningfulVoteRound)?.customRules ?: rules

fun Collection<UncertaintyOption>.replace(name: OptionName, replacer: (UncertaintyOption) -> UncertaintyOption)
  = replace({ it.name == name }, replacer)

fun Collection<FlipResult>.count(face: CoinFace) = filter { it.result == face }.size
