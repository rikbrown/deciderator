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
  val sessionsSubject: BehaviorSubject<Set<WebSocketSession>> = BehaviorSubject.createDefault(setOf())

  fun addSession(session: WebSocketSession) {
    logger.info { "New session registered: ${session.sessionId}" }
    sessions[session.sessionId] = session
    updateSessionsSubject()
  }

  fun removeSession(session: WebSocketSession) {
    logger.info { "Removing session: ${session.sessionId}" }

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

  /**
   * Returns the [UncertaintyId], if any, associated with this session.
   */
  fun getSessionUncertainty(sessionId: SessionId) = sessionUncertainty[sessionId]

  /**
   * Sets the username associated with this session.
   */
  fun setUsername(session: WebSocketSession, username: Username) {
    session.username = username
    sessionUncertainty[session.sessionId]?.let {
      updateSessionsSubject()
    }
  }

  /**
   * Return a [CompositeDisposable] for [sessionId], which will be disposed when the session is removed.
   */
  fun getDisposable(sessionId: SessionId): CompositeDisposable
    = sessionDisposables.computeIfAbsent(sessionId) { CompositeDisposable() }

  private fun updateSessionsSubject() {
    sessionsSubject.onNext(sessions.values.toSet())
  }

}

private val logger = KotlinLogging.logger {}
