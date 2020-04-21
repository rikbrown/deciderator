package codes.rik.deciderator.types

import com.fasterxml.jackson.annotation.JsonTypeInfo

object Messages {

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  interface UncertaintyRequest
  data class CreateUncertaintyRequest(
    val name: String,
    val options: Set<String>
  ) : UncertaintyRequest

  data class JoinUncertaintyRequest(val uncertaintyId: UncertaintyId) : UncertaintyRequest
  data class LeaveUncertaintyRequest(val uncertaintyId: UncertaintyId) : UncertaintyRequest
  data class GetUncertaintyRequest(val uncertaintyId: UncertaintyId) : UncertaintyRequest
  data class SetUsernameRequest(val username: String) : UncertaintyRequest

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  interface UncertaintyMessage
  abstract class UncertaintyErrorMessage(val error: String) : UncertaintyMessage

  data class ActiveSessionsMessage(
    val sessionId: SessionId,
    val onlineSessionIds: Set<SessionId>
  ) : UncertaintyMessage

  data class UncertaintyCreatedMessage(val uncertaintyId: UncertaintyId) : UncertaintyMessage
  data class UncertaintyUsersMessage(
    val uncertaintyId: UncertaintyId,
    val username: Username,
    val users: List<Username>
  ) : UncertaintyMessage

  data class UncertaintyJoinedMessage(val uncertainty: Uncertainty) : UncertaintyMessage

  data class UncertaintyDetailsMessage(
    val uncertainty: Uncertainty
  ) : UncertaintyMessage
}
