package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.model.match.Match;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Player player; // the player associated to this game session
    private List<Match> activeMatches;
    private List<Match> endedMatches;

    public Game(Player player) {
        this.player = player;
        activeMatches = new ArrayList<>();
        endedMatches = new ArrayList<>();
    }
}
