package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.user.Player;

import java.util.ArrayList;
import java.util.List;

public class Game {
    // TODO: to delete
    private final Player player; // the player associated to this game session
    private List<Match> activeMatches;
    private List<Match> endedMatches;

    public Game(Player player) {
        this.player = player;
        activeMatches = new ArrayList<>();
        endedMatches = new ArrayList<>();
    }

    public void setActiveMatches(List<Match> activeMatches){
        this.activeMatches = activeMatches;
    }
    public void setEndedMatches(List<Match> endedMatches) {
        this.endedMatches = endedMatches;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Match> getActiveMatches() {
        return activeMatches;
    }

    public List<Match> getEndedMatches() {
        return endedMatches;
    }
}
