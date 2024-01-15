package it.unibo.sd.project.mastermind.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Attempt {
    private final List<String> colorSequence;
    private final Player attemptMadeBy;
    private Hints hints;

    public Attempt(List<String> colorSequence, Player attemptMadeBy) {
        this.colorSequence = colorSequence;
        this.attemptMadeBy = attemptMadeBy;
        this.hints = null;
    }

    public Attempt(List<String> colorSequence, Player attemptMadeBy, Hints hints) {
        this.colorSequence = colorSequence;
        this.attemptMadeBy = attemptMadeBy;
        this.hints = hints;
    }

    public void computeHints(SecretCode secretCode) {
        //TODO
        if (colorSequence != null && hints == null) {
            byte rightPositions = 0;
            for (int i = 0; i < colorSequence.size(); i++)
                if (colorSequence.get(i).equals(secretCode.getCode().get(i)))
                    rightPositions++;

            byte rightColours = (byte) -rightPositions;
            List<String> temporarySequence = new ArrayList<>(secretCode.getCode());
            for (String color : colorSequence)
                if (temporarySequence.contains(color)) {
                    temporarySequence.remove(color);
                    rightColours++;
                }
            this.hints = new Hints(rightPositions, rightColours);
        }
    }

    public List<String> getColorSequence() {
        return List.copyOf(this.colorSequence);
    }

    public Player getPlayer() {
        return attemptMadeBy;
    }

    public Hints getHints() {
        return hints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attempt attempt = (Attempt) o;
        return Objects.equals(colorSequence, attempt.getColorSequence());
    }

    @Override
    public int hashCode() {
        return Objects.hash(colorSequence);
    }
}
