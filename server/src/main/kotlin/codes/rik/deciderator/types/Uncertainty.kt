package codes.rik.deciderator.types

import java.lang.IllegalStateException
import java.time.Duration

data class Uncertainty(
  val id: UncertaintyId,
  val name: String,
  val rules: UncertaintyRules,
  val options: List<UncertaintyOption>
)

data class UncertaintyRules(
  val bestOf: Int,
  val finalTwoHeadToHead: Boolean
)

data class UncertaintyOption(
  val name: String,
  val eliminated: Boolean = false,
  val active: ActiveOptionProperties? = null
)

data class ActiveOptionProperties(
  val coinStyle: String,
  val roundComplete: Boolean = false,
  val results: List<FlipResult> = listOf()
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

val Uncertainty.activeOption get() = options.find { it.active != null }?.active ?: throw IllegalStateException()
