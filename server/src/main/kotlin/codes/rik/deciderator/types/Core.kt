package codes.rik.deciderator.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.apache.commons.lang3.RandomStringUtils

interface IdValueType {
  @get:JsonValue
  val id: String
}

data class UncertaintyId(override val id: String): IdValueType {
  companion object {
    @JsonCreator @JvmStatic fun uncertaintyId(id: String) = UncertaintyId(id)

    fun create() = UncertaintyId(RandomStringUtils.randomAlphanumeric(3).toUpperCase())
  }
}

data class Username(override val id: String): IdValueType {
  val username = id;
  override fun toString() = id
}

data class SessionId(override val id: String): IdValueType {
  override fun toString() = id
}
