package pacman;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SimpleDisplay extends JComponent {
    int w, h;
    Dimension d;
    ArrayList<Drawable> objects;

    public void paint(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, w, h);
        draw(g);
    }

    public void draw(Graphics g) {
        if (objects != null && g != null) {
            synchronized (SimpleDisplay.class) {
                for (Drawable d : objects) {
                    d.draw(g, w, h);
                }
            }
        }
    }

    public void updateObjects(ArrayList<Drawable> objects) {
        synchronized (SimpleDisplay.class) {
            this.objects = objects;
        }
        repaint();
    }

    public SimpleDisplay(int w, int h) {
        this.w = w;
        this.h = h;
        d = new Dimension(w, h);
        setBackground(Color.black);
        setFocusable(true);
    }

    public Dimension getPreferredSize() {
        return d;
    }
}
