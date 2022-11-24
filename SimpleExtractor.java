package pacman;

import java.util.*;

import static pacman.MsPacInterface.*;

public class SimpleExtractor {

    int black = -16777216;
    boolean valid, firstPreProcess = false;
    static ArrayList<ConnectedSet> entrances = new ArrayList<>();

    int pillOffset = 16;
    int wallOffsetUp = 3;
    int wallOffsetDown = 6;
    int wallOffsetCheckWall = 10;
    int[] temp;

    int ghostLairWallUp = 114 * fullscreenSizeTimes;
    int ghostLairWallDown = 148 * fullscreenSizeTimes;
    int ghostLairWallLeft = 82 * fullscreenSizeTimes;
    int ghostLairWallRight = 137 * fullscreenSizeTimes;

    int width, height;
    IntStack stack;
    HashSet uniques;
    GameState gameState;

    public SimpleExtractor(int width, int height) {
        this.width = width;
        this.height = height;
        int size = 4 * width * height;
        stack = new IntStack(size);
        uniques = new HashSet();
        gameState = new GameState();
        valid = false;

    }

    //mengecek apakah pixel pada pix merupakan warna pada set color, jika ya 
    public ArrayList<Drawable> consume(int[] pix, Set<Integer> colors) {
        ArrayList<Drawable> objects = new ArrayList<Drawable>();
        ArrayList<ConnectedSet> pills = new ArrayList<ConnectedSet>();
        ConnectedSet agentTemp = new ConnectedSet(0, 0, 0);
        temp = pix.clone();

        for (int pixIndex = 0; pixIndex < pix.length; pixIndex++) {
            //tidak perlu mengecek sarang hantu
            if (inRange(pixIndex)) {
                continue;
            }
            if (pix[pixIndex] != black && colors.contains(pix[pixIndex])) {
                //connected set adalah definisi objek pada game
                ConnectedSet cs = consume(pix, pixIndex, pix[pixIndex]);
                if (cs.pill()) {
                    if (entrances.isEmpty()) {
//                      membuat entrance point
                        firstPreProcess = true;
                        int firstIndex = pixIndex;
                        int tempIndex = pixIndex;

                        while (tempIndex < pix.length) {
                            //pos disini adalah jarak antar pill, angka 1 untuk jarak ke tembok
                            int posRight = tempIndex + pillOffset;
                            int posRight1 = posRight + wallOffsetDown;
                            int posLeft = tempIndex - pillOffset;
                            int posLeft1 = posLeft - wallOffsetUp;
                            int posDown = tempIndex + pillOffset * width > pix.length - 1 ? pix.length - 1 : tempIndex + pillOffset * width;
                            int posDown1 = posDown + wallOffsetDown * width;
                            int posUp = tempIndex - pillOffset * width < 0 ? 0 : tempIndex - pillOffset * width;
                            int posUp1 = posUp - wallOffsetUp * width;

                            //mencegah entrance lain kedetect dengan mengecek diluar daerah pill
                            int posRightDown = tempIndex + 4 + 4 * width;
                            int posLeftDown = tempIndex - 1 + 4 * width;
                            int posLeftUp = tempIndex - 1 - width;
                            int posRightUp = tempIndex + 4 - width;
//                            boolean isPowerPill = temp[tempIndex - 1] == cs.fg;
                            boolean entranceNearby = temp[posUp] == entrance || temp[posLeft] == entrance;
                            boolean isEntrance = false;
                            boolean isCorner = false;

                            if (isPixelPill(temp, tempIndex, cs.fg)
                                    && !entranceNearby) {
                                boolean pillRight = isPixelPill(temp, posRight, cs.fg);
                                boolean pillLeft = isPixelPill(temp, posLeft, cs.fg);
                                boolean pillUp = isPixelPill(temp, posDown, cs.fg);
                                boolean pillDown = isPixelPill(temp, posUp, cs.fg);
                                boolean blackRight = isPixelBlack(temp, posRight1);
                                boolean blackLeft = isPixelBlack(temp, posLeft1);
                                boolean blackUp = isPixelBlack(temp, posUp1);
                                boolean blackDown = isPixelBlack(temp, posDown1);
                                if ((pillRight || pillLeft)
                                        && (pillUp || pillDown)) {
                                    isEntrance = true;
                                    if (!((pillRight || blackRight) && (pillLeft || blackLeft)) &&
                                            !((pillUp) && (pillDown))) {
                                        isCorner = true;
                                    }
                                } else if ((pillRight && pillLeft)
                                        && (blackUp || blackDown)) {
                                    isEntrance = true;
                                } else if ((pillUp && pillDown)
                                        && (blackLeft || blackRight)) {
                                    isEntrance = true;
                                }
                            } else if (isPixelBlack(temp, tempIndex) && (isPixelBlack(temp, posLeftDown) && isPixelBlack(temp, posLeftUp)
                                    && isPixelBlack(temp, posRightDown) && isPixelBlack(temp, posRightUp))) {
                                if ((!checkWall(tempIndex, temp, -width, pillOffset + wallOffsetCheckWall)
                                        || !checkWall(tempIndex, temp, width, pillOffset + wallOffsetCheckWall))
                                        && (!checkWall(tempIndex + 3, temp, -width, pillOffset + wallOffsetCheckWall)
                                        || !checkWall(tempIndex + 3, temp, width, pillOffset + wallOffsetCheckWall))
                                        && (!checkWall(tempIndex, temp, 1, pillOffset + wallOffsetCheckWall)
                                        || !checkWall(tempIndex, temp, -1, pillOffset + wallOffsetCheckWall))
                                        && (!checkWall(tempIndex + 3 * width, temp, 1, pillOffset + wallOffsetCheckWall)
                                        || !checkWall(tempIndex + 3 * width, temp, -1, pillOffset + wallOffsetCheckWall))) {
                                    isEntrance = true;
                                }
                            } else if (isPixelYellow(temp, tempIndex)) {
                                if ((!checkWallYellow(tempIndex, temp, -width, pillOffset + wallOffsetCheckWall)
                                        || !checkWallYellow(tempIndex, temp, width, pillOffset + wallOffsetCheckWall))
                                        && (!checkWallYellow(tempIndex + 3, temp, -width, pillOffset + wallOffsetCheckWall)
                                        || !checkWallYellow(tempIndex + 3, temp, width, pillOffset + wallOffsetCheckWall))
                                        && (!checkWallYellow(tempIndex, temp, 1, pillOffset + wallOffsetCheckWall)
                                        || !checkWallYellow(tempIndex, temp, -1, pillOffset + wallOffsetCheckWall))
                                        && (!checkWallYellow(tempIndex + 3 * width, temp, 1, pillOffset + wallOffsetCheckWall)
                                        || !checkWallYellow(tempIndex + 3 * width, temp, -1, pillOffset + wallOffsetCheckWall))) {
                                    isEntrance = true;
                                }
                            }
                            if (isEntrance) {
                                temp[tempIndex] = temp[tempIndex + 1] = temp[tempIndex + width] = temp[tempIndex + 1 + width] = MsPacInterface.entrance;
                                ConnectedSet exEntrance = consume(temp, tempIndex, temp[tempIndex]);
                                exEntrance.isCorner = isCorner;
                                temp[tempIndex] = temp[tempIndex + 1] = temp[tempIndex + width] = temp[tempIndex + 1 + width] = MsPacInterface.entrance;
                                pills.add(exEntrance);
                            }

                            //untuk ganti baris firstindex dan kolom tempindex dari array pixel, dengan index
                            if (tempIndex % width + pillOffset > width) {
                                firstIndex += pillOffset * width;
                                tempIndex = firstIndex;
                            } else tempIndex += pillOffset;
                        }
                    }
                    pills.add(cs);
                } else {
                    objects.add(cs);
                }
                if (cs.isPacMan()) {
                    agentTemp = cs;
                } else {
                    gameState.update(cs, pix);
                }
            }
        }

        //pertama gamestate update pill, baru agent, lalu entrance
        saveEntrancePoints(pills);
        gameState.update(agentTemp, temp);
        objects.addAll(pills);
        objects.addAll(entrances);
        objects.add(gameState);
        return objects;
    }

    public boolean checkWallYellow(int index, int[] pix, int delta, int offset) {
        for (int i = 0; i <= offset; i++) {
            if (index + i * delta < pix.length
                    && pix[index + i * delta] != black
                    && pix[index + i * delta] != pacMan)
                return true;
        }
        return false;
    }

    public boolean checkWall(int index, int[] pix, int delta, int offset) {
        for (int i = 0; i <= offset; i++) {
            if (index + i * delta < pix.length
                    && pix[index + i * delta] != black) return true;
        }
        return false;
    }

    public boolean inRange(int pixIndex) {
        if (pixIndex % width >= ghostLairWallLeft
                && pixIndex % width <= ghostLairWallRight
                && pixIndex / width >= ghostLairWallUp
                && pixIndex / width <= ghostLairWallDown) return true;
        else return false;
    }

    public boolean isPixelPill(int[] pix, int index, int fg) {
        if (index > 0 && index < pix.length && pix[index] == fg) return true;
        else return false;
    }

    public boolean isPixelBlack(int[] pix, int index) {
        if (index > 0 && index < pix.length && pix[index] == black) return true;
        else return false;
    }

    public boolean isPixelYellow(int[] pix, int index) {
        if (index > 0 && index < pix.length && pix[index] == pacMan) return true;
        else return false;
    }

    public void saveEntrancePoints(ArrayList<ConnectedSet> pills) {
//        System.out.println(pills.size());
        if (pills.size() > 200 && !valid) {
            valid = true;
            firstPreProcess = false;
            for (ConnectedSet pill : pills) {
                if (pill.fg == MsPacInterface.entrance) {
//                    System.out.println("added");
                    entrances.add(pill);
                }
            }
        } else if (pills.isEmpty()) {
            valid = false;
        } else {
            //biar ndak hilang entrancenya
            for (ConnectedSet entrance : entrances) {
                int pixIndex = entrance.xMin + entrance.yMin * width;
                temp[pixIndex] = temp[pixIndex + 1] = temp[pixIndex + width] = temp[pixIndex + 1 + width] = MsPacInterface.entrance;
            }
        }
    }

    //consume mengambil komponen layar screen capture, dengan cara mengarah ke 4 arah dengan stack, tiap titik di add ke connected set
    //digunakan untuk mengetahui seberapa panjang suatu titik warna yang terhubung
    public ConnectedSet consume(int[] pix, int pixIndex, int fg) {
        ConnectedSet connectedSet = new ConnectedSet(pixIndex % width, pixIndex / width, fg);
        stack.reset();
        stack.push(pixIndex);
        while (!stack.isEmpty()) {
            pixIndex = stack.pop();
            if (pix[pixIndex] == fg) {
                connectedSet.add(pixIndex % width, pixIndex / width, pixIndex, fg);

                int cx = pixIndex % width;
                int cy = pixIndex / width;

                pix[pixIndex] = 0;

                if (cx > 0) {
                    stack.push(pixIndex - 1);
                }
                if (cy > 0) {
                    stack.push(pixIndex - width);
                }
                if (cx < (width - 1)) {
                    stack.push(pixIndex + 1);
                }
                if (cy < (height - 1)) {
                    stack.push(pixIndex + width);
                }
            }
        }
        return connectedSet;
    }
}
