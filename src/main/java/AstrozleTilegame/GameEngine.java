package AstrozleTilegame;

import java.awt.*;

import java.awt.event.KeyEvent;
import java.util.Iterator;

import AstrozleAudio.AudioManager;
import AstrozleGraphics.Sprite;
import AstrozleInput.GameAction;
import AstrozleInput.InputManager;
import AstrozleTest.GameCore;
import AstrozleTilegame.sprites.Creature;
import AstrozleTilegame.sprites.Astronot;
import AstrozleTilegame.sprites.PowerUp;

/**
 * GameManager manages all parts of the game.
 */
public class GameEngine extends GameCore {
    public static final float GRAVITY = 0.002f;
    public static final float DEFAULT_ACCELERATION = 0.005f;
    
    private Point pointCache = new Point();
    private TileMap map;
    private MapLoader mapLoader;
    private InputManager inputManager;
    private TileMapDrawer drawer;
    
    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction exit;
    private GameAction nextLevel;
    private int collectedCoins = 0;
    private int numLives = 5;
   
    public void init() {
        super.init();
        
        // set up input manager
        initInput();
        
        // start resource manager
        mapLoader = new MapLoader(screen.getFullScreenWindow()
            .getGraphicsConfiguration());
        
        // load background images and define a parallax factor for each image
        drawer = new TileMapDrawer();
        drawer.setBackgrounds(new Image[] {
            mapLoader.loadImage("background_1.png"),
            mapLoader.loadImage("background_2.png"),
            mapLoader.loadImage("background_3.png"),
            mapLoader.loadImage("background_4.png"),
            mapLoader.loadImage("background_5.png"),
            mapLoader.loadImage("background_6.png"),
            mapLoader.loadImage("background_7.png"),
            mapLoader.loadImage("background_8.png"),
            mapLoader.loadImage("background_9.png"),
            mapLoader.loadImage("background_10.png"),
            mapLoader.loadImage("background_11.png"),
        });
        
        drawer.setBackgroundParallaxFactors(new float[] {
            0.2f,
            0.4f,
            0.8f,
            1.4f,
            2.8f,
            2.8f,
            3.6f,
            4.6f,
            4.6f,
            5.525f,
            6.5f,
        });
        
        // load first map
        map = mapLoader.loadNextMap();
        
        AudioManager.getInstance().play(AudioManager.SoundType.BG_MUSIC);
    }
    
    /**
     * Closes any resources used by the GameManager.
     */
    public void stop() {
        super.stop();
    }
    
    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        jump = new GameAction("jump", GameAction.DETECT_INITAL_PRESS_ONLY);
        nextLevel = new GameAction("nextLevel", GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit", GameAction.DETECT_INITAL_PRESS_ONLY);
        
        inputManager = new InputManager(screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);
        
        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveLeft, KeyEvent.VK_A);
        
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_D);
        
        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
        inputManager.mapToKey(jump, KeyEvent.VK_UP);
        inputManager.mapToKey(jump, KeyEvent.VK_W);
        
        inputManager.mapToKey(nextLevel, KeyEvent.VK_N);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
    }
    
    private void checkInput(long elapsedTime) {
        if (exit.isPressed()) {
            AudioManager.getInstance().stopAll();
            stop();
        }
        
        Astronot player = (Astronot)map.getPlayer();
        if (player.isAlive()) {
            player.setAcceleration(0f);
            if (moveRight.isPressed())
                player.setAcceleration(DEFAULT_ACCELERATION);
            else if (moveLeft.isPressed())
                player.setAcceleration(-DEFAULT_ACCELERATION);
            
            if (jump.isPressed())
                player.jump(false);
            
            if (nextLevel.isPressed()) {
                mapLoader.loadNextMap();
                player.setState(Creature.STATE_DYING);
            }
        }
    }
    
    public void draw(Graphics2D g) {
        drawer.draw(g, map, screen.getWidth(), screen.getHeight());
        g.setColor(Color.WHITE);
        g.drawString("Press ESC for EXIT.", 10.0f, 20.0f);
        g.setColor(Color.GREEN);
        g.drawString("Coins: " + collectedCoins, 300.0f, 20.0f);
        g.setColor(Color.YELLOW);
        g.drawString("Lives: " + (numLives), 500.0f, 20.0f);
        g.setColor(Color.WHITE);
        g.drawString("Home: " + mapLoader.currentMap, 700.0f, 20.0f);
    }
    
    /**
     * Gets the current map.
     */
    public TileMap getMap() {
        return map;
    }
    
    /**
     * Gets the tile that a Sprites collides with.Only the
        Sprite's X or Y should be changed, not both.Returns null
        if no collision is detected.
     */
    public Point getTileCollision(Sprite sprite, float newX, float newY) {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);
        
        // get the tile locations
        int fromTileX = TileMapDrawer.pixelsToTiles(fromX);
        int fromTileY = TileMapDrawer.pixelsToTiles(fromY);
        int toTileX = TileMapDrawer.pixelsToTiles(toX + sprite.getWidth() - 1);
        int toTileY = TileMapDrawer.pixelsToTiles(toY + sprite.getHeight() - 1);
        
        // check each tile for a collision
        for (int x = fromTileX; x <= toTileX; x++) {
            for (int y = fromTileY; y <= toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null) {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }
        
        // no collision found
        return null;
    }
    
    /**
     * Checks if two Sprites collide with one another.Returns
        false if the two Sprites are the same.Returns false if
        one of the Sprites is a Creature that is not alive.
     */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2)
            return false;
        
        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive())
            return false;
        if (s2 instanceof Creature && !((Creature)s2).isAlive())
            return false;
        
        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());
        
        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }
    
    /**
     * Gets the Sprite that collides with the specified Sprite,
     * or null if no Sprite collides with the specified Sprite.
     */
    public Sprite getSpriteCollision(Sprite sprite) {
        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }
        
        // no collision found
        return null;
    }
    
    /**
     * Updates Animation, position, and velocity of all Sprites
     * in the current map.
     */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();
        
        // player fallen
        if (player.getY() > screen.getHeight() + player.getHeight() * 2) {
            player.setState(Creature.STATE_DEAD);
            drawer.resetStartPositions();
            numLives--;
            AudioManager.getInstance().play(AudioManager.SoundType.PLAYER_DEATH);
        }
        
        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            map = mapLoader.reloadMap();
            return;
        }
        
        // get keyboard/mouse input
        checkInput(elapsedTime);
        
        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);
        
        // update other sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD)
                    i.remove();
                else
                    updateCreature(creature, elapsedTime);
            }
            // normal update
            sprite.update(elapsedTime);
        }
    }
    
    /**
     * Updates the creature, applying gravity for creatures that
     * aren't flying, and checks collisions.
     */
    private void updateCreature(Creature creature,
        long elapsedTime) {
        
        // apply gravity
        if (!creature.isFlying())
            creature.setVelocityY(creature.getVelocityY() + GRAVITY * elapsedTime);
        
        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile = getTileCollision(creature, newX, creature.getY());
        if (tile == null) 
            creature.setX(newX);
        else {
            // line up with the tile boundary
            if (dx > 0)
                creature.setX(TileMapDrawer.tilesToPixels(tile.x) - creature.getWidth());
            else if (dx < 0)
                creature.setX(TileMapDrawer.tilesToPixels(tile.x + 1));
            creature.collideHorizontal();
        }
        if (creature instanceof Astronot)
            checkPlayerCollision((Astronot)creature, false);
        
        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null)
            creature.setY(newY);
        else {
            // line up with the tile boundary
            if (dy > 0)
                creature.setY(TileMapDrawer.tilesToPixels(tile.y) - creature.getHeight());
            else if (dy < 0)
                creature.setY(TileMapDrawer.tilesToPixels(tile.y + 1));
            
            creature.collideVertical();
        }
        if (creature instanceof Astronot) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Astronot)creature, canKill);
        }
    }
    
    /**
     * Checks for Player collision with other Sprites. 
       If canKill is true, collisions with Creatures will kill them.
     */
    public void checkPlayerCollision(Astronot player,
            boolean canKill) {
        if (!player.isAlive())
            return;
        
        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp)
            acquirePowerUp((PowerUp)collisionSprite);
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if ((canKill || player.getInvincible()) && badguy.isKillable()) {
                AudioManager.getInstance().play(AudioManager.SoundType.CREATURE_DEATH);
                
                // kill the badguy and make player bounce
                badguy.setState(Creature.STATE_DYING);
                
                if (canKill) {
                    player.setY(badguy.getY() - player.getHeight());
                    player.jump(true);
                }
            }
            else {
                // player dies!
                player.setState(Creature.STATE_DYING);
                drawer.resetStartPositions();
                numLives--;
                if (numLives == 0) {
                    AudioManager.getInstance().stopAll();
                    AudioManager.getInstance().play(AudioManager.SoundType.GAME_OVER);
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    stop();
                }
                else
                    AudioManager.getInstance().play(AudioManager.SoundType.PLAYER_DEATH);
            }
        }
    }
    
    /**
     * Gives the player the specified power up and removes it
     * from the map.
     * @param powerUp
     */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);
        
        if (powerUp instanceof PowerUp.Coin) {
            AudioManager.getInstance().play(AudioManager.SoundType.COIN_PICKUP);
            
            collectedCoins++;
            if (collectedCoins == 100) {
                numLives++;
                collectedCoins=0;
            }
        }
        else if (powerUp instanceof PowerUp.Star) {
            AudioManager.getInstance().play(AudioManager.SoundType.STAR_PICKUP);
            
            Astronot player = (Astronot)map.getPlayer();
            player.setInvincible(true);
        }
        else if (powerUp instanceof PowerUp.Goal) {
            Astronot player = (Astronot)map.getPlayer();
            player.setInvincible(false);
            
            AudioManager.getInstance().play(AudioManager.SoundType.LEVEL_ADVANCE);
            
            // advance to next map
            //map = mapLoader.loadNextMap();
        }
    }
}