package pacman;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;

//nyalakan gamenya dulu, baru jalanin ini
//untuk game ms pacman dari revenge of arcade harus jalanin administrator
//dan matikan disable display scaling on hdpi settings
public class MsPacInterface {

    static boolean display = true;
    static int fullscreenSizeTimes = 2;

    public static void main(String[] args) throws Exception {
        /*
        sekalian membuat objek robot untuk screen capture, simpleExtractor, simpleDisplay
        membuat array pixels untuk menampung seluruh pixel yang akan diambil oleh robot
        simpleDisplay ditaruh ke JEasyFrame untuk ditampilkan
         */
        MsPacInterface msPacInterface = new MsPacInterface();

        //membuat robot untuk menggerakkan pacman
        PacMover pacMover = new PacMover();

        //membuat display untuk menunjukkan ke arah mana pacman
        DirectionComponent directionComponent = DirectionComponent.easyUse();

        //loop untuk AI pacman
        while (true) {
            //menyimpan hasil pixels dari sceen capture
            int[] pix = msPacInterface.getPixels();

            //detect screen capture untuk objek pada game pacman
            msPacInterface.analyseComponents(pix);

            //menentukan bergerak kemana agen
            int action = msPacInterface.simpleExtractor.gameState.agent.move(msPacInterface.simpleExtractor.gameState);
            pacMover.move(action);

            //update arah yang ditampilkan
            double ghostDistance = 1000;
            if (msPacInterface.simpleExtractor.gameState.closestGhost != null) {
                ghostDistance = msPacInterface.simpleExtractor.gameState.agent.cur.dist(msPacInterface.simpleExtractor.gameState.closestGhost);
            }
            if (display) {
                directionComponent.update(action, ghostDistance);
            }
        }
    }

    public void analyseComponents(int[] pix) {
        //reset jarak pil terdekat
        simpleExtractor.gameState.reset();
        //consume mengecek per pixel itu apa
        ArrayList<Drawable> al = simpleExtractor.consume(pix, colors);
        // System.out.println("Components " + al);

        //menggambar objek
        if (display) {
            simpleDisplay.updateObjects(al);
        }
    }

    //screen resolution 1920x1080
    static int left = 850;
    static int top = 450;
    public static int width = 224 * fullscreenSizeTimes;
    public static int height = 256 * fullscreenSizeTimes;
    int[] pixels;
    Robot robot;
    SimpleExtractor simpleExtractor;
    SimpleDisplay simpleDisplay;

    /*
    step mendapatkan warna:
    1. Color.getRGB dari palete warna
     */
    //untuk mengerti, Integer.toBinaryString memberi format 32 bit binary, 8 bit alpha, red, green, blue
    static int blinky = -65536;//;
    static int pinky = -18689;//-;
    static int inky = -16711681;//-;
    static int sue = -18859;//-;
    static int pacMan = -256;
    static int edible = -14408449;//-1825537
    static int pill = -2434305;
    static int wall = -18774; // atau mata hantu saat edible
    static int entrance = -2404305;

    static HashSet<Integer> colors = new HashSet<Integer>();

    static {
        colors.add(blinky);
        colors.add(pinky);
        colors.add(inky);
        colors.add(sue);
        colors.add(pacMan);
        colors.add(edible);
        colors.add(pill);
        colors.add(wall);
    }

    public MsPacInterface() throws Exception {
        //robot adalah class dari oracle untuk input sendiri
        robot = new Robot();
        //array pixel ukuran layar 
        pixels = new int[width * height];
        simpleExtractor = new SimpleExtractor(width, height);
        simpleDisplay = new SimpleDisplay(width, height);
        new JEasyFrame(simpleDisplay, "Extracted", true);
    }

    //getPixels untuk mendapatkan screen game pacman, dari posisi kiri atas lalu panjang lebar
    public int[] getPixels() {
        BufferedImage im = robot.createScreenCapture(new Rectangle(left, top, width, height));
        //getRGB adalah fungsi dari BufferedImage, parameter x awal, y awal, width, height, array tujuan, offeset array, brp pixels per line
        im.getRGB(0, 0, width, height, pixels, 0, width);
        return pixels;
    }
}
