package codes.rik.deciderator.server

import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.Messages.UncertaintyMessage
import codes.rik.deciderator.types.SessionId
import codes.rik.deciderator.types.Username
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

val objectMapper = jacksonObjectMapper()
  .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
  .registerModule(Jdk8Module())
  .registerModule(JavaTimeModule());

fun WebSocketSession.sendMessage(message: UncertaintyMessage) {
  objectMapper.writeValueAsString(message)
    .also { logger.info { "[OUTGOING] $it" } }
    .also { sendMessage(TextMessage(objectMapper.writeValueAsString(message))) }
}

val WebSocketSession.sessionId get() = SessionId(this.id)
var WebSocketSession.username: Username
  set(username: Username) {
    this.attributes["username"] = username
  }
  get() = this.attributes["username"] as? Username ?: Username(this.sessionId.toString())
//var WebSocketSession.username: String
//  set(username) { attributes["username"] = username }
//  get() = attributes["username"] as? String ?: id

private val logger = KotlinLogging.logger {}
