package codes.rik.deciderator.server

import codes.rik.deciderator.types.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
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
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val msg = objectMapper.readValue<UncertaintyRequest>(message.payload)
        when (msg) {
            is SetUsernameRequest -> setUsername(session, msg.username)
            is CreateUncertaintyRequest -> createUncertainty(session)
            is JoinUncertaintyRequest -> joinUncertainty(session, msg.uncertaintyId)
        }
    }

    private fun setUsername(session: WebSocketSession, username: String) {
        session.username = username
        session.sendMessage(UsernameSetMessage(username))
    }

    private fun createUncertainty(session: WebSocketSession) {
        val uncertaintyId = UncertaintyId.create()
        val uncertainty = Uncertainty(uncertaintyId)
        uncertainties[uncertaintyId] = uncertainty

        session.sendMessage(UncertaintyCreatedMessage(uncertaintyId))
    }

    private fun joinUncertainty(session: WebSocketSession, uncertaintyId: UncertaintyId) {
        val oldUncertaintyId = sessionUncertainty[session.sessionId]

        // Is new uncertainty even valid?
        if (uncertainties[uncertaintyId] == null) {
            session.sendMessage(UncertaintyNotFoundMessage(uncertaintyId))
            return
        }

        // Join this uncertainty
        sessionUncertainty[session.sessionId] = uncertaintyId
        session.sendMessage(UncertaintyJoinedMessage(uncertaintyId))

        // Notify users. If we left an old uncertainty, notify that too.
        notifyUsers(uncertaintyId)
        if (oldUncertaintyId != null) notifyUsers(oldUncertaintyId)
    }

    private fun notifyUsers(uncertaintyId: UncertaintyId) {
        val sessions = getUncertaintySessions(uncertaintyId)

        sessions.forEach { session ->
            session.sendMessage(UncertaintyActiveUsersMessage(
                uncertaintyId = uncertaintyId,
                users = sessions.map { s -> UncertaintyJoinedUser(s.sessionId, s.username) }.toSet()))
        }
    }

    private fun getUncertaintySessions(uncertaintyId: UncertaintyId): Set<WebSocketSession> {
        return sessionUncertainty
                .filter { (_, otherUncertaintyId) -> otherUncertaintyId == uncertaintyId }
                .mapNotNull { (sessionId, _) -> sessions[sessionId] }
                .toSet()
    }

}

private val objectMapper = jacksonObjectMapper()

private fun WebSocketSession.sendMessage(message: UncertaintyMessage) = sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
private val WebSocketSession.sessionId get() = SessionId(this.id)
private var WebSocketSession.username: String
    set(username) { attributes["username"] = username }
    get() = attributes["username"] as? String ?: id


