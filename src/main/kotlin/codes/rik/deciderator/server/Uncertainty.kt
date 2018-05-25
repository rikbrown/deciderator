package codes.rik.deciderator.server

import codes.rik.deciderator.types.*
import codes.rik.deciderator.types.CoinFlipResult.*
import org.springframework.web.socket.WebSocketSession
import java.security.SecureRandom
import kotlinx.coroutines.experimental.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KProperty

class Uncertainty(
        val id: UncertaintyId,
        var name: String = "Unnamed Uncertainty",
        var decisions: MutableSet<CoinFlipResult> = mutableSetOf(),
        private val publisher: UncertaintyPublisher) {

    private var flipping = AtomicBoolean(false)

    suspend fun calculateDecision() {
        if (!flipping.compareAndSet(false, true)) {
            return
        }

        val decision = if (RANDOM.nextBoolean()) HEADS else TAILS

        publisher.onRotationUpdated(Rotation(z = -0.01))

        delay(500)
        publisher.onRotationUpdated(Rotation(z = -0.05))

        delay(500)
        publisher.onRotationUpdated(Rotation(z = -0.1))

        delay(500)
        publisher.onRotationUpdated(Rotation(z = -0.2))

        delay(500)
        publisher.onRotationUpdated(Rotation(z = -0.3))

        delay(500)
        publisher.onRotationUpdated(Rotation(z = -0.5))

        delay(4000)
        decisions.add(decision)
        publisher.onDecisionMade(decision, decisions)


        flipping.set(false)

    }

    override fun toString(): String {
        return "Uncertainty(id=$id, name='$name')"
    }

    val info get() = UncertaintyInfo(name, decisions)

}

private val RANDOM = SecureRandom()
