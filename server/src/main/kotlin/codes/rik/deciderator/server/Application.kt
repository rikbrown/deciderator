package codes.rik.deciderator.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@SpringBootApplication
open class DecideratorApplication

fun main(args: Array<String>) {
  runApplication<DecideratorApplication>(*args)
}

@Configuration
@EnableWebSocket
open class WSConfig : WebSocketConfigurer {
  override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
    registry.addHandler(DecideratorHandler, "/handler")
      .setAllowedOrigins("*")
      .withSockJS()
  }
}
