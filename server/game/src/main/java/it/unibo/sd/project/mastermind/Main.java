package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.model.GameManager;
import it.unibo.sd.project.mastermind.model.UserManager;

public class Main {
    public static void main(String[] args) {
        boolean forTesting = false;
        new UserManager(forTesting);
        new GameManager(forTesting);
    }
}
