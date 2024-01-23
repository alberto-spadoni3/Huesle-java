package it.unibo.sd.project.mastermind.controllers;

import com.mongodb.client.MongoDatabase;
import it.unibo.sd.project.mastermind.SettingsRequest;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.user.UserOperationResult;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class SettingsController {
    private final DBManager<Player> userDB;

    public SettingsController(MongoDatabase database) {
        this.userDB = new DBManager<>(database, "users", "username", Player.class);
    }

    public Function<String, String> getSettings() {
        return username -> {
            AtomicReference<UserOperationResult> userOperationResult = new AtomicReference<>();
            try {
                Optional<Player> playerOptional = userDB.getDocumentByField("username", username);
                playerOptional.ifPresent(player -> userOperationResult.set(
                        new UserOperationResult(
                            (short) 200,
                            "Returning requested settings...",
                            player)));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (userOperationResult.get() == null)
                    userOperationResult.set(new UserOperationResult(
                            (short) 400,
                            "No data available: player not found"));
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(userOperationResult.get());
        };
    }

    public Function<String, String> updateAccessibilitySettings() {
        return message -> {
            AtomicReference<UserOperationResult> userOperationResult = new AtomicReference<>();
            try {
                SettingsRequest settingsRequest = Presentation.deserializeAs(message, SettingsRequest.class);
                String requesterUsername = settingsRequest.getRequesterUsername();
                Optional<Player> playerOptional = userDB.getDocumentByField(
                        "username",
                        requesterUsername);
                playerOptional.ifPresent(player -> {
                    player.setAccessibilitySettings(settingsRequest.getAccessibilitySettings());
                    userDB.update(requesterUsername, player);
                    userOperationResult.set(new UserOperationResult(
                            (short) 200,
                            "Accessibility settings updated successfully!"));
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (userOperationResult.get() == null)
                    userOperationResult.set(new UserOperationResult(
                            (short) 400,
                            "Update not possible: player not found"));
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(userOperationResult.get());
        };
    }

    public Function<String, String> updateProfilePictureID() {
        return message -> {
            AtomicReference<UserOperationResult> userOperationResult = new AtomicReference<>();
            try {
                SettingsRequest settingsRequest = Presentation.deserializeAs(message, SettingsRequest.class);
                String requesterUsername = settingsRequest.getRequesterUsername();
                Optional<Player> playerOptional = userDB.getDocumentByField(
                        "username",
                        requesterUsername);
                playerOptional.ifPresent(player -> {
                    player.setProfilePictureID(settingsRequest.getProfilePictureID());
                    userDB.update(requesterUsername, player);
                    userOperationResult.set(new UserOperationResult(
                            (short) 200,
                            "Profile picture updated successfully!"));
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (userOperationResult.get() == null)
                    userOperationResult.set(new UserOperationResult(
                            (short) 400,
                            "Update not possible: player not found"));
            }
            return Presentation.serializerOf(UserOperationResult.class).serialize(userOperationResult.get());
        };
    }
}
