package codes.rik.deciderator.server

import codes.rik.deciderator.types.SessionId
import codes.rik.deciderator.types.UncertaintyId
import codes.rik.deciderator.types.Username
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import mu.KotlinLogging
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
  private val sessions: MutableMap<SessionId, WebSocketSession> = mutableMapOf()
  private val sessionUncertainty: ConcurrentMap<SessionId, UncertaintyId> = ConcurrentHashMap()
  private val sessionDisposables: ConcurrentMap<SessionId, CompositeDisposable> = ConcurrentHashMap()
  val uncertaintySessionsSubject: BehaviorSubject<Map<UncertaintyId, Set<WebSocketSession>>> = BehaviorSubject.createDefault(mapOf())
  val sessionsSubject: BehaviorSubject<Set<WebSocketSession>> = BehaviorSubject.createDefault(setOf())

  val sessionIds get() = sessions.keys

  fun forEach(itr: (SessionId, WebSocketSession) -> Unit) = sessions.forEach(itr)

  fun addSession(session: WebSocketSession) {
    sessions[session.sessionId] = session
    updateSessionsSubject()
  }

  fun removeSession(session: WebSocketSession) {
    // Remove session and uncertainty link
    sessions.remove(session.sessionId)
    updateSessionsSubject()

    sessionUncertainty[session.sessionId]?.let { unlinkSessionUncertainty(session.sessionId, it) }
    sessionUncertainty.remove(session.sessionId)

    // Dispose of session subscriptions
    sessionDisposables.remove(session.sessionId)?.dispose()
  }

  /**
   * Associate [sessionId] with [uncertaintyId]
   * @return the previous [UncertaintyId] associated with [sessionId], if any.
   */
  fun linkSessionUncertainty(sessionId: SessionId, uncertaintyId: UncertaintyId) {
    sessionUncertainty[sessionId] = uncertaintyId
    updateSessionsSubject()
  }

  /**
   * Disassociates [sessionId] with [uncertaintyId]
   */
  fun unlinkSessionUncertainty(sessionId: SessionId, uncertaintyId: UncertaintyId) {
    sessionUncertainty.remove(sessionId, uncertaintyId)
    updateSessionsSubject()
  }

  fun getSessionUncertainty(sessionId: SessionId) = sessionUncertainty[sessionId]

  fun setUsername(session: WebSocketSession, username: Username) {
    session.username = username
    sessionUncertainty[session.sessionId]?.let {
      updateSessionsSubject()
    }
  }
  fun getDisposable(sessionId: SessionId): CompositeDisposable = sessionDisposables.computeIfAbsent(sessionId) { CompositeDisposable() }

  private fun updateSessionsSubject() {
    sessionsSubject.onNext(sessions.values.toSet())
  }

//  private fun updateUncertaintySessionsSubject() {
//    sessionUncertainty.entries
//      .groupBy { (_, uncertaintyId) -> uncertaintyId }
//      .mapValues { (_, entries) -> entries.map { it.key } }
//      .mapValues { (_, sessionIds) -> sessionIds.map { sessions[it] }.filterNotNull().toSet() }
//      .also { uncertaintySessionsSubject.onNext(it) }
//  }

//  fun getUncertaintySessionsSubject(uncertaintyId: UncertaintyId): BehaviorSubject<Set<WebSocketSession>> {
//    return uncertaintySessionsSubject.computeIfAbsent(uncertaintyId) { BehaviorSubject.createDefault(getUncertaintySessions(uncertaintyId))}
//  }

}

data class SessionNotFoundException(val id: SessionId) : RuntimeException("Session not found: $id")

private val logger = KotlinLogging.logger {}
