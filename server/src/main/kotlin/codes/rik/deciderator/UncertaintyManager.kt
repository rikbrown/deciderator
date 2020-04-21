package codes.rik.deciderator

import codes.rik.deciderator.types.ActiveOptionProperties
import codes.rik.deciderator.types.CoinFace
import codes.rik.deciderator.types.FlipResult
import codes.rik.deciderator.types.Uncertainty
import codes.rik.deciderator.types.UncertaintyId
import codes.rik.deciderator.types.UncertaintyOption
import codes.rik.deciderator.types.UncertaintyRules
import org.apache.commons.lang3.RandomStringUtils
import java.time.Duration
import kotlin.random.Random

object UncertaintyManager {
  private val uncertainties: MutableMap<UncertaintyId, Uncertainty> = HashMap()

  init {
    uncertainties[UncertaintyId("foo")] = Uncertainty(
      id = UncertaintyId("foo"),
      name = "[TEST] What game should we play next?",
      rules = UncertaintyRules(bestOf = 5, finalTwoHeadToHead = true),
      options = listOf(
        UncertaintyOption("EU4"),
        UncertaintyOption("Civ VI"),
        UncertaintyOption("HoI4", eliminated = true),
        UncertaintyOption("Stellaris",
          active = ActiveOptionProperties(
            coinStyle = "germany",
            results = listOf(
              FlipResult(
                result = CoinFace.HEADS,
                coinStyle = "germany",
                flippedBy = "Rik",
                waitTime = Duration.ofMillis(1234),
                flipTime = Duration.ofMillis(4567)
              ),
              FlipResult(
                result = CoinFace.HEADS,
                coinStyle = "germany",
                flippedBy = "Mark",
                waitTime = Duration.ofMillis(2345),
                flipTime = Duration.ofMillis(7655)
              ),
              FlipResult(
                result = CoinFace.TAILS,
                coinStyle = "germany",
                flippedBy = "Mark",
                waitTime = Duration.ofMillis(23112),
                flipTime = Duration.ofMillis(5342)
              ),
            )
          )
        )
      )
    )
  }

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
          active = i.takeIf { it == 0 }?.let {
            ActiveOptionProperties(coinStyle = "germany")
          }
        )
      }
    )
    uncertainties[uncertainty.id] = uncertainty
    return uncertainty.id
  }

  fun get(id: UncertaintyId): Uncertainty? {
    return uncertainties[id]
  }

  private fun createId(): UncertaintyId {
    do {
      val id = UncertaintyId.create()
      if (!uncertainties.containsKey(id)) {
        return id
      }
    } while (true)
  }

}
