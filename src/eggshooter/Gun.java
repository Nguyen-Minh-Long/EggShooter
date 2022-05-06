package eggshooter;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Group Javascript Leader: Nguyen Vuong Khang Hy Tester: Nguyen Minh
 * Long Designer: Nguyen Tan Vu Class IA1401 Game Egg Shooter
 */
public class Gun extends Sprite {

    // declare new object Image
    private Image TestImage;
    // declare new object BufferedImage
    private BufferedImage bf;
    // declare angle of the gun
    private double currentAngle;
    // declare status shooted of the gun
    private boolean isShooted;

    /**
     * Constructor
     */
    public Gun() {
        this.isShooted = false;
        imageLoader();
    }

    /**
     * Rotate the gun to the left
     */
    public void leftRotate() {
        //rotate 5 degrees at a time
        if (currentAngle >= -57.6) {
            currentAngle -= 3.6;
        }
    }

    /**
     * Rotate the gun to the right
     */
    public void rightRotate() {
        //rotate 5 degrees at a time
        if (currentAngle <= 57.6) {
            currentAngle += 3.6;
        }
    }

    /**
     * Load gun image
     */
    public void imageLoader() {
        try {
            String testPath = "/img/gun.png";
            TestImage = ImageIO.read(getClass().getResourceAsStream(testPath));

        } catch (IOException ex) {
        }
    }

    /**
     * Draw gun
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {

        bf = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);

        try {
            animation(bf.getGraphics());
            g.drawImage(bf, 0, 0, null);
        } catch (Exception ex) {

        }
    }

    /**
     * Animation of gun
     *
     * @param g
     */
    public void animation(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform origXform = g2d.getTransform();
        AffineTransform newXform = (AffineTransform) (origXform.clone());
        //center of rotation is center of the panel
        int xRot = this.getWidth() / 2;
        int yRot = this.getHeight() / 2;
        newXform.rotate(Math.toRadians(currentAngle), xRot, yRot);
        g2d.setTransform(newXform);
        //draw image centered in panel
        int x = (getWidth() - TestImage.getWidth(this)) / 2;
        int y = (getHeight() - TestImage.getHeight(this)) / 2;
        g2d.drawImage(TestImage, x, y, this);
        g2d.setTransform(origXform);
    }

    /**
     * Pressed keyboard event
     *
     * @param ke
     */
    public void keyPressed(KeyEvent ke) {
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                this.rightRotate();
                break;
            case KeyEvent.VK_LEFT:
                this.leftRotate();
                break;

        }
        repaint();
    }

    /**
     * getter
     *
     * @return angle of the gun
     */
    public double getCurrentAngle() {
        return currentAngle;
    }

    /**
     * setter
     *
     * @param currentAngle angle of the gun
     */
    public void setCurrentAngle(double currentAngle) {
        this.currentAngle = currentAngle;
    }

    /**
     * getter
     *
     * @return status shooted of the gun
     */
    public boolean isIsShooted() {
        return isShooted;
    }

    /**
     * setter
     *
     * @param isShooted status shooted of the gun
     */
    public void setIsShooted(boolean isShooted) {
        this.isShooted = isShooted;
    }
}
