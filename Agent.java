package pacman;

import static pacman.MsPacInterface.height;
import static pacman.MsPacInterface.pacMan;
import static pacman.MsPacInterface.width;

import java.awt.*;

public class Agent implements Drawable {

    int[] pixTemp;
    int x, y;
    int w, h;
    Color color;

    int blinky = -65536;//;
    int pinky = -18689;//;
    int inky = -16711681;//;
    int sue = -18859;//;

    int black = -16777216;

    int up = 0;
    int right = 1;
    int down = 2;
    int left = 3;

    Vector2d vUp = new Vector2d(0, -1);
    Vector2d vRight = new Vector2d(1, 0);
    Vector2d vDown = new Vector2d(0, 1);
    Vector2d vLeft = new Vector2d(-1, 0);

    int agentToPowerDist = 48;
    int agentToPowerDist1 = 96;
    int agentToGhostDist = 64;
    int agentToGhostDist1 = 128;
    int agentToGhostDist2 = 144;
    int agentToEdibleDist1 = 128;
    int ghostToPowerDist = 64;

    //dirs urutannya atas,kanan,bawah,kiri
    String directionHelper[] = {"up", "right", "down", "left"};
    static int[] dirs = {-width, 1, width, -1};
    static Vector2d[] vDirs = {
            new Vector2d(0, -1),
            new Vector2d(1, 0),
            new Vector2d(0, 1),
            new Vector2d(-1, 0)};
    Vector2d currentGoal = null;
    final double ghostCost = 50000;

    // distance is the distance in each direction to the nearest wall
    static int[] distance;
    static boolean[] closeToWall;
    static boolean[] ghostInPath;
    static int[] lastColorSearchLastEntrance;
    int move;

    Vector2d cur, tmp;

    public Agent() {
        distance = new int[]{20, 20, 20, 20};
        closeToWall = new boolean[]{false, false, false, false};
        ghostInPath = new boolean[]{false, false, false, false};
        lastColorSearchLastEntrance = new int[]{0, 0, 0, 0};
        cur = new Vector2d();
        tmp = new Vector2d();
        System.out.println("new agent");
    }

    public Agent(ConnectedSet cs, int[] pix) {
        this();
        update(cs, pix);
    }

    public void update(ConnectedSet cs, int[] pix) {
        cs.validate();
        w = cs.width;
        h = cs.height;
        x = cs.xMin + w / 2;
        y = cs.yMin + h / 2 - 2;
        cur.set(x, y);
        for (int i = 0; i < dirs.length; i++) {
            distance[i] = search(x + y * width, pix, dirs[i]);
        }
        this.color = cs.c;
    }

    public int setDir(GameState gs) {
        Vector2d w = new Vector2d(gs.closestEntrance.x - gs.lastEntrance.x, gs.closestEntrance.y - gs.lastEntrance.y);

        if (w.scalarProduct(vUp) > 0) {
            return up;
        } else if (w.scalarProduct(vRight) > 0) {
            return right;
        } else if (w.scalarProduct(vDown) > 0) {
            return down;
        } else if (w.scalarProduct(vLeft) > 0) {
            return left;
        } else {
            return -1;
        }
    }

    public int move(GameState gs) {
        move = -1;
        double best = Integer.MAX_VALUE;
        double curScore;

        boolean rule1 = false;
        for (int i = 0; i < dirs.length; i++) {
            if (distance[i] != 0) {

                tmp.set(cur);
                tmp.add(vDirs[i]);

                curScore= Integer.MAX_VALUE;

                double agentToPower = evaluatePowerPill(tmp, gs);
                double agentToGhost = evaluateGhost(tmp, gs);
                double agentToEdibleGhost = evaluateEdibleGhost(tmp, gs);
                double ghostToPower = evaluatePowerPill(gs.closestGhostToPower, gs);

                if (gs.closestPill != null) {
                    currentGoal = new Vector2d(gs.closestPill);
                    curScore = eval(tmp, gs);
                }

                rule1 = false;
                boolean version2 = false;
                if (agentToPower < agentToPowerDist &&
                        agentToGhost > agentToGhostDist &&
                        ghostToPower > ghostToPowerDist) {
                    rule1 = true;
                } else if (gs.closestPowerPill != null &&
                        agentToGhost < agentToGhostDist1 &&
                        agentToPower < agentToPowerDist1 &&
                        agentToPower < ghostToPower) {
                    currentGoal = new Vector2d(gs.closestPowerPill);
                    curScore = agentToPower;
                    version2 = true;
                } else if (gs.closestPowerPill != null &&
                        agentToGhost < agentToGhostDist1 &&
                        agentToPower < ghostToPower) {
                    currentGoal = new Vector2d(gs.closestPowerPill);
                    curScore = agentToPower;
                } else if (gs.closestEdibleGhost != null &&
                        agentToGhost < agentToGhostDist1 &&
                        agentToEdibleGhost < agentToEdibleDist1) {
                    currentGoal = new Vector2d(gs.closestEdibleGhost);
                    curScore = agentToEdibleGhost;
                } else if (gs.closestGhost != null &&
                        agentToGhost < agentToGhostDist1) {
                } else if (gs.closestEdibleGhost != null &&
                        agentToGhost > agentToGhostDist2 &&
                        agentToEdibleGhost < agentToEdibleDist1) {
                    currentGoal = new Vector2d(gs.closestEdibleGhost);
                    curScore = agentToEdibleGhost;
                    version2 = true;
                } else if (agentToGhost > agentToGhostDist2) {
                    version2 = true;
                }

                gs.setEntrance(SimpleExtractor.entrances);
                if (currentGoal != null
                        && gs.closestPill != null
                        && currentGoal.sameVector(gs.closestPill)) {
                    curScore = evalEntranceToGoal(tmp, gs, curScore);
                }

                double ghostToEntrance = evaluateEntrance(gs.closestGhost, gs);

                curScore *= 1000;
//                System.out.println(curScore);
                if (rule1) {
                    move = (move + 2) % 4;
                }

                //version2 saat tidak ada hantu yg dekat dengan agent
                if (!version2) {
                    if (gs.lastEntrance != null && gs.closestEntrance != null) {
                        boolean isMovingToDirection = i == setDir(gs);
                        Vector2d agentEntranceDirection = new Vector2d(tmp);
                        agentEntranceDirection.add(vDirs[i]);
                        if (isMovingToDirection) {
                            curScore += 500000 / Math.pow(ghostToEntrance, 2);
                            if (gs.closestEntrance.isCorner
                                    && !currentGoal.sameVector(gs.closestPowerPill)) {
                                curScore += 5000;
                            }
                            if (gs.closestGhost != null
                                    && gs.closestEntrance.dist(gs.closestGhost) < GameState.entranceMinDist * 6
                                    && gs.closestEntrance.dist(gs.closestGhost) < gs.closestEntrance.dist(cur)) {
                                curScore += 1000000;
                            }
                        }
                    }
                    if (ghostInPath[i]
                            || gs.lastEntrance != null && (tmp.dist(gs.lastEntrance)<cur.dist(gs.lastEntrance)
                            && gs.closestGhost != null && gs.lastEntrance.dist(gs.closestGhost) < GameState.entranceMinDist*2)) {
                        curScore += 6000000;
                    }
                }

                System.out.printf(directionHelper[i] + "\t%.2f:" + "\tbest:" + best
                        + "\tghost " + ghostInPath[i]
                        + "\tversion2 " + version2 +"\n" , curScore);
                if (curScore < best) {
                    move = i;
                    best = curScore;
                }
            }
        }
        System.out.println();
        move += 1;
        return move;
    }

    public double eval(Vector2d pos, GameState gs) {
        if (gs.closestPill != null) {
            return pos.dist(gs.closestPill);
        } else {
            return 99999;
        }
    }

    public double evalEntranceToGoal(Vector2d pos, GameState gs, double curScore) {
        if (gs.closestEntrance != null) {
            return pos.dist(gs.closestEntrance);
        } else {
            return curScore;
        }
    }


    public double evaluateGhost(Vector2d pos, GameState gs) {
        if (gs.closestGhost != null) {
            return pos.dist(gs.closestGhost);
        } else {
            return 99999;
        }
    }

    public double evaluateEdibleGhost(Vector2d pos, GameState gs) {
        if (gs.closestEdibleGhost != null) {
            return pos.dist(gs.closestEdibleGhost);
        } else {
            return 99999;
        }
    }

    public double evaluatePowerPill(Vector2d pos, GameState gs) {
        if (pos != null && gs.closestPowerPill != null) {
            return pos.dist(gs.closestPowerPill);
        } else {
            return 99999;
        }
    }

    public double evaluateEntrance(Vector2d pos, GameState gs) {
        if (pos != null && gs.closestEntrance != null) {
            return pos.dist(gs.closestEntrance);
        } else {
            return 99999;
        }
    }

    private int search(int p, int[] pix, int delta) {
        int len = 0;
        int i = 0;
        int ghostTiles = 0;
        pixTemp = pix;

        if (delta == -width) {
            i = 0;
        } else if (delta == 1) {
            i = 1;
        } else if (delta == width) {
            i = 2;
        } else if (delta == -1) {
            i = 3;
        }

        closeToWall[i] = false;
        int pp = pix[p];
        ghostInPath[i] = false;
        try {
            while (pp == black
                    || pp == MsPacInterface.pacMan
                    || pp == MsPacInterface.pill
                    || pp == MsPacInterface.edible
                    || pp == blinky
                    || pp == MsPacInterface.entrance) {
                len++;
                //memperkecil jarak pandang agent
                p += delta;
                if (len > 64) {
                    return len;
                }

                //warna hantu saat biru untuk mulut mata
                if (pp == MsPacInterface.edible && pix[p] == MsPacInterface.wall) {
                    return len + 8;
                }

                //untuk mengecek hantu dan save jika ada
                if (p > 0 && p < pix.length && (pix[p] != black && pix[p] != MsPacInterface.pill
                        && pix[p] != pacMan && pix[p] != MsPacInterface.entrance)) {
                    ghostTiles++;
                    if (ghostTiles > 9) {
                        ghostInPath[i] = true;
                    }
                }

                pp = pix[p];
            }
        } catch (Exception e) {
        }
        if (p > 0 && p < width*height) {
            if (pix[p] == inky || pix[p] == pinky || pix[p] == sue) {
                ghostInPath[i] = true;
            }
        }

        Color c = new Color(pp);
        //untuk bordir window pacman supaya dapat tembus pojok ke pojok
        if (pp != black && c.getRed() == c.getBlue() && c.getBlue() == c.getGreen()) {
            return 12;
        }

        if (len < 28) {
            closeToWall[i] = true;
            return 0;
        }
        return len;
    }

    //melakukan draw untuk garis jarak dari pusat agen ke jarak terjauh sebelum tembok
    public void draw(Graphics gg, int ww, int hh) {
        Graphics2D g = (Graphics2D) gg;
        g.setColor(color);
        g.fillRect(x - w / 2, y - h / 2, w, h);
        // now the four lines
        g.setColor(Color.green);
        g.drawLine(x, y, x, y - distance[0]);
        g.drawLine(x, y, x + distance[1], y);
        g.drawLine(x, y, x, y + distance[2]);
        g.drawLine(x, y, x - distance[3], y);
        g.setColor(Color.red);
        if (move > 0) {
            tmp.set(vDirs[move - 1]);
            g.drawLine(x, y, x + (int) tmp.x, y + (int) tmp.y);
        }
    }
}
