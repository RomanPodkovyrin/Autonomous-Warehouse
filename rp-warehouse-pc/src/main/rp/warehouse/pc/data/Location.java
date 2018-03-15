package rp.warehouse.pc.data;

import lejos.geom.Point;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Location(Point point) {
        this.x = (int) point.x;
        this.y = (int) point.y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Point toPoint() {
        return new Point(x, y);
    }

    @Override
    public String toString() {
        return "X: " + this.x + ", Y: " + this.y;
    }

    @Override
    public boolean equals(Object o) {
        Location l = (Location) o;
        return this.x == l.x && this.y == l.y;
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }
}
