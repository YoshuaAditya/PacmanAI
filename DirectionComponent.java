package pacman;

import javax.swing.*;
import java.awt.*;

public class DirectionComponent extends JComponent {
    
    static double far=1000;

    public static void main(String[] args) throws Exception {
        DirectionComponent dc = easyUse();
        for (int i=0; i<100; i++) {
            dc.update(i % 5,far);
            Thread.sleep(500);
        }
    }

    public static DirectionComponent easyUse() {
        DirectionComponent dc = new DirectionComponent();
        JPanel p = new JPanel();
        p.add(dc);
        JEasyFrame f = new JEasyFrame(p, "Test", true);
        f.setLocation(300, 750);
        return dc;
    }

    int w = 50;
    int h = 50;
    int[] x = new int[3];
    int[] y = new int[3];


    public void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.black);
        g.fillRect(0, 0, w, h);
        if(far<30) g.setColor(Color.red);
        else g.setColor(Color.cyan);

        if (dir == 0) {
            g.fillRect(w/3, h/3, w/3, h/3);
        } else {
            g.rotate((dir-1) * 90 * Math.PI / 180, w/2, h / 2);
            x[0] = w/3; y[0] = h/3;
            x[1] = w/2; y[1] = 0;
            x[2] = 2 * w/3; y[2] = h/3;
            g.fillPolygon(x, y, 3);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }

    int dir;

    public void update(int dir, double d) {
        this.dir = dir;
        far=d;
        repaint();
    }

}
