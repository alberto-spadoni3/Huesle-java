package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.model.GameManager;
import it.unibo.sd.project.mastermind.model.user.UserManager;

public class Main {
    public static void main(String[] args) {
        boolean forTesting = false;
        UserManager userManager = new UserManager(forTesting);
        GameManager gameManager = new GameManager(forTesting);
    }
}
