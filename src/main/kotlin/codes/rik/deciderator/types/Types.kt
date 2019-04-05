package codes.rik.deciderator.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.apache.commons.lang3.RandomStringUtils
import java.security.SecureRandom
import java.util.*

interface IdValueType {
    @get:JsonValue val id: String
}

data class UncertaintyId(override val id: String): IdValueType {
    companion object {
        @JsonCreator @JvmStatic fun uncertaintyId(id: String) = UncertaintyId(id)

        fun create() = UncertaintyId(RandomStringUtils.randomAlphanumeric(3).toUpperCase())
    }
}

data class SessionId(override val id: String): IdValueType

data class UncertaintyJoinedUser(
        val sessionId: SessionId,
        val name: String)

data class Rotation(
        val x: Double = 0.0,
        val y: Double = 0.0,
        val z: Double = 0.0)

enum class CoinFlipResult {
    HEADS, TAILS
}

enum class CoinStyle {
    EU_GERMANY,
    FIRST_WORLD_WAR,
    GERMANY,
}