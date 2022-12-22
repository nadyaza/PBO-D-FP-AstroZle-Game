package AstrozleTilegame.sprites;

import AstrozleGraphics.Animation;

/**
    A Grub is a Creature that moves slowly on the ground.
*/
public class Alien extends Creature {

    public Alien(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }


    public float getMaxSpeed() {
        return 0.05f;
    }

}
