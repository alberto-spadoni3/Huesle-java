package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

public class MatchDeserializerTests {
    @Test
    void matchDeserializerTest() {
        Match m = Presentation.deserializerOf(Match.class).deserialize(getMatchAsJson());
        System.out.println(m.getMatchID());
        System.out.println(m.getMadeAttempts().size());
        System.out.println(m.getMatchStatus().getPlayers().size());
        System.out.println(m.getSecretCode().getCode());
    }

    private String getMatchAsJson() {
        return "{\"UUID\":\"a55ae86e-44e4-4883-91ee-9ae3efbbc439\",\"matchStatus\":{\"matchState\":\"PLAYING\",\"players\":[{\"username\":\"alice\",\"email\":\"alice@huesle.it\",\"password\":\"$2a$14$LaQ3nBUITMpv8zXqQJCmfeeQOVgRfWs1dtgD07qaA2dlNVcHQU.5G\",\"refreshToken\":\"\",\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":true,\"colorblindMode\":false}},{\"username\":\"bob\",\"email\":\"bob@huesle.it\",\"password\":\"$2a$14$tC/kGYDi3tG4RXSaH9ev4uVXrduWhjsNCURGOOr0KpqV7O4H6sNtu\",\"refreshToken\":\"\",\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":true,\"colorblindMode\":false}}],\"nextPlayer\":{\"username\":\"bob\",\"email\":\"bob@huesle.it\",\"password\":\"$2a$14$tC/kGYDi3tG4RXSaH9ev4uVXrduWhjsNCURGOOr0KpqV7O4H6sNtu\",\"refreshToken\":\"\",\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":true,\"colorblindMode\":false}}},\"attempts\":{},\"secretCode\":[\"coral\",\"forestgreen\",\"forestgreen\",\"coral\"]}";
    }
}
