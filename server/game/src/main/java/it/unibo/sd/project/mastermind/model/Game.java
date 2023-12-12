package it.unibo.sd.project.mastermind.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Player player; // the player associated to this game session
    private List<Math> activeMatches;
    private List<Math> endedMatches;

    public Game(Player player) {
        this.player = player;
        activeMatches = new ArrayList<>();
        endedMatches = new ArrayList<>();
    }
}
