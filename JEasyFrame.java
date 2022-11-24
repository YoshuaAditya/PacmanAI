package pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static pacman.MsPacInterface.fullscreenSizeTimes;

public class JEasyFrame extends JFrame {
    public Component comp;
    public boolean exitOnClose;
    public static boolean EXIT_DEFAULT = false;
    public static String DEFAULT_TITLE = "Closeable Frame";

    public boolean canClose() {
        return true;
    }

    public JEasyFrame() {
        this(DEFAULT_TITLE, EXIT_DEFAULT);
    }

    public JEasyFrame(String title) {
        this(title, EXIT_DEFAULT);
    }

    public JEasyFrame(String title, boolean exit) {
        super(title);
        exitOnClose = exit;
        addWindowListener(new Closer());
    }

    class Closer extends WindowAdapter {
        public Closer() {
        }

        public void windowClosing(WindowEvent e) {
            tryClose();
        }
    }

    public JEasyFrame(Component comp, String title, boolean exit) {
        this(title, exit);
        this.comp = comp;
        getContentPane().add(BorderLayout.CENTER, comp);  
        setLocation(300 / fullscreenSizeTimes, 390 / fullscreenSizeTimes);
        pack();
        setVisible(true);
        repaint();
    }

    public void tryClose() {
        if (canClose()) {
            if (exitOnClose)
                System.exit(0);
            else
                dispose();
        }
    }
}
