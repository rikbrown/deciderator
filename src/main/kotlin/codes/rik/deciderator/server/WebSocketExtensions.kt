import codes.rik.deciderator.types.SessionId
import codes.rik.deciderator.types.UncertaintyMessage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.experimental.async
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

val objectMapper = jacksonObjectMapper()

fun WebSocketSession.sendMessage(message: UncertaintyMessage) {
    //async {
        System.err.println(message)
        sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    //}
}
val WebSocketSession.sessionId get() = SessionId(this.id)
var WebSocketSession.username: String
    set(username) { attributes["username"] = username }
    get() = attributes["username"] as? String ?: id


