package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.Player;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MatchStatus {
    private MatchState matchState;
    private final List<Player> players;
    private Player nextPlayer;
    private boolean abandoned;

    public MatchStatus(List<Player> players) {
        this.matchState = MatchState.PLAYING;
        this.players = players;
        this.nextPlayer = extractFirstPlayer();
    }

    public MatchStatus(List<Player> players, MatchState matchState, Player nextPlayer) {
        this.players = players;
        this.matchState = matchState;
        this.nextPlayer = nextPlayer;
    }

    public void setMatchState(MatchState matchState) {
        this.matchState = matchState;
    }

    public void setNextPlayer(Player nextPlayer) {
        this.nextPlayer = nextPlayer;
    }

    public MatchState getState() {
        return matchState;
    }

    public Player getNextPlayer() {
        return nextPlayer;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isAbandoned() {
        return abandoned;
    }

    public void setAbandoned() {
        this.abandoned = true;
    }

    public void changeNextPlayer(Player player) {
        this.nextPlayer = player;
    }

    public void setState(MatchState newState) {
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
}
