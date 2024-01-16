package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.SecretCode;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchState;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameLogicTests {
    private final Player player1 = new Player("fabio", "fabiofazio@huesle.it", "password");
    private final Player player2 = new Player("Anna", "anna@huesle.it", "password");
    private final ArrayList<String> secretCode = new ArrayList<>(List.of("gold", "crimson", "mediumblue", "rebeccapurple"));
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
        Player firstPlayer = getPlayerFromUsername(match.getMatchStatus().getNextPlayer());
        Attempt attempt1 = new Attempt(List.of("crimson", "gold", "coral", "coral"), firstPlayer);
        match.tryToGuess(attempt1);
        assertEquals(1, match.getMadeAttempts().size());

        // given attempt1, the hints computation process should conclude with rightPos = 0 and rightCol = 2
        assertEquals(0, attempt1.getHints().getRightPositions());
        assertEquals(2, attempt1.getHints().getRightColours());

        Player secondPlayer = getPlayerFromUsername(match.getMatchStatus().getNextPlayer());
        Attempt attempt2 = new Attempt(List.of("gold", "coral", "rebeccapurple", "forestgreen"), secondPlayer);
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
            Player nextPlayer = getPlayerFromUsername(matchStatus.getNextPlayer());
            Attempt attempt = getAttempt(randomCode, nextPlayer);
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
            Player nextPlayer = getPlayerFromUsername(matchStatus.getNextPlayer());
            if (i == 9) attempt = getAttempt(secretCode, nextPlayer);
            else attempt = getAttempt(randomCode, nextPlayer);
            match.tryToGuess(attempt);
        }
        assertEquals(10, match.getMadeAttempts().size());

        // we simulated 10 guesses, the last of which is the correct one.
        // The match should be ended with a winner
        assertEquals(MatchState.VICTORY, matchStatus.getState());
    }

    private List<String> getRandomCode() {
        // generate a random code which is not the correct one
        List<String> randomCode = new SecretCode().getCode();
        while (randomCode.equals(secretCode))
            randomCode = new SecretCode().getCode();
        return randomCode;
    }

    private Attempt getAttempt(List<String> colorSequence, Player player) {
        return new Attempt(colorSequence, player);
    }

    private Player getPlayerFromUsername(String username) {
        return player1.getUsername().equals(username) ? player1 : player2;
    }

    private Match getMatch() {
        return new Match(
                UUID.randomUUID(),
                new MatchStatus(List.of(player1, player2)),
                new ArrayList<>(),
                new SecretCode(secretCode));
    }
}
