package eggshooter;

import java.awt.image.BufferedImage;
import javax.swing.JLabel;

/**
 *
 * @author Group Javascript
 * Leader: Nguyen Vuong Khang Hy
 * Tester: Nguyen Minh Long
 * Designer: Nguyen Tan Vu
 * Class IA1401
 * Game Egg Shooter
 */
public class Sprite extends JLabel{
    // declare new object BufferedImage
    private BufferedImage image;
    
    /**
     * getter
     * @return 
     */
    public BufferedImage getImage() {
        return image;
    }
    /**
     * setter
     * @param image 
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }
    
}
