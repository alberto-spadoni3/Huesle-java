package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.*;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchOperationResult;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.model.user.UserOperationResult;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.ArrayList;
import java.util.List;

public class OperationResultDeserializer extends AbstractJsonDeserializer<OperationResult> {
    @Override
    protected OperationResult deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            short statusCode = result.get("statusCode").getAsShort();
            String message = result.get("resultMessage").getAsString();

            try {
                // in case jsonElement is a UserOpResult
                if (result.has("relatedUser") && result.get("relatedUser").isJsonObject()) {
                    JsonObject jsonPlayer = (JsonObject) result.get("relatedUser");
                    String accessToken = result.get("accessToken").getAsString();
                    Player player = Presentation.deserializeAs(jsonPlayer.toString(), Player.class);
                    return new UserOperationResult(statusCode, message, player, accessToken);
                }

                // in case jsonElement is a MatchOpResult
                if (result.has("matchAccessCode") && result.get("matchAccessCode").isJsonPrimitive()) {
                    String matchAccessCode = result.get("matchAccessCode").getAsString();
                    return new MatchOperationResult(statusCode, message, matchAccessCode);
                } else if (result.has("matches") && result.get("matches").isJsonArray()) {
                    List<Match> matches = new ArrayList<>();
                    JsonArray jsonMatches = result.get("matches").getAsJsonArray();
                    for (JsonElement element : jsonMatches)
                        matches.add(Presentation.deserializeAs(element.toString(), Match.class));
                    boolean pending = result.get("pending").getAsBoolean();
                    return new MatchOperationResult(statusCode, message, matches, pending);
                }

                // in case jsonElement is a GuessOpResult
                if (result.has("updatedStatus") && result.has("submittedAttemptHints")) {
                    JsonObject jsonUpdatedStatus = result.get("updatedStatus").getAsJsonObject();
                    MatchStatus updatedStatus = Presentation.deserializeAs(jsonUpdatedStatus.toString(), MatchStatus.class);
                    JsonObject jsonAttemptHints = result.get("submittedAttemptHints").getAsJsonObject();
                    Hints hints = Presentation.deserializeAs(jsonAttemptHints.toString(), Hints.class);
                    return new GuessOperationResult(statusCode, message, updatedStatus, hints);
                }
            } catch (Exception e ) {
                System.out.println(e.getMessage());
            }

            // otherwise, return a basic OpResult
            return new OperationResult(statusCode, message);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as OperationResult");
        }
    }
}
