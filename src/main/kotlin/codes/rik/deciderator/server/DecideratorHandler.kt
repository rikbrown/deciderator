package codes.rik.deciderator.server

import codes.rik.deciderator.types.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import kotlinx.coroutines.experimental.async
import objectMapper
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import sendMessage
import sessionId
import username
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class DecideratorHandler: TextWebSocketHandler() {

    private val sessions: ConcurrentMap<SessionId, WebSocketSession> = ConcurrentHashMap()
    private val uncertainties: ConcurrentMap<UncertaintyId, Uncertainty> = ConcurrentHashMap()
    private val sessionUncertainty: ConcurrentMap<SessionId, UncertaintyId> = ConcurrentHashMap()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.sessionId] = session
        session.sendMessage(HelloMessage(
                sessionId = session.sessionId,
                onlineSessionIds = sessions.keys,
                message = "Hello ${session.id}"))
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.sessionId)

        with(sessionUncertainty[session.sessionId]) {
            if (this != null) notifyUsers(this)
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        System.err.println(message.payload)
        val msg = objectMapper.readValue<UncertaintyRequest>(message.payload)
        when (msg) {
            is SetUsernameRequest -> setUsername(session, msg.username)
            is CreateUncertaintyRequest -> createUncertainty(session)
            is JoinUncertaintyRequest -> joinUncertainty(session, msg.uncertaintyId)
            is SetUncertaintyNameRequest -> setUncertaintyName(session, msg.uncertaintyId, msg.name)
            is MakeDecisionRequest -> makeDecision(session, msg.uncertaintyId)
        }
    }

    private fun setUncertaintyName(session: WebSocketSession, uncertaintyId: UncertaintyId, name: String) {
        val uncertainty = uncertainties[uncertaintyId]
        if (uncertainty == null) {
            session.sendMessage(UncertaintyNotFoundMessage(uncertaintyId))
            return
        }

        uncertainty.name = name

        sendToUncertaintySessions(uncertaintyId, UncertaintyUpdatedMessage(uncertaintyId, uncertainty.info))
    }

    private fun makeDecision(session: WebSocketSession, uncertaintyId: UncertaintyId) {
        val uncertainty = uncertainties[uncertaintyId]
        if (uncertainty == null) {
            session.sendMessage(UncertaintyNotFoundMessage(uncertaintyId))
            return
        }

        async {
            uncertainty.calculateDecision()
        }
    }

    private fun setUsername(session: WebSocketSession, username: String) {
        session.username = username
        session.sendMessage(UsernameSetMessage(username))
    }

    private fun createUncertainty(session: WebSocketSession) {
        val uncertaintyId = createUncertaintyId()

        val uncertainty = Uncertainty(uncertaintyId, publisher = object : UncertaintySocketPublisher(uncertaintyId) {
            override fun getSessions() = getUncertaintySessions(uncertaintyId)
        })

        uncertainties[uncertaintyId] = uncertainty
        sessionUncertainty[session.sessionId] = uncertaintyId // implicitly joins
        notifyUsers(uncertaintyId)
        session.sendMessage(UncertaintyCreatedMessage(uncertaintyId, uncertainty.info))
    }

    private fun createUncertaintyId(): UncertaintyId {
        val id = UncertaintyId.create()
        return if (uncertainties.keys.contains(id)) {
            createUncertaintyId()
        } else {
            id
        }
    }

    private fun joinUncertainty(session: WebSocketSession, uncertaintyId: UncertaintyId) {
        val oldUncertaintyId = sessionUncertainty[session.sessionId]

        // Is new uncertainty even valid?
        val uncertainty = uncertainties[uncertaintyId]
        if (uncertainty == null) {
            session.sendMessage(UncertaintyNotFoundMessage(uncertaintyId))
            return
        }

        // Join this uncertainty
        sessionUncertainty[session.sessionId] = uncertaintyId
        session.sendMessage(UncertaintyJoinedMessage(uncertaintyId, uncertainty.info))

        // Notify users. If we left an old uncertainty, notify that too.
        notifyUsers(uncertaintyId)
        if (oldUncertaintyId != null) notifyUsers(oldUncertaintyId)
    }

    private fun notifyUsers(uncertaintyId: UncertaintyId) {
        val sessions = getUncertaintySessions(uncertaintyId)
        val msg = UncertaintyActiveUsersMessage(
                uncertaintyId = uncertaintyId,
                users = sessions.map { s -> UncertaintyJoinedUser(s.sessionId, s.username) }.toSet())

        sendToUncertaintySessions(uncertaintyId, msg)
    }

    private fun sendToUncertaintySessions(uncertaintyId: UncertaintyId, message: UncertaintyMessage) {
        val sessions = getUncertaintySessions(uncertaintyId)
        sessions.forEach { session -> session.sendMessage(message) }
    }

    private fun getUncertaintySessions(uncertaintyId: UncertaintyId): Set<WebSocketSession> {
        return sessionUncertainty
                .filter { (_, otherUncertaintyId) -> otherUncertaintyId == uncertaintyId }
                .mapNotNull { (sessionId, _) -> sessions[sessionId] }
                .toSet()
    }

}

