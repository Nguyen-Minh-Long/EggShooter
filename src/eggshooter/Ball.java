package eggshooter;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Group Javascript
 * Leader: Nguyen Vuong Khang Hy
 * Tester: Nguyen Minh Long
 * Designer: Nguyen Tan Vu
 * Class IA1401
 * Game Egg Shooter
 */
public class Ball extends Sprite {
    // declare ball color
    private int color;
    /**
     * Constructor
     */
    public Ball() {  
    }
    /**
     * Constructor
     * @param image BufferedImage
     */
    public Ball(BufferedImage image) {
        super.setImage(image);
    }
    /**
     * Draw ball
     * @param g 
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(getImage(), 0, 0, this);

    }
    /**
     * getter
     * @return ball color
     */
    public int getColor() {
        return color;
    }
    /**
     * setter
     * @param color ball color
     */
    public void setColor(int color) {
        this.color = color;
    }
    
}


