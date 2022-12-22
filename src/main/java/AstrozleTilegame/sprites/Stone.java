package AstrozleTilegame.sprites;

import AstrozleGraphics.Animation;

/**
    A trap is an object that kills the player on touch.
*/
public class Stone extends Creature {
    // A trap is not killable by default
    public Stone(Animation left, Animation right,
        Animation deadLeft, Animation deadRight) {
        super(left, right, deadLeft, deadRight, false);
    }
}

