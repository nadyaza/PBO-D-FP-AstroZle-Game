package AstrozleTilegame.sprites;

import AstrozleGraphics.Animation;

/**
    A Fly is a Creature that fly slowly in the air.
*/
public class Pesawat extends Creature {
    public Pesawat(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }

    public float getMaxSpeed() {
        return 0.1f;
    }

    public boolean isFlying() {
        return isAlive();
    }
}
