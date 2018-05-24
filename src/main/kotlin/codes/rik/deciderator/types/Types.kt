package codes.rik.deciderator.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.util.*

interface IdValueType {
    @get:JsonValue val id: String
}

data class UncertaintyId(override val id: String): IdValueType {
    companion object {
        @JsonCreator @JvmStatic fun uncertaintyId(id: String) = UncertaintyId(id)
        fun create() = UncertaintyId(UUID.randomUUID().toString())
    }
}

data class SessionId(override val id: String): IdValueType

data class UncertaintyJoinedUser(
        val sessionId: SessionId,
        val name: String)