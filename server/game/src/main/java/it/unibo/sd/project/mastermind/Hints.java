package it.unibo.sd.project.mastermind;

public class Hints {
    private final byte rightPositions;
    private final byte rightColours;

    public Hints(byte rightPositions, byte rightColours) {
        this.rightPositions = rightPositions;
        this.rightColours = rightColours;
    }

    public byte getRightPositions() {
        return rightPositions;
    }

    public byte getRightColours() {
        return rightColours;
    }
}
