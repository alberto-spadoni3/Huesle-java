package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.user.Player;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MatchStatus {
    private MatchState matchState;
    private final List<Player> players;
    private String nextPlayer;
    private boolean abandoned;

    public MatchStatus(List<Player> players) {
        this.matchState = MatchState.PLAYING;
        this.players = players;
        this.nextPlayer = extractFirstPlayer();
    }

    public MatchStatus(List<Player> players, MatchState matchState, String nextPlayer) {
        this.players = players;
        this.matchState = matchState;
        this.nextPlayer = nextPlayer;
    }

    public MatchState getState() {
        return matchState;
    }

    public String getNextPlayer() {
        return nextPlayer;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void updatePlayers(List<Player> updatedPlayers) {
        this.players.clear();
        this.players.addAll(updatedPlayers);
    }

    public boolean isAbandoned() {
        return abandoned;
    }

    public void setAbandoned() {
        this.abandoned = true;
    }

    public void changeNextPlayer(String player) {
        this.nextPlayer = player;
    }

    public void setState(MatchState newState) {
        this.matchState = newState;
    }

    private String extractFirstPlayer() {
        return this.players.get(new Random().nextInt(2)).getUsername();
    }

    public void switchPlayer() {
        Optional<Player> player = this.players.stream().filter((p) -> !p.getUsername().equals(nextPlayer)).findFirst();
        player.ifPresentOrElse(
                p -> this.nextPlayer = p.getUsername(),
                () -> { throw new RuntimeException("Problems in changing the player for the next turn"); });
    }
}
