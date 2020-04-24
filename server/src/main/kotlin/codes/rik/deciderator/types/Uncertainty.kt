package codes.rik.deciderator.types

import java.lang.IllegalStateException
import java.time.Duration

data class Uncertainty(
  val id: UncertaintyId,
  val name: String,
  val rules: UncertaintyRules,
  val options: List<UncertaintyOption>,
)

data class UncertaintyRules(
  val bestOf: Int,
  val startingBestOf: Int = bestOf,
  val finalTwoHeadToHead: Boolean
)

data class UncertaintyOption(
  val name: String,
  val startedLoopEliminated: Boolean = false,
  val eliminated: Boolean = false,
  val coinStyle: String,
  val active: ActiveOptionProperties? = null
)

data class ActiveOptionProperties(
  val roundComplete: RoundCompleteMetadata? = null,
  val results: List<FlipResult> = listOf()
)

data class RoundCompleteMetadata(
  val nextRoundIsLightningLoop: Boolean = false,
  val nextRoundIsHeadToHead: Boolean = false,
  val overallWinner: String? = null,
)

data class FlipResult(
  val result: CoinFace,
  val coinStyle: String,
  val flippedBy: Username,
  val waitTime: Duration,
  val flipTime: Duration
)

enum class CoinFace {
  HEADS, TAILS
}

val Uncertainty.activeOption get() = options.find { it.active != null } ?: throw IllegalStateException()
val Uncertainty.activeOptionProps get() = options.find { it.active != null }?.active ?: throw IllegalStateException()
val Uncertainty.remainingOptions get() = options.filter { !it.eliminated }
