
/*
 *  Compilation:  javac PowerOf2IndexMaxPQ.java
 *  Execution:    java PowerOf2IndexMaxPQ
 *
 *  Maximum-oriented, indexed PQ implementation using a multiway heap. Width
 *  of the heap is a parameter in the constructor.
 */

import edu.princeton.cs.introcs.StdOut;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The <tt>PowerOf2IndexMaxPQ</tt> class uses a power-of-2 width heap to
 * implement a maximum-oriented, indexed priority queue of generic keys. It
 * supports the usual <em>insert</em> and <em>delete-the-maximum</em>
 * operations, along with <em>delete</em> and <em>change-the-key</em>
 * methods. In order to let the client refer to keys on the priority queue, an
 * integer between 0 and capacity-1 is associated with each key&mdash;the client
 * uses this integer to specify which key to delete or change. It also supports
 * methods for peeking at the maximum key, testing if the priority queue is
 * empty, and iterating through the keys.
 * <p>
 * This implementation uses a d-way heap along with an array to
 * associate keys with integers in the given range. The <em>insert</em>,
 * <em>delete-the-maximum</em>, <em>delete</em>,
 * <em>change-key</em>, <em>decrease-key</em>, and <em>increase-key</em>
 * operations take logarithmic time. The <em>is-empty</em>, <em>size</em>,
 * <em>max-index</em>, <em>max-key</em>, and <em>key-of</em> operations take
 * constant time. Construction takes time proportional to the specified
 * capacity.
 * <p>
 * For additional documentation, see
 * <a href="http://algs4.cs.princeton.edu/24pq">Section 2.4</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Dean Elzinga
 */
public class MultiwayHeapIndexMaxPQ<Key extends Comparable<Key>>
    implements Iterable<Integer> {
  private final int D;                 // Width  of the d-way heap.

  //static private final int P_DEFAULT = 3; // Default to 8-heap.
  static public final int D_DEFAULT = 6;
  //private int p;           // 2-to-the-p heap. Each parent has 1<<p children.
  private int capacity;        // Maximum number of elements on PQ.
  private int N;           // Number of elements on PQ.
  private int[] pq;        // Heap using 0-based indexing.
  private int[] qp;        // Inverse of pq - qp[pq[i]] = pq[qp[i]] = i.
  private Key[] keys;      // keys[i] = priority of i

  /**
   * Initializes an empty multiway indexed priority queue with indices between 0
   * and capacity-1.
   *
   * @param capacity the keys on the priority queue are index from 0 to size-1
   * @throws java.lang.IllegalArgumentException if capacity < 0
   */
  public MultiwayHeapIndexMaxPQ(int D, int capacity) {
    this.D = D;
    if (capacity < 0) {
      throw new IllegalArgumentException();
    }
    this.capacity = capacity;
    keys = (Key[]) new Comparable[capacity];    // Make this of length 
    pq = new int[capacity];
    qp = new int[capacity];                   // Make this of length size
    for (int i = 0; i <= capacity - 1; i++) {
      qp[i] = -1;
    }
  }

  /**
   * Initializes an empty multiway indexed priority queue with indices between 0
   * and capacity-1.
   *
   * @param capacity the keys on the priority queue are index from 0 to size-1
   * @throws java.lang.IllegalArgumentException if size < 0
   */
  public MultiwayHeapIndexMaxPQ(int capacity) {
    this(D_DEFAULT, capacity);
  }

  /**
   * Is the priority queue empty?
   *
   * @return true if the priority queue is empty; false otherwise
   */
  public boolean isEmpty() {
    return N == 0;
  }

  /**
   * Is i an index on the priority queue?
   *
   * @param i an index
   * @throws java.lang.IndexOutOfBoundsException unless (0 &le; i < capacity)
   */
  public boolean contains(int i) {
    if (i < 0 || i >= capacity) {
      throw new IndexOutOfBoundsException();
    }
    return qp[i] != -1;
  }

  /**
   * Returns the number of keys on the priority queue.
   *
   * @return the number of keys on the priority queue
   */
  public int size() {
    return N;
  }

  /**
   * Associates key with index i.
   *
   * @param i an index
   * @param key the key to associate with index i
   * @throws java.lang.IndexOutOfBoundsException unless 0 &le; i < capacity 
   * @thr
   * ows java.util.IllegalArgumentException if there already is an item
   * associated with index i
   */
  public void insert(int i, Key key) {
    if (i < 0 || i >= capacity) {
      throw new IndexOutOfBoundsException();
    }
    if (contains(i)) {
      throw new IllegalArgumentException("Index already in the priority queue.");
    }
    qp[i] = N;
    pq[N] = i;
    keys[i] = key;
    swim(N++);
  }

  /**
   * Returns an index associated with a maximum key.
   *
   * @return an index associated with a maximum key
   * @throws java.util.NoSuchElementException if priority queue is empty
   */
  public int maxIndex() {
    if (N == 0) {
      throw new NoSuchElementException("Priority queue underflow");
    }
    return pq[0];
  }

  /**
   * Returns a maximum key.
   *
   * @return a maximum key
   * @throws java.util.NoSuchElementException if priority queue is empty
   */
  public Key maxKey() {
    if (N == 0) {
      throw new NoSuchElementException("Priority queue underflow");
    }
    return keys[pq[0]];
  }

  /**
   * Removes a maximum key and returns its associated index.
   *
   * @return an index associated with a maximum key
   * @throws java.util.NoSuchElementException if priority queue is empty
   */
  public int delMax() {
    if (N == 0) {
      throw new NoSuchElementException("Priority queue underflow");
    }
    int max = pq[0];
    swap(0, --N);
    sink(0);
    qp[max] = -1;          // -1 represents empty.
    keys[pq[N]] = null;    // Help with garbage collection.
    pq[N] = -1;            // Not needed.
    return max;
  }

  /**
   * Returns the key associated with index i.
   *
   * @param i the index of the key to return
   * @return the key associated with index i
   * @throws java.lang.IndexOutOfBoundsException unless 0 &le; i < NMAX @ throws
   * java.util.NoSuchElementException no key is associated with index i
   */
  public Key keyOf(int i) {
    if (i < 0 || i >= capacity) {
      throw new IndexOutOfBoundsException();
    }
    if (!contains(i)) {
      throw new NoSuchElementException("Index not in priority queue.");
    } else {
      return keys[i];
    }
  }

  /**
   * Change the key associated with index i to the specified value.
   *
   * @param i the index of the key to change
   * @param key change the key assocated with index i to this key
   * @throws java.lang.IndexOutOfBoundsException unless 0 &le; i < NMAX @
   * deprecated Replaced by changeKey()
   */
  @Deprecated public void change(int i, Key key) {
    changeKey(i, key);
  }

  /**
   * Change the key associated with index i to the specified value.
   *
   * @param i the index of the key to change
   * @param key new key for index i, can be greater or less than current key.
   * @throws java.lang.IndexOutOfBoundsException unless 0 <= i < capacity @thr
   * ows java.util.NoSuchElementException no key associated with index i
   */
  public void changeKey(int i, Key key) {
    if (i < 0 || i >= capacity) {
      throw new IndexOutOfBoundsException();
    }
    if (!contains(i)) {
      throw new NoSuchElementException("Index not in priority queue.");
    }
    keys[i] = key;
    swim(qp[i]); // Must swim AND sink if we don't specify increase/decrease.
    sink(qp[i]);
  }

  /**
   * Decrease the key associated with index i to the specified value.
   *
   * @param i the index of the key to decrease
   * @param key new value for key at index i, must be strictly lower relative to
   * Key.compareTo().
   * @throws java.lang.IndexOutOfBoundsException unless 0 <= i < capacity @thr
   * ows java.lang.IllegalArgumentException if key &ge; key associated with
   * index i @throws java.util.NoSuchElementException no key is associated with
   * index i
   */
  public void decreaseKey(int i, Key key) {
    if (i < 0 || i >= capacity) {
      throw new IndexOutOfBoundsException();
    }
    if (!contains(i)) {
      throw new NoSuchElementException("Index not in priority queue.");
    }
    if (keys[i].compareTo(key) <= 0) {
      throw new IllegalArgumentException(
          "decreaseKey() argument must strictly decrease key.");
    }
    keys[i] = key;
    swim(qp[i]);
  }

  /**
   * Increase the key associated with index i to the specified value.
   *
   * @param i the index of the key to increase
   * @param key new value for key at index i, must be strictly greater relative
   * to Key.compareTo().
   * @throws java.lang.IndexOutOfBoundsException unless 0 <= i < capacity @thr
   * ows java.lang.IllegalArgumentException if key &le; key associated with
   * index i @throws java.util.NoSuchElementException no key is associated with
   * index i
   */
  public void increaseKey(int i, Key key) {
    if (i < 0 || i >= capacity) {
      throw new IndexOutOfBoundsException();
    }
    if (!contains(i)) {
      throw new NoSuchElementException("Index not in priority queue.");
    }
    if (keys[i].compareTo(key) >= 0) {
      throw new IllegalArgumentException(
          "increaseKey() argument must strictly increase key.");
    }
    keys[i] = key;
    sink(qp[i]);
  }

  /**
   * Remove the key associated with index i.
   *
   * @param i the index of the key to remove
   * @throws java.lang.IndexOutOfBoundsException unless 0 &le; i < capacity 
   * @thr
   * ows java.util.NoSuchElementException no key is associated with index i
   */
  public void delete(int i) {
    if (i < 0 || i >= capacity) {
      throw new IndexOutOfBoundsException();
    }
    if (!contains(i)) {
      throw new NoSuchElementException("Index not in priority queue.");
    }
    int index = qp[i];
    swap(index, --N); // Last element is at N-1.
    swim(index);
    sink(index);
    keys[i] = null; // Help with garbage collection.
    qp[i] = -1;
  }

  /*
   * General helper functions
   */
  private boolean less(int i, int j) {
    return keys[pq[i]].compareTo(keys[pq[j]]) < 0;
  }

  private void swap(int i, int j) {
    int swap = pq[i];
    pq[i] = pq[j];
    pq[j] = swap;
    qp[pq[i]] = i;
    qp[pq[j]] = j;
  }

  /*
   * Heap helper functions
   */
  /* Parent of k in this 0-based, d-ary heap is at (k-1)>>p instead 
   * of at k/2 in the 1-based, binary case.
   */
  private void swim(int k) {
    while (k > 0 && less((k - 1) / D, k)) {
      swap(k, (k - 1) / D);
      k = (k - 1) / D;
    }
  }

  // First child of item at k is at D * k + 1, rather than at 2*k,
  // as in the 0-based, binary case.
  private void sink(int k) {
    while (D * k + 1 <= N - 1) { // While k has a child in the array,
//    while ((k << p) + 1 < N) {
//      int next = (k << p) + 1;
      int next = D * k + 1; // first child of k
      int last = next + D - 1;
      // lastChild = maximum(lastChild, N-1):
      last = last <= N - 1 ? last : N - 1;
      // firstChild gets index of max item among (1<<p) sibs.
      for (int sibling = next + 1; sibling <= last; sibling++) {
        if (less(next, sibling)) {
          next = sibling;
        }
      }
      if (!less(k, next)) {
        break;
      }
      swap(k, next);
      k = next;
    }
  }

  /*
   * Iterators
   */
  /**
   * Returns an iterator that iterates over the keys on the priority queue in
   * ascending order. The iterator doesn't implement <tt>remove()</tt> since
   * it's optional.
   *
   * @return an iterator that iterates over the keys in ascending order
   */
  public Iterator<Integer> iterator() {
    return new HeapIterator();
  }

  private class HeapIterator implements Iterator<Integer> {
    // Create a new pq.
    private MultiwayHeapIndexMaxPQ<Key> copy;

    // Add all elements to the copy of the heap.
    // This takes linear time; already in heap order, so no keys move.
    public HeapIterator() {
      copy = new MultiwayHeapIndexMaxPQ<Key>(pq.length);
      for (int i = 0; i <= N - 1; i++) {
        copy.insert(pq[i], keys[pq[i]]);
      }
    }

    public boolean hasNext() {
      return !copy.isEmpty();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public Integer next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return copy.delMax();
    }
  }

  /**
   * Unit tests the <tt>PowerOf2IndexMinPQ</tt> data type.
   */
  public static void main(String[] args) {
    // insert a bunch of strings
    String[] strings = {
      "it", "was", "the", "best", "of", "times", "it", "was", "the", "worst"
    };

    MultiwayHeapIndexMaxPQ<String> pq =
        new MultiwayHeapIndexMaxPQ<String>(strings.length);
    for (int i = 0; i < strings.length; i++) {
      pq.insert(i, strings[i]);
    }

    // delete and print each key
    while (!pq.isEmpty()) {
      int i = pq.delMax();
      StdOut.println(i + " " + strings[i]);
    }
    StdOut.println();

    // reinsert the same strings
    for (int i = 0; i < strings.length; i++) {
      pq.insert(i, strings[i]);
    }

    // print each key using the iterator
    for (int i : pq) {
      StdOut.println(i + " " + strings[i]);
    }
    while (!pq.isEmpty()) {
      pq.delMax();
    }
  }
}
