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

interface NextRoundRequest extends DecideratorRequest {
  uncertaintyId: string;
}

interface SetUsernameRequest extends DecideratorRequest {
  username: string;
}

interface UncertaintyCreatedMessage extends UncertaintyMessage {
  uncertaintyId: string;
}

interface RoundData {
}

type CoinFace = "HEADS" | "TAILS";

interface FlipResult {
  coinStyle: string;
  flipTime: number;
  flippedBy: string;
  result: CoinFace;
  waitTime: number;
}

interface Round {
  coinStyle: string;
  data: RoundData;
  results: FlipResult[];
  winningFace: CoinFace | null;
}

interface UncertaintyOption {
  coinStyle: string;
  eliminated: boolean;
  name: string;
  startedLoopEliminated: boolean;
}

interface UncertaintyRules {
  bestOf: number;
  finalTwoHeadToHead: boolean;
  startingBestOf: number;
}

interface Winner {
  coinStyle: string;
  face: CoinFace;
  name: string;
}

interface Uncertainty {
  currentRound: Round;
  id: string;
  name: string;
  options: UncertaintyOption[];
  rules: UncertaintyRules;
  winner: Winner | null;
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

interface HeadToHeadRound extends RoundData {
  headsOption: string;
  tailsOption: string;
}

interface MeaningfulVoteRound extends RoundData {
  customRules: UncertaintyRules | null;
  option: string;
}
