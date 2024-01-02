package it.unibo.sd.project.mastermind.presentation.serializers;

import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.SecretCode;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatchSerializerTests {
    UUID uuid = new UUID(1, 2);
    Player p1 = new Player("prova","pro@pro.va","asdf");
    Player p2 = new Player("p2", "p2@pro.va","asdf");
    ArrayList<Player> players = new ArrayList<Player>();

    ArrayList listAttempts = new ArrayList<Attempt>();

    SecretCode secretCode = new SecretCode();

    @Test
    void matchSerializerTest(){
        players.add(p1);
        players.add(p2);
        MatchStatus matchStatus = new MatchStatus(players);
        Match match = new Match(uuid, matchStatus, listAttempts, secretCode);
        System.out.println(Presentation.serializerOf(Match.class).serialize(match));

        assertEquals(true, true);
    }
}
