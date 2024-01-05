package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.SecretCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Match {
    private final UUID matchID;
    private List<Attempt> madeAttempts;
    private final SecretCode secretCode;
    private MatchStatus matchStatus;

    // Constructor used when creating a new match
    public Match(List<Player> players) {
        this.matchID = UUID.randomUUID();
        this.secretCode = new SecretCode();
        this.matchStatus = new MatchStatus(players);
        this.madeAttempts = new ArrayList<>();
    }

    // Constructor used when the match is taken from the database
    public Match(UUID matchID, MatchStatus status, List<Attempt> attempts, SecretCode code) {
        this.matchID = matchID;
        this.matchStatus = status;
        this.madeAttempts = attempts;
        this.secretCode = code;
    }

    public void tryToGuess(Attempt attempt) {
        //TODO
        attempt.computeHints(this.secretCode);
        madeAttempts.add(attempt);
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
}
