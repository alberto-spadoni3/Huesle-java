package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.model.match.*;
import it.unibo.sd.project.mastermind.model.user.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameLogicTests {
    private final Player player1 = new Player("fabio", "fabiofazio@huesle.it", "password");
    private final Player player2 = new Player("Anna", "anna@huesle.it", "password");
    private final ArrayList<String> secretCode = new ArrayList<>(
        List.of("gold", "crimson", "mediumblue", "rebeccapurple"));
    private final Match match = getMatch();

    @Test
    void testInitialConditions() {
        Match newMatch = new Match(List.of(player1, player2));
        MatchStatus matchStatus = newMatch.getMatchStatus();

        assertEquals(MatchState.PLAYING, matchStatus.getState());
        assertTrue(newMatch.isNotOver());
        assertEquals(0, newMatch.getMadeAttempts().size());
    }

    @Test
    void testHintsComputation() {
        Attempt attempt1 = new Attempt(List.of("crimson", "gold", "coral", "coral"),
            match.getMatchStatus().getNextPlayer());
        match.tryToGuess(attempt1);
        assertEquals(1, match.getMadeAttempts().size());

        // given attempt1, the hints computation process should conclude with rightPos = 0 and rightCol = 2
        assertEquals(0, attempt1.getHints().getRightPositions());
        assertEquals(2, attempt1.getHints().getRightColours());

        Attempt attempt2 = new Attempt(List.of("gold", "coral", "rebeccapurple", "forestgreen"),
            match.getMatchStatus().getNextPlayer());
        match.tryToGuess(attempt2);
        assertEquals(2, match.getMadeAttempts().size());

        // given attempt2, the hints computation process should conclude with rightPos = 1 and rightCol = 1
        assertEquals(1, attempt2.getHints().getRightPositions());
        assertEquals(1, attempt2.getHints().getRightColours());
    }

    @Test
    void simulateMatchDraw() {
        List<String> randomCode = getRandomCode();

        MatchStatus matchStatus = match.getMatchStatus();
        for (int i = 0; i < Match.ATTEMPTS; i++) {
            Attempt attempt = getAttempt(randomCode, matchStatus.getNextPlayer());
            match.tryToGuess(attempt);
        }
        assertEquals(10, match.getMadeAttempts().size());

        // now the available attempts are finished and since nobody has guessed
        // the correct code, the match should be ended with a DRAW
        assertEquals(MatchState.DRAW, matchStatus.getState());
    }

    @Test
    void simulateMatchVictory() {
        Attempt attempt;
        List<String> randomCode = getRandomCode();

        MatchStatus matchStatus = match.getMatchStatus();
        for (int i = 0; i < Match.ATTEMPTS; i++) {
            if (i == 9) attempt = getAttempt(secretCode, matchStatus.getNextPlayer());
            else attempt = getAttempt(randomCode, matchStatus.getNextPlayer());
            match.tryToGuess(attempt);
        }
        assertEquals(10, match.getMadeAttempts().size());

        // we simulated 10 guesses, the last of which is the correct one.
        // The match should be ended with a winner
        assertEquals(MatchState.VICTORY, matchStatus.getState());
    }

    @Test
    void testTurnSwitching() {
        MatchStatus matchStatus = match.getMatchStatus();
        String firstPlayer = matchStatus.getNextPlayer();

        Attempt attempt = new Attempt(List.of("gold", "coral", "rebeccapurple", "forestgreen"), firstPlayer);
        match.tryToGuess(attempt);

        // After the first player makes an attempt, the next player should be the second player
        assertNotEquals(firstPlayer, matchStatus.getNextPlayer());
    }

    @Test
    void testTurnRestriction() {
        MatchStatus matchStatus = match.getMatchStatus();
        String firstPlayer = matchStatus.getNextPlayer();
        String secondPlayer = firstPlayer.equals(player1.getUsername()) ? player2.getUsername() : player1.getUsername();

        // Create an attempt by the second player when it's the first player's turn
        Attempt attempt = new Attempt(List.of("gold", "coral", "rebeccapurple", "forestgreen"), secondPlayer);

        // Expect an exception to be thrown
        assertThrows(RuntimeException.class, () -> match.tryToGuess(attempt));

        // Verify that the turn has not changed
        assertEquals(firstPlayer, matchStatus.getNextPlayer());
    }

    @Test
    void testSecretCodeValidity() {
        SecretCode secretCode = new SecretCode();
        List<String> code = secretCode.getCode();

        // Check that the secret code has the correct length
        assertEquals(4, code.size());

        // Check that all colors in the secret code are valid
        List<String> validColors = List.of("gold", "crimson", "mediumblue", "rebeccapurple", "coral", "forestgreen");
        for (String color : code) {
            assertTrue(validColors.contains(color));
        }
    }

    private List<String> getRandomCode() {
        // generate a random code which is not the correct one
        List<String> randomCode = new SecretCode().getCode();
        while (randomCode.equals(secretCode))
            randomCode = new SecretCode().getCode();
        return randomCode;
    }

    private Attempt getAttempt(List<String> colorSequence, String playerUsername) {
        return new Attempt(colorSequence, playerUsername);
    }

    private Match getMatch() {
        return new Match(
            UUID.randomUUID(),
            new MatchStatus(List.of(player1, player2)),
            new ArrayList<>(),
            new SecretCode(secretCode));
    }
}
