package pacman;

public class Vector2d {

    public double x, y;
    //ini untuk entrance saja, mungkin perlu dibuat class terpisah
    public boolean isCorner = false;

    public Vector2d() {
        this(0, 0);
    }

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d(Vector2d v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void add(Vector2d v) {
        this.x += v.x;
        this.y += v.y;
    }


    public double scalarProduct(Vector2d v) {
        return x * v.x + y * v.y;
    }

    public void set(Vector2d v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return x + " : " + y;
    }

    public static double sqr(double x) {
        return x * x;
    }

    public double sqDist(Vector2d v) {
        return sqr(x - v.x) + sqr(y - v.y);
    }

    public double dist(Vector2d v) {
        return Math.sqrt(sqDist(v));
    }

    public int vectorDirection(Vector2d v) {
        Vector2d w = new Vector2d(this.x - v.x, this.y - v.y);
        if (w.x != 0 && w.y != 0)
            return 0;

        if (w.x == 0) {
            if (w.y > 0)
                return -MsPacInterface.width;
             else if (w.y < 0)
                return MsPacInterface.width;

        } else if (w.y == 0) {
            if (w.x < 0) {
                return 1;
            } else if (w.x > 0) {
                return -1;
            }
        }
        return 0;
    }

    public boolean straightLine(Vector2d v) {
        if (x == v.x && y == v.y) {
            return false;
        } else if (x == v.x || y == v.y) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sameVector(Vector2d v) {
        if (x == v.x && y == v.y) {
            return true;
        } else {
            return false;
        }
    }
}
