package it.unibo.sd.project.mastermind.model.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SecretCode {
    public static final int COLOR_SEQUENCE_LENGTH = 4;

    private final List<String> possibleColours = List.of("crimson",
                                                         "coral",
                                                         "gold",
                                                         "forestgreen",
                                                         "mediumblue",
                                                         "rebeccapurple");
    private final ArrayList<String> colorCode;

    public SecretCode() {
        this.colorCode = this.generateColorSequence();
    }

    public List<String> getCode() {
        return this.colorCode;
    }

    public SecretCode(ArrayList<String> sequence) {
        this.colorCode = sequence;
    }

    private ArrayList<String> generateColorSequence() {
        ArrayList<String> randomColours = new ArrayList<>(COLOR_SEQUENCE_LENGTH);
        for (int i = 0; i < COLOR_SEQUENCE_LENGTH; i++) {
            int randomIndex = (int) Math.floor(new Random().nextDouble() * possibleColours.size());
            randomColours.add(possibleColours.get(randomIndex));
        }
        return randomColours;
    }
}
