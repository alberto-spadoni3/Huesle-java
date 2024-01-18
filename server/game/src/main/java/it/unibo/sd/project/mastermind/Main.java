package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.model.GameManager;
import it.unibo.sd.project.mastermind.model.user.UserManager;

public class Main {
    public static void main(String[] args) {
        boolean forTesting = false;
        new UserManager(forTesting);
        new GameManager(forTesting);
    }

    // TODO LIST
    //  adjust client code in order to implement SockJS communication with the server
    //  settings: profileSetting, updatePassword, profilePics
    //  stats: userStats, notifications, newNotifications
    //  refactor WebServer so that getHandler applies a consumer that returns a JsonObject instead of a String
}
