package codes.rik.deciderator.server

import codes.rik.deciderator.types.*
import org.springframework.web.socket.WebSocketSession
import sendMessage

interface UncertaintyPublisher {
    fun onRotationUpdated(rotation: Rotation)
    fun onDecisionMade(decision: CoinFlipResult, decisions: List<CoinFlipResult>)
}

abstract class UncertaintySocketPublisher(private val uncertaintyId: UncertaintyId): UncertaintyPublisher {
    abstract fun getSessions(): Set<WebSocketSession>

    override fun onRotationUpdated(rotation: Rotation) {
        val message = DecidingMessage(uncertaintyId, rotation)
        getSessions().forEach { session -> session.sendMessage(message) }
    }

    override fun onDecisionMade(decision: CoinFlipResult, decisions: List<CoinFlipResult>) {
        val message = DecisionMessage(uncertaintyId, decision, decisions)
        getSessions().forEach { session -> session.sendMessage(message) }
    }

}