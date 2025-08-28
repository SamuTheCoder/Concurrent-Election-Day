/**
 * MyArrayList
 * A class that implements a Object ArrayList
 */
public class MyArrayList<T> {
    // Array to store elements
    private T[] data;   
    private int size;       

    /**
     * Constructor for MyArrayList
     */
    public MyArrayList() {
        this.data = (T[]) new Object[10];
        this.size = 0;
    }

    /**
     * Add an item to the list
     * @param value
     */
    public void add(T value) {
        if (size == data.length) {
            resize();
        }
        data[size++] = value;
    }

    /**
     * Remove the first item from the list
     */
    public T removeFirst() {
        if (size == 0) {
            throw new IndexOutOfBoundsException("List is empty");
        }
        T first = data[0];
        for (int i = 1; i < size; i++) {
            data[i - 1] = data[i];
        }
        data[--size] = null; // clear the last element
        return first;
    }

    /**
     * Get an item from the list
     * @param index
     * @return T
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index");
        }
        return data[index];
    }

    /**
     * Get the size of the list
     * @return int
     */
    public int size() {
        return size;
    }

    /**
     * Resize the list
     */
    private void resize() {
        T[] newData = (T[]) new Object[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        data = newData;
    }

    /**
     * Check if the list contains an item
     * @param o
     * @return boolean
     */
    public boolean query(T o) {
        for (int i = 0; i < size; i++) {
            if (data[i].equals(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear the list
     */
    public void clear() {
        size = 0;
        this.data = (T[]) new Object[10];
    }

    // For tests
    public static void main(String[] args) {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        list.add(8);
        list.add(9);
        list.add(10);
        list.add(11);
        System.out.println(list.size()); // 11
        System.out.println(list.query(11)); // true
        System.out.println(list.query(12)); // false
        list.clear();
        System.out.println(list.size()); // 0
        System.out.println(list.query(3)); // false
    }
}
