package pacman;

        import java.util.Collection;
        import java.util.HashMap;
        import java.awt.*;
        import java.util.ArrayList;

public class GameState implements Drawable {

    static int strokeWidth = 5;
    static Stroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    Collection<ConnectedSet> pills;

    Collection<ConnectedSet> ghosts;
    Agent agent;
    Vector2d closestPill;
    Vector2d closestPowerPill;
    Vector2d closestEdibleGhost;
    Vector2d closestGhost;
    Vector2d closestGhostToPower;
    Vector2d closestEntrance;
    Vector2d previousLastEntrance;
    static Vector2d lastEntrance;

    boolean retry = false;
    boolean hasDecided = false;
    boolean newLastEntrance = false;

    Vector2d tmp;

    static int entranceMinDist = 9;

    public GameState() {
        agent = new Agent();
        tmp = new Vector2d();
    }

    public void reset() {
        closestPill = null;
        closestGhost = null;
        closestGhostToPower = null;
        closestEdibleGhost = null;
    }

    public void update(ConnectedSet cs, int[] pix) {
        if (cs.isPacMan()) {
            agent.update(cs, pix);
            retry = false;
        } else if (cs.ghostLike()) {
            tmp.set((cs.xMin + cs.xMax) / 2, (cs.yMin + cs.yMax) / 2);//offset mata dengan titik tengah pacman
            if (closestGhost == null) {
                closestGhost = new Vector2d(tmp);
            } else if (tmp.dist(agent.cur) < closestGhost.dist(agent.cur)) {
                closestGhost.set(tmp);
            }

            if (closestGhostToPower == null) {
                closestGhostToPower = new Vector2d(tmp);
            } else if (closestPowerPill != null && tmp.dist(closestPowerPill) < closestGhostToPower.dist(closestPowerPill)) {
                closestGhostToPower.set(tmp);
            }

        } else if (cs.pill()) {
            // keep track of the position of the closest pill
            tmp.set(cs.x, cs.y);
            if (closestPill == closestGhost) {
                closestPill = null;
            }
            //TODO jangan goal yg tembus hantu
            if (closestGhost != null && tmp.dist(closestGhost) < 16) {
                closestPill = null;
            } else if (closestPill == null) {
                closestPill = new Vector2d(tmp);
            } else if (tmp.dist(agent.cur) < closestPill.dist(agent.cur)) {
                closestPill.set(tmp);
            }
        } else if (cs.powerPill() && closestEdibleGhost == null) {
            // keep track of the position of the closest pill
            tmp.set(cs.x, cs.y);
            if (closestPowerPill == null) {
                closestPowerPill = new Vector2d(tmp);
            } else if (tmp.dist(agent.cur) < closestPowerPill.dist(agent.cur)) {
                closestPowerPill.set(tmp);
            }
        } else if (cs.edible()) {
            // keep track of the position of the closest pill
            tmp.set(cs.x, cs.y);
            if (closestEdibleGhost == null) {
                closestEdibleGhost = new Vector2d(tmp);
            } else if (tmp.dist(agent.cur) < closestEdibleGhost.dist(agent.cur)) {
                closestEdibleGhost.set(tmp);
            }
            closestPowerPill = null;
        } else if (cs.ready()) {
            lastEntrance = null;
            closestEntrance = null;
            closestPowerPill = null;
            newLastEntrance = false;
            hasDecided=false;
        }
    }

    public void setEntrance(ArrayList<ConnectedSet> entrances) {
        if (newLastEntrance) {
            if (lastEntrance != null && agent != null && agent.cur.dist(lastEntrance) > entranceMinDist * 2) {
                newLastEntrance = false;
            }
            if (search(lastEntrance.vectorDirection(closestEntrance))) {
                newLastEntrance = false;
            }
            return;
        }
        for (ConnectedSet entrance : entrances) {
            tmp.set(entrance.x, entrance.y);
//            System.out.println("tmp " + tmp.toString());
            if (tmp.dist(agent.cur) > 160) {
                //tidak usah dihitung karena tidak cocok
            } else {
                //vector test untuk menambah akurasi dist dari agent ke pill, dengna menengahkan pill
                Vector2d test = new Vector2d(tmp);
                double dist1 = 99999;
                hasDecided = false;
                if (agent != null) {
                    dist1 = agent.cur.dist(test);
                    test.x += 2;
                    dist1 = Math.min(dist1, agent.cur.dist(test));
                    test.y += 2;
                    dist1 = Math.min(dist1, agent.cur.dist(test));
                    test.x -= 2;
                    dist1 = Math.min(dist1, agent.cur.dist(test));
                }
                if (dist1 < entranceMinDist && !retry && lastEntrance != tmp) {
                    if (lastEntrance != null && agent.currentGoal != null
                            && closestPill != null && agent.currentGoal.sameVector(closestPill)) {
                        previousLastEntrance = new Vector2d(lastEntrance);
                    }
                    lastEntrance = new Vector2d(tmp);
//                    System.out.println("redo entrance");
                    closestEntrance = null;
                    retry = true;
//                    if (!(closestPowerPill != null && agent.currentGoal.sameVector(closestPowerPill))
//                            && !(closestEdibleGhost != null && agent.currentGoal.sameVector(closestEdibleGhost))) {
                    if (!(closestEdibleGhost != null && agent.currentGoal.sameVector(closestEdibleGhost))) {
                        agent.currentGoal = closestPill;
//                        }
                    }
                    setEntrance(entrances);
                    break;
                }
//                else if (lastEntrance != null && !hasDecided) {
                else if (lastEntrance != null && !hasDecided && !(previousLastEntrance != null && previousLastEntrance.sameVector(tmp))) {
                    int move = lastEntrance.vectorDirection(tmp);
                    boolean wallOrGhostDetect = search(move);
//                    boolean isReachable = Agent.distance[direction(move)] + 8 > tmp.dist(agent.cur);
                    boolean straightGoalEntrance = agent.currentGoal != null
                            && lastEntrance.vectorDirection(agent.currentGoal) != 0
                            && !wallOrGhostDetect;
                    boolean entranceRequirements = lastEntrance.straightLine(tmp)
                            && !wallOrGhostDetect;
                    if (straightGoalEntrance) {
                        entranceRequirements = entranceRequirements
                                && lastEntrance.vectorDirection(agent.currentGoal) == move;
                    }

                    if (closestEntrance == null && entranceRequirements) {
                        closestEntrance = new Vector2d(tmp);
                        closestEntrance.isCorner = tmp.isCorner;
                    } else if (agent != null && agent.currentGoal != null && closestEntrance != null) {
//                        if (tmp.dist(agent.currentGoal) < closestEntrance.dist(agent.currentGoal)) {
                        if (tmp.dist(agent.cur) + tmp.dist(agent.currentGoal) < closestEntrance.dist(agent.cur) + closestEntrance.dist(agent.currentGoal)) {
                            if (entranceRequirements) {
                                closestEntrance.set(tmp);
                                closestEntrance.isCorner = tmp.isCorner;
                            }
                        }
                    }
                }
            }
        }
        if (closestEntrance != null) {
//            retry=false;
            newLastEntrance = true;
        }
    }

    private boolean search(int delta) {
        if (delta == -MsPacInterface.width) {
            return Agent.ghostInPath[0] || Agent.closeToWall[0];
        } else if (delta == 1) {
            return Agent.ghostInPath[1] || Agent.closeToWall[1];
        } else if (delta == MsPacInterface.width) {
            return Agent.ghostInPath[2] || Agent.closeToWall[2];
        } else if (delta == -1) {
            return Agent.ghostInPath[3] || Agent.closeToWall[3];
        } else {
            return true;
        }
    }

    //draw garis dari tujuan ke pacman
    public void draw(Graphics gg, int w, int h) {
        //To change body of implemented methods use File | Settings | File Templates.
        Graphics2D g = (Graphics2D) gg;

        if (agent != null) {
            agent.draw(g, w, h);
            if (closestGhost != null) {
                g.setStroke(stroke);
                g.setColor(Color.MAGENTA);
                g.drawLine((int) closestGhost.x, (int) closestGhost.y, (int) agent.cur.x, (int) agent.cur.y);
            }
            if (closestPill != null) {
                g.setStroke(stroke);
                g.setColor(Color.cyan);
                g.drawString("P", (int) closestPill.x, (int) closestPill.y);
//                g.drawLine((int) closestPill.x, (int) closestPill.y, (int) agent.cur.x, (int) agent.cur.y);
            }
            if (closestEntrance != null) {
                g.setStroke(stroke);
                g.setColor(Color.PINK);
                g.drawLine((int) closestEntrance.x, (int) closestEntrance.y, (int) agent.cur.x, (int) agent.cur.y);
            }
            if (agent.currentGoal != null) {
                g.setStroke(stroke);
                g.setColor(Color.LIGHT_GRAY);
//                g.drawLine((int) agent.currentGoal.x, (int) agent.currentGoal.y, (int) agent.cur.x, (int) agent.cur.y);
                g.drawOval((int) agent.currentGoal.x, (int) agent.currentGoal.y, 8, 8);
            }
            if (lastEntrance != null) {
//                System.out.println(lastEntrance.toString());
                g.setStroke(stroke);
                g.setColor(Color.CYAN);
                g.drawLine((int) lastEntrance.x, (int) lastEntrance.y, (int) lastEntrance.x + 4, (int) lastEntrance.y + 4);
            }
        }
    }
}
