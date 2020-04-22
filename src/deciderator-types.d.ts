interface UncertaintyMessage {
}

interface ActiveSessionsMessage extends UncertaintyMessage {
  onlineSessionIds: string[];
  sessionId: string;
}

interface Quaternion {
  w: number;
  x: number;
  y: number;
  z: number;
}

interface DeltaXY {
  x: number;
  y: number;
}

interface CoinState {
  drag: number;
  interactive: boolean;
  quaternion: Quaternion | null;
  rotateDelta: DeltaXY;
  rotationSpeed: number;
}

interface CoinStateMessage extends UncertaintyMessage {
  coinState: CoinState;
  uncertaintyId: string;
}

interface DecideratorRequest {
}

interface CreateUncertaintyRequest extends DecideratorRequest {
  name: string;
  options: string[];
}

interface FlipCoinRequest extends DecideratorRequest {
  uncertaintyId: string;
}

interface GetUncertaintyRequest extends DecideratorRequest {
  uncertaintyId: string;
}

interface JoinUncertaintyRequest extends DecideratorRequest {
  uncertaintyId: string;
}

interface LeaveUncertaintyRequest extends DecideratorRequest {
  uncertaintyId: string;
}

interface SetUsernameRequest extends DecideratorRequest {
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

interface UpdateCoinStateRequest extends DecideratorRequest {
  coinState: CoinState;
  uncertaintyId: string;
}

interface UpdateCoinStyleRequest extends DecideratorRequest {
  coinStyle: string;
  uncertaintyId: string;
}

