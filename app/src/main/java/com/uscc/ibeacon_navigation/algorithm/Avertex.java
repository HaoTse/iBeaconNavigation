package com.uscc.ibeacon_navigation.algorithm;

/**
 * Created by Oslo on 12/11/16.
 */
public class Avertex implements Comparable {
    public final int x;
    public final int y;
    public Avertex parent;
    public int heuristicCost = 0;
    public int finalCost = 0;

    public Avertex() {
        this.x = 0;
        this.y = 0;
    }

    public Avertex(int x, int y) {
        this.x = x;
        this.y = y;

    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + "]";
    }

}
