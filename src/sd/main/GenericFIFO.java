/**
 * GenericFIFO
 * This class represents a generic Object FIFO queue.
 */
public class GenericFIFO<T> {
    // Data structure to store the queue
    private MyArrayList<T> data;         

    /**
     * Constructor for GenericFIFO
     */
    public GenericFIFO() {
        data = new MyArrayList<T>();
    }

    /**
     * Add an item to the queue
     * @param x
     */
    public void enQueue(T x) {
        data.add(x);
        //System.out.println(this + " data size: " + data.size());
    }    
  
    /**
     * Remove an item from the queue
     * @return boolean
     */
    public T deQueue() {
        if (isEmpty()) {
            return null;
        }
        T object = data.removeFirst();
        return object;
    }
  
    /**
     * Get the front item from the queue
     * @return T
     */
    public T front() {
        return data.get(0);
    }

    /**
     * Check if the queue is empty
     * @return boolean
     */
    public boolean isEmpty() {
        if(data.size() == 0){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Get the size of the queue
     * @return int
     */
    public int size() {
        return data.size();
    }
}