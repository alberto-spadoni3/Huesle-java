package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.user.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Match {
    public static final byte ATTEMPTS = 10;
    private final UUID matchID;
    private final MatchStatus matchStatus;
    private final List<Attempt> madeAttempts;
    private byte remainingAttempts;
    private final SecretCode secretCode;

    // Constructor used when creating a new match
    public Match(List<Player> players) {
        this.matchID = UUID.randomUUID();
        this.secretCode = new SecretCode();
        this.matchStatus = new MatchStatus(players);
        this.madeAttempts = new ArrayList<>();
        this.remainingAttempts = ATTEMPTS;
    }

    public Match(UUID matchID, MatchStatus status, List<Attempt> attempts, SecretCode code) {
        this.matchID = matchID;
        this.matchStatus = status;
        this.madeAttempts = attempts;
        this.secretCode = code;
        this.remainingAttempts = (byte) (ATTEMPTS - madeAttempts.size());
    }

    public void tryToGuess(Attempt attempt) throws RuntimeException {
        if (!matchStatus.getNextPlayer().equals(attempt.madeBy()))
            throw new RuntimeException("This is not " + attempt.madeBy() + " turn.");

        attempt.computeHints(this.secretCode);
        madeAttempts.add(attempt);
        remainingAttempts--;
        if (attempt.getHints().getRightPositions() == SecretCode.COLOR_SEQUENCE_LENGTH)
            matchStatus.setState(MatchState.VICTORY);
        else if (remainingAttempts > 0)
            matchStatus.changeTurn();
        else
            matchStatus.setState(MatchState.DRAW);
    }

    public UUID getMatchID() {
        return matchID;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public List<Attempt> getMadeAttempts() {
        return madeAttempts;
    }

    public SecretCode getSecretCode() {
        return secretCode;
    }

    public boolean isNotOver() {
        return !List.of(MatchState.DRAW, MatchState.VICTORY).contains(matchStatus.getState());
    }

    public boolean isPlayerTurn(String playerUsername) {
        return matchStatus.getNextPlayer().equals(playerUsername);
    }
}
