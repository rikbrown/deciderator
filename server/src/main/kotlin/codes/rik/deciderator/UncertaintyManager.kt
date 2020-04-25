package codes.rik.deciderator

import codes.rik.deciderator.types.ActiveOptionProperties
import codes.rik.deciderator.types.CoinFace.HEADS
import codes.rik.deciderator.types.CoinFace.TAILS
import codes.rik.deciderator.types.CoinStyle
import codes.rik.deciderator.types.FlipResult
import codes.rik.deciderator.types.OptionName
import codes.rik.deciderator.types.Round
import codes.rik.deciderator.types.Round.HeadToHeadRound
import codes.rik.deciderator.types.Round.MeaningfulVoteRound
import codes.rik.deciderator.types.RoundCompleteMetadata
import codes.rik.deciderator.types.Uncertainty
import codes.rik.deciderator.types.UncertaintyId
import codes.rik.deciderator.types.UncertaintyOption
import codes.rik.deciderator.types.UncertaintyRules
import codes.rik.deciderator.types.Username
import codes.rik.deciderator.types.activeOption
import codes.rik.deciderator.types.activeOptionProps
import codes.rik.deciderator.types.remainingOptions
import codes.rik.deciderator.types.replace
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.time.Duration

object UncertaintyManager {
  private val uncertainties = PLACEHOLDER_UNCERTAINTIES.associateBy { it.id }.toMutableMap()

  /**
   * Create a new uncertainty
   */
  fun create(name: String, options: Set<OptionName>): UncertaintyId {
    val uncertainty = Uncertainty(
      id = createId(),
      name = name,
      rules = UncertaintyRules(
        bestOf = 5,
        finalTwoHeadToHead = true
      ),
      options = options.map {
        UncertaintyOption(
          name = it,
          coinStyle = CoinStyle("germany"),
        )
      },
      currentRound = MeaningfulVoteRound(options.first()) // TODO: only 2 options = h2h
    )
    uncertainties[uncertainty.id] = uncertainty
    return uncertainty.id
  }

  /**
   * Retrieve an existing uncertainty
   */
  fun get(id: UncertaintyId) = uncertainties[id] ?: throw UncertaintyNotFoundException(id)

  /**
   * Update the coin style for an uncertainty
   */
  fun updateCoinStyle(uncertaintyId: UncertaintyId, style: CoinStyle) {
    val uncertainty = get(uncertaintyId)
    uncertainties[uncertaintyId] = when (val round = uncertainty.currentRound) {
      is MeaningfulVoteRound -> uncertainty.copy(
        options = uncertainty.options.replace(round.option) { it.copy(coinStyle = style) }
      )
      is HeadToHeadRound -> uncertainty.copy(currentRound = round.copy(coinStyle = style))
    }
  }

  /**
   * Add a result to an uncertainty, potentially triggering side effects such as round, loop
   * or uncertainty completion.
   */
  fun addResult(uncertaintyId: UncertaintyId, result: FlipResult) {
    val uncertainty = get(uncertaintyId)
    var newUncertainty = uncertainty

    uncertainty.copy(
      currentRound = when (val round = uncertainty.currentRound) {
        is MeaningfulVoteRound -> round.copy(results = round.results + result)
        is HeadToHeadRound -> round.copy(results = round.results + result)
      }
    )

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

data class UncertaintyNotFoundException(val id: UncertaintyId) : RuntimeException("Uncertainty not found: $id")
