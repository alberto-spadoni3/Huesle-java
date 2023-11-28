package it.unibo.sd.project.mastermind;

import java.util.List;
import java.util.Objects;

public class Attempt {
    private final List<String> colorSequence;
    private final Player attemptMadeBy;
    private Hints hints;

    public Attempt(List<String> colorSequence, Player attemptMadeBy, Hints hints) {
        this.colorSequence = colorSequence;
        this.attemptMadeBy = attemptMadeBy;
    }

    public void computeHints(SecretCode secretCode) {
        //TODO
        byte zero = Byte.parseByte("0");
        this.hints = new Hints(zero, zero);
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
