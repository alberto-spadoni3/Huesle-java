package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.SecretCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Match {
    public static final byte ATTEMPTS = 10;
    private final UUID matchID;
    private MatchStatus matchStatus;
    private List<Attempt> madeAttempts;
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

    public void tryToGuess(Attempt attempt) {
        if (matchStatus.getNextPlayer().equals(attempt.getPlayer().getUsername())) {
            attempt.computeHints(this.secretCode);
            madeAttempts.add(attempt);
            remainingAttempts--;
            if (attempt.getHints().getRightPositions() == SecretCode.COLOR_SEQUENCE_LENGTH)
                matchStatus.setState(MatchState.VICTORY);
            else if (remainingAttempts > 0)
                matchStatus.switchPlayer();
            else
                matchStatus.setState(MatchState.DRAW);
        }
    }

    public boolean isNotOver() {
        return !List.of(MatchState.DRAW, MatchState.VICTORY).contains(matchStatus.getState());
    }

    public boolean isPlayerTurn(String playerUsername) {
        return matchStatus.getNextPlayer().equals(playerUsername);
    }
}
