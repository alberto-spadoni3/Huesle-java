package it.unibo.sd.project.mastermind.model;

import java.util.ArrayList;
import java.util.List;

public class SecretCode {
    private final int COLOR_SEQUENCE_LENGTH = 4;
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
        // TODO: implement a color sequence generator
        return null;
    }
}
