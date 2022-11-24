package pacman;

import java.awt.*;

import static pacman.MsPacInterface.fullscreenSizeTimes;

public class ConnectedSet implements Drawable {

    int blinky = -65536;//;
    int pinky = -18689;//;
    int inky = -16711681;//;
    int sue = -18859;//;
    int x, y;
    int width, height;//disini width height dari objek bukan layar
    int fg; // the value of the FG pixels
    int xMin, xMax, yMin, yMax;//xmin adalah titik paling kiri objek, ymin atas, xmax kanan, ymax
    Color c;
    public int px, py;
    boolean valid = false;

    //ini untuk entrance saja, mungkin perlu dibuat class terpisah
    public boolean isCorner = false;

    int dotSize = 3;
    int powerPillSize = 15;
    int ghostSize = 13 * fullscreenSizeTimes + 1;

    public void draw(Graphics g, int w, int h) {
        validate();
        g.setColor(c);
        if (ghostLike()) {
            g.fillRect(xMin, yMin, width, height);
        } else if (edible()) {
            g.fillRect(xMin, yMin, width, height);
        } else if (powerPill() || pill()) {
            g.drawRect(xMin, yMin, width, height);
        } else if (entrance()) {
            if (isCorner) {
                g.setColor(Color.green);
            }
            g.drawRect(xMin, yMin, 3, 3);
        } else if (wall()) {
            g.drawRect(xMin, yMin, width, height);
        }
    }

    public void validate() {
        if (!valid) {
            width = xMax - xMin;
            height = yMax - yMin;
            valid = true;
        }
    }


    public boolean ghostLike() {
        validate();
        return ghostColor(fg) && width == ghostSize && height == ghostSize;
    }

    public boolean wall() {
        validate();
        return ghostColor(fg);
    }

    public boolean edible() {
        validate();
        return (MsPacInterface.edible == fg || MsPacInterface.pill == fg)
                && between(width, ghostSize - 16, ghostSize + 16)
                && between(height, ghostSize - 16, ghostSize + 16);
    }

    public boolean ghostColor(int c) {
        return c == MsPacInterface.blinky
                || c == MsPacInterface.pinky
                || c == MsPacInterface.inky
                || c == MsPacInterface.sue;
    }

    public boolean isPacMan() {
        validate();
        return fg == MsPacInterface.pacMan && width >= 18 && height >= 18;
    }

    public boolean ready() {
        validate();
        return fg == MsPacInterface.pacMan && width == 7 && height == 9;
    }

    public boolean pill() {
        validate();
        return width == dotSize && height == dotSize && fg != MsPacInterface.entrance && fg != MsPacInterface.edible;
    }

    public boolean powerPill() {
        validate();
        return width == powerPillSize && height == powerPillSize;
    }

    public boolean entrance() {
        validate();
        c = new Color(fg);
        return fg == MsPacInterface.entrance;
    }

    public static boolean between(int x, int low, int high) {
        return x >= low && x <= high;
    }

    public ConnectedSet(int x, int y, int fg) {
        this.x = x;
        this.y = y;
        xMin = x;
        xMax = x;
        yMin = y;
        yMax = y;
        this.fg = fg;
        c = new Color((fg & 0xFF0000) >> 16, (fg & 0xFF00) >> 8, (fg & 0xFF));
    }

    public void add(int px, int py, int pos, int val) {
        xMin = Math.min(px, xMin);
        xMax = Math.max(px, xMax);
        yMin = Math.min(py, yMin);
        yMax = Math.max(py, yMax);
        valid = false;
    }

    public String toString() {
        return x + " : " + y ;
    }

}
