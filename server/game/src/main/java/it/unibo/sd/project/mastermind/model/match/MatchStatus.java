package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.Player;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MatchStatus {
    private MatchState matchState;
    private final List<Player> players;
    private Player nextPlayer;
    private byte remainingAttempts;

    public MatchStatus(List<Player> players) {
        this.matchState = MatchState.PLAYING;
        this.players = players;
        this.nextPlayer = extractFirstPlayer();
        this.remainingAttempts = (byte) 10;
    }

    public void changeNextPlayer(Player player) {
        this.nextPlayer = player;
    }

    public void changeState(MatchState newState) {
        this.matchState = newState;
    }

    private Player extractFirstPlayer() {
        return this.players.get(new Random().nextInt(2));
    }

    public void switchPlayer() {
        Optional<Player> player = this.players.stream().filter((p) -> p != nextPlayer).findFirst();
        player.ifPresentOrElse(
                value -> this.nextPlayer = value,
                () -> { throw new RuntimeException("Problems in changing the player for the next turn"); });
    }

    public void decrementRemainingAttempts() {
        this.remainingAttempts--;
    }

    public MatchState getMatchState() {
        return matchState;
    }

    public Player getNextPlayer() {
        return nextPlayer;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
