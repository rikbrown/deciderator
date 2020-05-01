package codes.rik.deciderator.types

import codes.rik.deciderator.CoinState
import com.fasterxml.jackson.annotation.JsonTypeInfo

object Messages {

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  interface DecideratorRequest
  data class CreateUncertaintyRequest(
    val name: String,
    val options: Set<String>,
    val bestOf: Int
  ) : DecideratorRequest

  data class JoinUncertaintyRequest(val uncertaintyId: UncertaintyId) : DecideratorRequest
  data class LeaveUncertaintyRequest(val uncertaintyId: UncertaintyId) : DecideratorRequest
  data class GetUncertaintyRequest(val uncertaintyId: UncertaintyId) : DecideratorRequest
  data class SetUsernameRequest(val username: String) : DecideratorRequest
  data class UpdateCoinStyleRequest(val uncertaintyId: UncertaintyId, val coinStyle: String) : DecideratorRequest
  data class UpdateCoinStateRequest(val uncertaintyId: UncertaintyId, val coinState: CoinState) : DecideratorRequest
  data class FlipCoinRequest(val uncertaintyId: UncertaintyId) : DecideratorRequest
  data class NextRoundRequest(val uncertaintyId: UncertaintyId): DecideratorRequest

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

  data class CoinStateMessage(
    val uncertaintyId: UncertaintyId,
    val coinState: CoinState
  ) : UncertaintyMessage
}
