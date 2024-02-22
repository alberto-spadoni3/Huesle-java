package it.unibo.sd.project.mastermind;

public class Main {
    public static void main(String[] args) {
        boolean forTesting = false;
        new UserManager(forTesting);
        new GameManager(forTesting);
    }
}
