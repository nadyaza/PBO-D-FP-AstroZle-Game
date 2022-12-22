package AstrozleGraphics;

import java.awt.Image;


import java.util.ArrayList;

public class Animation {
    private ArrayList<AnimFrame> frames;
    private int currFrameIndex;
    private long animTime;
    private long totalDuration;

    public int getSize() {
        return frames.size();
    }
    
    public Animation() {
        this(new ArrayList<AnimFrame>(), 0);
    }

    private Animation(ArrayList<AnimFrame> frames, long totalDuration) {
        this.frames = frames;
        this.totalDuration = totalDuration;
        start();
    }

    public Object clone() {
        return new Animation(frames, totalDuration);
    }

    public synchronized void addFrame(Image image, long duration) {
        totalDuration += duration;
        frames.add(new AnimFrame(image, totalDuration));
    }

    public synchronized void start() {
        animTime = 0;
        currFrameIndex = 0;
    }

    public synchronized void update(long elapsedTime) {
        if (frames.size() > 1) {
            animTime += elapsedTime;

            if (animTime >= totalDuration) 
            {
                animTime = animTime % totalDuration;
                currFrameIndex = 0;
            }

            while (animTime > getFrame(currFrameIndex).endTime) 
                currFrameIndex++;
        }
    }

    public synchronized Image getImage() {
        if (frames.isEmpty())
            return null;
        else 
            return getFrame(currFrameIndex).image;
    }

    private AnimFrame getFrame(int i) {
        return (AnimFrame)frames.get(i);
    }

    private class AnimFrame {
        Image image;
        long endTime;

        public AnimFrame(Image image, long endTime) {
            this.image = image;
            this.endTime = endTime;
        }
    }
}
