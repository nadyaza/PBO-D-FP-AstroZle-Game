package AstrozleTilegame;

import AstrozleGraphics.Animation;
import AstrozleGraphics.Sprite;
import AstrozleTilegame.sprites.Pesawat;
import AstrozleTilegame.sprites.Alien;
import AstrozleTilegame.sprites.Astronot;
import AstrozleTilegame.sprites.PowerUp;
import AstrozleTilegame.sprites.Stone;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;


/**
    The ResourceManager class loads and manages tile Images and
    "host" Sprites used in the game. Game Sprites are cloned from
    "host" Sprites.
*/
public class MapLoader {
    private ArrayList<Image> tiles;
    public int currentMap;
    private GraphicsConfiguration gc;

    // host sprites used for cloning
    private Sprite playerSprite;
    private Sprite starSprite;
    private Sprite coinSprite;
    private Sprite goalSprite;
    private Sprite grubSprite;
    private Sprite flySprite;
    private Sprite trapSprite;

    /**
        Creates a new ResourceManager with the specified
        GraphicsConfiguration.
    */
    public MapLoader(GraphicsConfiguration gc) {
        this.gc = gc;
        loadTileImages();
        loadCreatureSprites();
        loadPowerUpSprites();
    }

    /**
        Gets an image from the images/ directory.
    */
    public Image loadImage(String name) {
        String filename = "images/" + name;
        return new ImageIcon(filename).getImage();
    }

    public Image getMirrorImage(Image image) {
        return getScaledImage(image, -1, 1);
    }

    public Image getFlippedImage(Image image) {
        return getScaledImage(image, 1, -1);
    }

    private Image getScaledImage(Image image, float x, float y) {
        // set up the transform
        AffineTransform transform = new AffineTransform();
        transform.scale(x, y);
        transform.translate(
            (x-1) * image.getWidth(null) / 2,
            (y-1) * image.getHeight(null) / 2);

        // create a transparent (not translucent) image
        Image newImage = gc.createCompatibleImage(
            image.getWidth(null),
            image.getHeight(null),
            Transparency.BITMASK);

        // draw the transformed image
        Graphics2D g = (Graphics2D)newImage.getGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        return newImage;
    }

    public TileMap loadNextMap() {
        TileMap map = null;
        while (map == null) {
            currentMap++;
            try {
                map = loadMap(
                    "maps/map" + currentMap + ".txt");
            }
            catch (IOException ex) {
                if (currentMap == 2) {
                    // no maps to load!
                    return null;
                }
                currentMap = 0;
                map = null;
            }
        }

        return map;
    }

    public TileMap reloadMap() {
        try {
            return loadMap("maps/map" + currentMap + ".txt");
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private TileMap loadMap(String filename) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        int width = 0;
        int height = 0;

        // read every line in the text file into the list
        BufferedReader reader = new BufferedReader(
            new FileReader(filename));
        while (true) {
            String line = reader.readLine();
            // no more lines to read
            if (line == null) {
                reader.close();
                break;
            }

            // add every line except for comments
            if (!line.startsWith("#")) {
                lines.add(line);
                width = Math.max(width, line.length());
            }
        }
        
        // parse the lines to create a TileEngine
        height = lines.size();
        TileMap newMap = new TileMap(width, height);
        for (int y=0; y<height; y++) {
            String line = (String)lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                char ch = line.charAt(x);

                // check if the char represents tile A, B, C etc.
                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size())
                    newMap.setTile(x, y, (Image)tiles.get(tile));

                // check if the char represents a sprite
                else if (ch == 'o')
                    addSprite(newMap, coinSprite, x, y);
                else if (ch == '!')
                    addSprite(newMap, starSprite, x, y);
                else if (ch == '*')
                    addSprite(newMap, goalSprite, x, y);
                else if (ch == '1')
                    addSprite(newMap, grubSprite, x, y);
                else if (ch == '2')
                    addSprite(newMap, flySprite, x, y);
                else if (ch == '-')
                    addSprite(newMap, trapSprite, x, y);
            }
        }

        // add the player to the map
        Sprite player = (Sprite)playerSprite.clone();
        player.setX(TileMapDrawer.tilesToPixels(3));
        player.setY(lines.size());
        newMap.setPlayer(player);

        return newMap;
    }

    private void addSprite(TileMap map,
        Sprite hostSprite, int tileX, int tileY) {
        if (hostSprite != null) {
            // clone the sprite from the "host"
            Sprite sprite = (Sprite)hostSprite.clone();

            // center the sprite
            sprite.setX(
                TileMapDrawer.tilesToPixels(tileX) +
                (TileMapDrawer.tilesToPixels(1) -
                sprite.getWidth()) / 2);

            // bottom-justify the sprite
            sprite.setY(
                TileMapDrawer.tilesToPixels(tileY + 1) -
                sprite.getHeight());

            // add it to the map
            map.addSprite(sprite);
        }
    }

    /*
        Code for loading sprites and images
    */
    public void loadTileImages() {
        // keep looking for tile A,B,C, etc. this makes it
        // easy to drop new tiles in the images/ directory
        tiles = new ArrayList<>();
        char ch = 'A';
        
        while (true) {
            String name = ch + ".png";
            File file = new File("images/" + name);
            if (!file.exists()) 
                break;
            
            tiles.add(loadImage(name));
            ch++;
        }
    }

    public void loadCreatureSprites() {
        int animCount = 7;
        Image[][][] images = new Image[animCount][4][];

        // load left-facing images
        images[0][0] = new Image[] {
            loadImage("player_idle1.png"),
            loadImage("player_idle2.png"),
            loadImage("player_idle3.png"),
            loadImage("player_idle4.png"),
            loadImage("player_idle5.png"),
            loadImage("player_idle6.png"),
            loadImage("player_idle7.png"),
            loadImage("player_idle8.png"),
            loadImage("player_idle9.png"),
            loadImage("player_idle10.png"),
            loadImage("player_idle11.png"),
            loadImage("player_idle12.png")
        };
        
        images[1][0] = new Image[] {
            loadImage("player_idle_rage1.png"),
            loadImage("player_idle_rage2.png"),
            loadImage("player_idle_rage3.png"),
            loadImage("player_idle_rage4.png"),
            loadImage("player_idle_rage5.png"),
            loadImage("player_idle_rage6.png"),
            loadImage("player_idle_rage7.png"),
            loadImage("player_idle_rage8.png"),
            loadImage("player_idle_rage9.png"),
            loadImage("player_idle_rage10.png"),
            loadImage("player_idle_rage11.png"),
            loadImage("player_idle_rage12.png")
        };
        
        images[2][0] = new Image[] {
            loadImage("player_walk1.png"),
            loadImage("player_walk2.png"),
            loadImage("player_walk3.png"),
            loadImage("player_walk4.png"),
            loadImage("player_walk5.png"),
            loadImage("player_walk6.png"),
            loadImage("player_walk7.png"),
            loadImage("player_walk8.png"),
            loadImage("player_walk9.png"),
            loadImage("player_walk10.png")
        };
        
        images[3][0] = new Image[] {
            loadImage("player_walk_rage1.png"),
            loadImage("player_walk_rage2.png"),
            loadImage("player_walk_rage3.png"),
            loadImage("player_walk_rage4.png"),
            loadImage("player_walk_rage5.png"),
            loadImage("player_walk_rage6.png"),
            loadImage("player_walk_rage7.png"),
            loadImage("player_walk_rage8.png"),
            loadImage("player_walk_rage9.png"),
            loadImage("player_walk_rage10.png")
        };
        
        images[4][0] = new Image[] {
            loadImage("pesawat1.png"),
            loadImage("pesawat2.png"),
            loadImage("pesawat3.png"),
            loadImage("pesawat4.png"),
            loadImage("pesawat5.png"),
            loadImage("pesawat6.png"),
            loadImage("pesawat7.png"),
            loadImage("pesawat8.png")
        };
        
        images[5][0] = new Image[] {
            loadImage("alien1.png"),
            loadImage("alien2.png"),
            loadImage("alien3.png"),
            loadImage("alien4.png"),
            loadImage("alien5.png"),
            loadImage("alien6.png"),
            loadImage("alien7.png"),
            loadImage("alien8.png")
        };
        
        images[6][0] = new Image[] {
            loadImage("stone1.png"),
            loadImage("stone2.png"),
            loadImage("stone3.png"),
            loadImage("stone4.png"),
            loadImage("stone5.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
            loadImage("stone1.png"),
        };
        
        for (int j = 1; j < 4; j++) {
            for (int i = 0; i < animCount; i++)
                images[i][j] = new Image[images[i][0].length];
        }
        
        for (int i = 0; i < animCount; i++) {
            for (int j = 0; j < images[i][0].length; j++) {
                // right-facing images
                images[i][1][j] = getMirrorImage(images[i][0][j]);
                // left-facing "dead" images
                images[i][2][j] = getFlippedImage(images[i][0][j]);
                // right-facing "dead" images
                images[i][3][j] = getFlippedImage(images[i][1][j]);
            }
        }

        // create creature animations
        Animation[] playerIdleAnim = new Animation[4];
        Animation[] playerIdleRageAnim = new Animation[4];
        Animation[] playerWalkingAnim = new Animation[4];
        Animation[] playerWalkingRageAnim = new Animation[4];
        Animation[] flyAnim = new Animation[4];
        Animation[] grubAnim = new Animation[4];
        Animation[] trapAnim = new Animation[4];
        
        for (int i = 0; i < 4; i++) {
            playerIdleAnim[i] = createAnim(images[0][i], 90);
            playerIdleRageAnim[i] = createAnim(images[1][i], 90);
            playerWalkingAnim[i] = createAnim(images[2][i], 50);
            playerWalkingRageAnim[i] = createAnim(images[3][i], 50);
            flyAnim[i] = createAnim(images[4][i], 60);
            grubAnim[i] = createAnim(images[5][i], 70);
            trapAnim[i] = createAnim(images[6][i], 90);
        }

        // create creature sprites
        playerSprite = new Astronot(playerIdleAnim, playerIdleRageAnim,
            playerWalkingAnim, playerWalkingRageAnim);
        flySprite = new Pesawat(flyAnim[0], flyAnim[1], flyAnim[2], flyAnim[3]);
        grubSprite = new Alien(grubAnim[0], grubAnim[1], grubAnim[2], grubAnim[3]);
        trapSprite = new Stone(trapAnim[0], trapAnim[1], trapAnim[2], trapAnim[3]);
    }

    private Animation createAnim(Image[] frames, long timePerFrame) {
        Animation anim = new Animation();
        for (Image frame : frames)
            anim.addFrame(frame, timePerFrame);
        
        return anim;
    }

    private void loadPowerUpSprites() {
        // create "goal" sprite
        Animation anim = new Animation();
        anim.addFrame(loadImage("home.png"), 150);
        goalSprite = new PowerUp.Goal(anim);

        // create "coin" sprite
        anim = new Animation();
        anim.addFrame(loadImage("bumi1.png"), 250);  
        anim.addFrame(loadImage("bumi2.png"), 250);
        anim.addFrame(loadImage("bumi3.png"), 250);
        anim.addFrame(loadImage("bumi4.png"), 250);
        anim.addFrame(loadImage("bumi5.png"), 250);
        coinSprite = new PowerUp.Coin(anim);

    }
}
