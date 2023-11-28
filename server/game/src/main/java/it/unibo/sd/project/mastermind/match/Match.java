package it.unibo.sd.project.mastermind.match;

import it.unibo.sd.project.mastermind.Attempt;
import it.unibo.sd.project.mastermind.Player;
import it.unibo.sd.project.mastermind.SecretCode;

import java.util.List;
import java.util.UUID;

public class Match {
    private final UUID matchID;
    private List<Attempt> madeAttempts;
    private final SecretCode secretCode;
    private MatchStatus matchStatus;
    // private List<Player> players;

    public Match(List<Player> players) {
        this.matchID = UUID.randomUUID();
        this.secretCode = new SecretCode();
        this.matchStatus = new MatchStatus(players);
    }

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
}
