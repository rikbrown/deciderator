interface UncertaintyMessage {
}

interface ActiveSessionsMessage extends UncertaintyMessage {
  onlineSessionIds: string[];
  sessionId: string;
}

interface UncertaintyRequest {
}

interface CreateUncertaintyRequest extends UncertaintyRequest {
  name: string;
  options: string[];
}

interface GetUncertaintyRequest extends UncertaintyRequest {
  uncertaintyId: string;
}

interface JoinUncertaintyRequest extends UncertaintyRequest {
  uncertaintyId: string;
}

interface LeaveUncertaintyRequest extends UncertaintyRequest {
  uncertaintyId: string;
}

interface SetUsernameRequest extends UncertaintyRequest {
  username: string;
}

interface UncertaintyCreatedMessage extends UncertaintyMessage {
  uncertaintyId: string;
}

type CoinFace = "HEADS" | "TAILS";

interface FlipResult {
  coinStyle: string;
  flipTime: number;
  flippedBy: string;
  result: CoinFace;
  waitTime: number;
}

interface ActiveOptionProperties {
  coinStyle: string;
  results: FlipResult[];
  roundComplete: boolean;
}

interface UncertaintyOption {
  active: ActiveOptionProperties | null;
  eliminated: boolean;
  name: string;
}

interface UncertaintyRules {
  bestOf: number;
  finalTwoHeadToHead: boolean;
}

interface Uncertainty {
  id: string;
  name: string;
  options: UncertaintyOption[];
  rules: UncertaintyRules;
}

interface UncertaintyDetailsMessage extends UncertaintyMessage {
  uncertainty: Uncertainty;
}

interface UncertaintyErrorMessage extends UncertaintyMessage {
  error: string;
}

interface UncertaintyJoinedMessage extends UncertaintyMessage {
  uncertainty: Uncertainty;
}

interface UncertaintyUsersMessage extends UncertaintyMessage {
  uncertaintyId: string;
  username: string;
  users: string[];
}

