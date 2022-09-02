package org.imperial.activemilespro.interface_utility;

public class MyInteger {
    private int value;

    public MyInteger(int p) {
        value = p;
    }

    public void add(int p) {
        value += p;
    }

    public int get() {
        return value;
    }
}
