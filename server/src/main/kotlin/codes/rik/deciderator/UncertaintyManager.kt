package codes.rik.deciderator

import codes.rik.deciderator.types.ActiveOptionProperties
import codes.rik.deciderator.types.CoinFace.HEADS
import codes.rik.deciderator.types.CoinFace.TAILS
import codes.rik.deciderator.types.FlipResult
import codes.rik.deciderator.types.RoundCompleteMetadata
import codes.rik.deciderator.types.Uncertainty
import codes.rik.deciderator.types.UncertaintyId
import codes.rik.deciderator.types.UncertaintyOption
import codes.rik.deciderator.types.UncertaintyRules
import codes.rik.deciderator.types.Username
import codes.rik.deciderator.types.activeOption
import codes.rik.deciderator.types.activeOptionProps
import codes.rik.deciderator.types.remainingOptions
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.time.Duration

object UncertaintyManager {
  private val uncertainties = PLACEHOLDER_UNCERTAINTIES.associateBy { it.id }.toMutableMap()

  fun create(name: String, options: Set<String>): UncertaintyId {
    val uncertainty = Uncertainty(
      id = createId(),
      name = name,
      rules = UncertaintyRules(
        bestOf = 5,
        finalTwoHeadToHead = true
      ),
      options = options.mapIndexed { i, name ->
        UncertaintyOption(
          name = name,
          coinStyle = "germany",
          active = i.takeIf { it == 0 }?.let { ActiveOptionProperties()
          }
        )
      }
    )
    uncertainties[uncertainty.id] = uncertainty
    return uncertainty.id
  }

  fun get(id: UncertaintyId): Uncertainty {
    return uncertainties[id] ?: throw RuntimeException("Unknown uncertainty: $id")
  }

  fun updateCoinStyle(uncertaintyId: UncertaintyId, style: String) {
    val newUncertainty = get(uncertaintyId).updateActiveOption { it.copy(coinStyle = style) }
    uncertainties[uncertaintyId] = newUncertainty
  }

  fun addResult(uncertaintyId: UncertaintyId, result: FlipResult) {
    val uncertainty = get(uncertaintyId)
    var newUncertainty = uncertainty

    // Add the new result
    newUncertainty = newUncertainty.updateActiveOption {
      it.copy(active = it.active?.copy(results = it.active.results + result))
    }

    // Calculate if heads or tails won the round
    val active = newUncertainty.activeOption.active!!
    when {
        active.results.filter { it.result == HEADS }.size > uncertainty.rules.bestOf.toDouble() / 2 -> false
        active.results.filter { it.result == TAILS }.size > uncertainty.rules.bestOf.toDouble() / 2 -> true
        else -> null
    }?.also { eliminated ->
      // Determine any special considerations for next round (if end of loop)
      val roundCompleteMetadata = when (newUncertainty.remainingOptions.size) {
        1 -> RoundCompleteMetadata(overallWinner = newUncertainty.remainingOptions.first().name)
        2 -> RoundCompleteMetadata(nextRoundIsHeadToHead = true)
        0 -> RoundCompleteMetadata(nextRoundIsLightningLoop = true)
        else -> RoundCompleteMetadata()
      }
      newUncertainty = newUncertainty.updateActiveOption {
        it.copy(
          eliminated = eliminated,
          active = active.copy(roundComplete = roundCompleteMetadata)
        )
      }
    }

    uncertainties[uncertaintyId] = newUncertainty
  }

  fun nextRound(uncertaintyId: UncertaintyId) {
    val uncertainty = get(uncertaintyId)
    var newUncertainty = uncertainty

    val curOption = uncertainty.activeOption
    var idx = uncertainty.options.indexOf(curOption) + 1
    var nxtOption: UncertaintyOption?
    while (true) {
      if (idx == uncertainty.options.size) {
        idx = 0

        // Do we need to do any new loop special behaviour?
        when (newUncertainty.remainingOptions.size) {
          1 -> {
            // winner. metadata should already be set, and this shouldn't really be called. no-op
            return
          }
          2 -> {
            // h2h. reconfigure.
            // FIXME - break out of this nextRound and reconfigure the Uncertainty
            return
          }
          0 -> {
            // switch to lightning rules, reset elimination status
            newUncertainty = newUncertainty.copy(
              rules = newUncertainty.rules.copy(bestOf = 1),
              options = newUncertainty.options.map { it.copy(eliminated = it.startedLoopEliminated) }
            )
          }
        }
      }

      nxtOption = uncertainty.options[idx]
      if (!nxtOption.eliminated) break
    }

    // update options
    newUncertainty = newUncertainty.copy(
      options = newUncertainty.options
        .map {
          it.copy(
            startedLoopEliminated = it.eliminated,
            active = when(it) {
              nxtOption -> ActiveOptionProperties()
              else -> null
            }
          )
        }
    )

    uncertainties[uncertaintyId] = newUncertainty
  }

  private fun Uncertainty.updateActiveOption(updater: (UncertaintyOption) -> UncertaintyOption) = copy(
    options = options
      .map {
        if (it.active != null) {
          updater(it)
        } else {
          it
        }
      }
  )

  private fun createId(): UncertaintyId {
    do {
      val id = UncertaintyId.create()
      if (!uncertainties.containsKey(id)) {
        return id
      }
    } while (true)
  }
}

private val PLACEHOLDER_UNCERTAINTIES = listOf(
  Uncertainty(
    id = UncertaintyId("foo"),
    name = "[TEST] What game should we play next?",
    rules = UncertaintyRules(bestOf = 5, finalTwoHeadToHead = true),
    options = listOf(
      UncertaintyOption("EU4", coinStyle = "first_world_war"),
      UncertaintyOption("Civ VI", coinStyle = "eu_germany"),
      UncertaintyOption("HoI4", coinStyle = "germany", eliminated = true),
      UncertaintyOption("Stellaris",
        coinStyle = "usa_trump",
        active = ActiveOptionProperties(
          results = listOf(
            FlipResult(
              result = HEADS,
              coinStyle = "germany",
              flippedBy = Username("Rik"),
              waitTime = Duration.ofMillis(1234),
              flipTime = Duration.ofMillis(4567)
            ),
            FlipResult(
              result = HEADS,
              coinStyle = "germany",
              flippedBy = Username("Mark"),
              waitTime = Duration.ofMillis(2345),
              flipTime = Duration.ofMillis(7655)
            ),
            FlipResult(
              result = TAILS,
              coinStyle = "germany",
              flippedBy = Username("Mark"),
              waitTime = Duration.ofMillis(23112),
              flipTime = Duration.ofMillis(5342)
            ),
          )
        )
      )
    )
  ),
  Uncertainty(
    id = UncertaintyId("bar"),
    name = "[TEST] Best of 1!",
    rules = UncertaintyRules(bestOf = 1, finalTwoHeadToHead = true),
    options = listOf(
      UncertaintyOption("EU4", coinStyle = "first_world_war", active = ActiveOptionProperties()),
      UncertaintyOption("Civ VI", coinStyle = "eu_germany"),
      UncertaintyOption("HoI4", coinStyle = "germany"),
    )
  ),
  Uncertainty(
    id = UncertaintyId("winner"),
    name = "[TEST] Already has a winner",
    rules = UncertaintyRules(bestOf = 5, finalTwoHeadToHead = true),
    options = listOf(
      UncertaintyOption("EU4", coinStyle = "first_world_war", eliminated = true),
      UncertaintyOption("Civ VI", coinStyle = "eu_germany"),
      UncertaintyOption("HoI4", coinStyle = "germany", eliminated = true),
      UncertaintyOption("Stellaris",
        coinStyle = "usa_trump",
        eliminated = true,
        active = ActiveOptionProperties(
          roundComplete = RoundCompleteMetadata(
            overallWinner = "Civ VI",
          ),
          results = listOf(
            FlipResult(
              result = TAILS,
              coinStyle = "germany",
              flippedBy = Username("Rik"),
              waitTime = Duration.ofMillis(1234),
              flipTime = Duration.ofMillis(4567)
            ),
          )
        )
      )
    )
  ),
)
