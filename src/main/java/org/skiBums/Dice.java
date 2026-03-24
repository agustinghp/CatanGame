package org.skiBums;

import java.util.Random;

public class Dice {
    final int sides;
    final Random random = new Random();

    public Dice(int sides) {
        this.sides = sides;
    }

    public int roll() {
        return random.nextInt(sides) + 1;
    }

    public int doubleRoll() {
        return (this.roll() + this.roll());
    }
}
