package codes.rik.deciderator.server

import dagger.Component
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import javax.inject.Singleton

fun main(args: Array<String>) {
  runApplication<DecideratorApplication>(*args)
}

@SpringBootApplication
open class DecideratorApplication

@Configuration
@EnableWebSocket
open class WSConfig : WebSocketConfigurer {
  private val component: DecideratorComponent = DaggerDecideratorComponent.create()
  override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
    registry.addHandler(component.handler, "/handler")
      .setAllowedOrigins("*")
      .withSockJS()
  }
}

@Singleton
@Component
interface DecideratorComponent {
  val handler: DecideratorHandler
}
