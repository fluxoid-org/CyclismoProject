package org.fluxoid.utils;

/**
 * Created by fluxoid on 01/02/17.
 */
public class RotatingView<V> {

    private final V [] items;
    private int headIndex = 0;

    public RotatingView(V[] items) {
        assert(items.length >= 1);
        this.items = items;
    }

    public V rotate() {
        V result = items[headIndex];
        headIndex = (headIndex + 1) % items.length;
        return result;
    }

    public static void main(String [] args) {
        Integer[] ints = new Integer[] {1,2,3,4};
        RotatingView<Integer> l = new RotatingView<>(ints);
        System.out.println(l.rotate());
        System.out.println(l.rotate());
        System.out.println(l.rotate());
        System.out.println(l.rotate());
        System.out.println(l.rotate());
        System.out.println(l.rotate());
        System.out.println(l.rotate());
        System.out.println(l.rotate());
        System.out.println(l.rotate());
    }
}
