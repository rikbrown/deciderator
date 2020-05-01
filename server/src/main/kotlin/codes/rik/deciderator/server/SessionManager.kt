package codes.rik.deciderator.server

import codes.rik.deciderator.types.SessionId
import codes.rik.deciderator.types.UncertaintyId
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
  private val sessions: ConcurrentMap<SessionId, WebSocketSession> = ConcurrentHashMap()
  private val sessionUncertainty: ConcurrentMap<SessionId, UncertaintyId> = ConcurrentHashMap()

  val sessionIds get() = sessions.keys

  fun forEach(itr: (SessionId, WebSocketSession) -> Unit) = sessions.forEach(itr)

  fun addSession(session: WebSocketSession) {
    sessions[session.sessionId] = session
  }

  fun removeSession(session: WebSocketSession) {
    sessions.remove(session.sessionId)
  }

  /**
   * Associate [sessionId] with [uncertaintyId]
   * @return the previous [UncertaintyId] associated with [sessionId], if any.
   */
  fun linkSessionUncertainty(sessionId: SessionId, uncertaintyId: UncertaintyId): UncertaintyId? {
    return sessionUncertainty.put(sessionId, uncertaintyId)
  }

  /**
   * Disassociates [sessionId] with [uncertaintyId]
   */
  fun unlinkSessionUncertainty(sessionId: SessionId, uncertaintyId: UncertaintyId) {
    sessionUncertainty.remove(sessionId, uncertaintyId)
  }

  fun getSessionUncertainty(sessionId: SessionId) = sessionUncertainty[sessionId]

  fun getUncertaintySessions(uncertaintyId: UncertaintyId): Set<WebSocketSession> {
    return sessionUncertainty
      .filterValues { it == uncertaintyId }
      .keys
      .mapNotNull { sessions[it] }
      .toSet()
  }

}
