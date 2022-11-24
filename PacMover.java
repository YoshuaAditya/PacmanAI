package pacman;

        import java.awt.*;
        import java.awt.event.KeyEvent;
        import java.util.Random;

public class PacMover {
    Robot robot;
    boolean keyPressed;
    int curKey;
    int curDir;

    static int autoDelay = 2;
    static Random r = new Random();

    //untuk memencet tombol kemananya
    static int[] keys = {-1, KeyEvent.VK_UP, KeyEvent.VK_RIGHT,
            KeyEvent.VK_DOWN, KeyEvent.VK_LEFT};

    public void move(int direction) {

        if (keyPressed) {
            robot.keyRelease( curKey );
            keyPressed = false;
        }

        if (direction > 0 && direction < keys.length) {
            curKey = keys[direction];
            robot.keyPress(curKey);
            robot.waitForIdle();
            keyPressed = true;
        }
        curDir = direction;
    }

    public PacMover() {
        keyPressed = false;
        try {
            robot = new Robot();
            robot.setAutoWaitForIdle(false);
            robot.setAutoDelay(autoDelay);
            System.out.println(robot.getAutoDelay());
            System.out.println(robot.isAutoWaitForIdle());
            curKey = -1;
        } catch(Exception e) {}
    }
}
