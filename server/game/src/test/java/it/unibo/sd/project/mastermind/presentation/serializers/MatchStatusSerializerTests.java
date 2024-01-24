package it.unibo.sd.project.mastermind.presentation.serializers;

import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


public class MatchStatusSerializerTests {
    Player p1 = new Player("prova","pro@pro.va","asdf");
    Player p2 = new Player("p2", "p2@pro.va","asdf");

    ArrayList<Player> players = new ArrayList<Player>();

    @Test
    void matchStatusSerializerTest(){
        players.add(p1);
        players.add(p2);
        MatchStatus ms = new MatchStatus(players);

        System.out.println(Presentation.serializerOf(MatchStatus.class).serialize(ms));
    }
}
