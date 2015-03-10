// Default package, like the rest of Algorithms 4 course library, to facilitate
// automated profiling and grading.

// Imports vital to the implementation.
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

// Imports used for testing only.
import edu.princeton.cs.introcs.StdRandom;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Scanner;

public class PowerOf2HeapMinPQ<Key> implements Iterable<Key> {
    private final int p;
    /**
     * Default width for 2^^p-wide heap. At time of writing, p is 3, an 8-heap.
     */
    static public final int POWER_DEFAULT = 3;
    private Key[] pq;                   // Store items at indices 0 to N-1.
    private int N;                       // Number of items on d-ary queue.
    private Comparator<Key> comparator;  // Optional Comparator.

    /**
     * Initializes an empty priority queue with the given initial capacity.
     *
     * @param p the power of 2 that is the arity of the d-ary heap
     * @param initCapacity the initial capacity of the priority queue
     */
    public PowerOf2HeapMinPQ(int p, int initCapacity) {
        assert 0 <= p && p <= Integer.SIZE - 1;
        this.p = p;
        pq = (Key[]) new Object[initCapacity];
        N = 0;
    }

    /**
     * Initializes an empty priority queue using d-ary heap.
     *
     *
     * @param p the heap width as a power of 2. 0 represents a unary
     * insertion-sorted array heap.
     */
    public PowerOf2HeapMinPQ(int p) {
        this(p, 1);
    }

    /**
     * Initializes an empty d-ary priority queue using the default arity.
     */
    public PowerOf2HeapMinPQ() {
        this(POWER_DEFAULT, 1);
    }

    /**
     * Initializes an empty d-ary priority queue with the given initial
     * capacity, using the given comparator.
     *
     * @param p the arity of the d-ary heap
     * @param initCapacity the initial capacity of the priority queue
     * @param comparator the order in which to compare the keys
     */
    public PowerOf2HeapMinPQ(int p, int initCapacity, Comparator<Key> comparator) {
        assert 0 <= p && p <= Integer.SIZE - 1;
        this.p = p;
        this.comparator = comparator;
        pq = (Key[]) new Object[initCapacity];
        N = 0;
    }

    /**
     * Initializes an empty 2^^n-ary priority p using the given comparator.
     *
     * @param p the power of 2 width of the heap
     * @param comparator the order in which to compare the keys
     */
    public PowerOf2HeapMinPQ(int p, Comparator<Key> comparator) {
        this(p, 1, comparator);
    }

    /**
     * Initializes an empty d-ary priority queue using the given comparator and
     * the default 2-power arity.
     *
     * @param comparator the order in which to compare the keys
     */
    public PowerOf2HeapMinPQ(Comparator<Key> comparator) {
        this(POWER_DEFAULT, 1, comparator);
    }

    /**
     * Initializes a d-ary priority queue from the array of keys. Like Sedgewick
     * and Wayne's reference binary heap, this d-ary generalization takes time
     * proportional to the number of keys, using sink-based heap construction.
     * Recall that in this implementation, the last item is at N-1, and the
     * parent of that item, the last parent, is at ((N-1) - 1)/D.
     *
     * @param p the 2-power to compute the arity of the d-ary heap
     * @param keys the array of keys
     */
    public PowerOf2HeapMinPQ(int p, Key[] keys) {
        this.p = p;
        N = keys.length;
        pq = (Key[]) new Object[keys.length];
        for (int i = 0; i < N; i++)
            pq[i] = keys[i];
        // Sink from last parent to top. Last parent is ((N-1) - 1) >> p
        for (int k = (N - 2) >> p; k >= 0; k--)
            sink(k);
        assert isMinHeap();
    }

    /**
     * Initializes a d-ary priority queue from the array of keys using the
     * default arity. Like Sedgewick and Wayne's reference binary heap, this
     * d-ary generalization takes time proportional to the number of keys, using
     * sink-based heap construction. Recall that in this implementation, the
     * last item is at N-1, and the parent of that item, the last parent, is at
     * ((N-1) - 1)/2-to-the-p, which equals (N-2)>>p.
     *
     * @param keys the array of keys
     */
    public PowerOf2HeapMinPQ(Key[] keys) {
        this(POWER_DEFAULT, keys);
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
     * Returns the number of keys on the priority queue.
     *
     * @return the number of keys on the priority queue
     */
    public int size() {
        return N;
    }

    /**
     * Returns a largest key on the priority queue.
     *
     * @return a largest key on the priority queue
     * @throws java.util.NoSuchElementException if the priority queue is empty
     */
    public Key min() {
        if (isEmpty()) throw new NoSuchElementException(
                "Priority queue underflow");
        return pq[0];
    }

    // helper function to double the size of the heap array
    private void resize(int capacity) {
        assert capacity >= N;
        Key[] temp = (Key[]) new Object[capacity];
        for (int i = 0; i < N; i++) temp[i] = pq[i];
        pq = temp;
    }

    /**
     * Adds a new key to the priority queue.
     *
     * @param x the new key to add to the priority queue
     */
    public void insert(Key x) {
        // If size is already at capacity, double size of array.
        // Unlike most "2" occurrences, this one didn't have to change to D
        // when changing from the binary case to the d-ary case;
        // two here pertains to array doubling rather than the binary heap.
        // In other words, we must have room for one more:
        // (N+1)<= pq.length or we better resize.
        if (N >= pq.length) resize((pq.length << 1));

        // Add x, make it swim up to preserve heap conditions.
        pq[N] = x;
        swim(N++);
        assert isMinHeap();
    }

    /**
     * Removes and returns a largest key on the priority queue.
     *
     * @return a largest key on the priority queue
     * @throws java.util.NoSuchElementException if priority queue is empty.
     */
    public Key delMin() {
        if (isEmpty()) throw new NoSuchElementException(
                "Priority queue underflow");
        Key min = pq[0];
        // Upon refactoring to inline the swap() helper function, it was
        // determined this swap could be turned into a half swap by commenting
        // out 2 lines:
        // Key swap = pq[0];
        pq[0] = pq[--N];
        // pq[N] = swap;
        sink(0);
        pq[N] = null;     // Avoid loitering and help with garbage collection.
        if (N > 0 && N < (pq.length + 1) >> 2) // Replaced pq.length with             
            resize((pq.length) >> 1); // pq.length+1 in both formulas.
        assert isMinHeap();
        return min;
    }

    /**
     * *********************************************************************
     * Helper functions to restore the heap invariant.
     * ******************************************************************
     */
    // For sink() and swim(), recall the basic facts about a heap of width 
    // 2-to-the-power-p, based at index 0:  the children of an
    // item at index j are from index (j<<p)+1...
    // To (j<<p)+1+(1<<p)-1, which equals (j+1)<<p; inclusive.
    // This is because there are exactly 2-to-the-p or (1<<p) children.
    //  
    // Inversely the parent of an item at k is at (k-1)>>p. 
    private void swim(int child) {
        int parent = (child - 1) >> p;
        while (child > 0 && greater(parent, child)) {
            Key swap = pq[child];
            pq[child] = pq[parent];
            pq[parent] = swap;
            child = parent;
            parent = (parent - 1) >> p;
        }
    }

    // sink(int) took more care than most methods to adapt from the binary case.
    // In the binary case, we only compared the 1st or left child with the 2nd 
    // or right child. Now we compare the 1st child with D-1 sibs.
    private void sink(int parent) {
        int child = (parent << p) + 1;
        while (child < N) {
            for (int sib = child + 1; sib < N && sib <= (parent + 1) << p; sib++)
                if (greater(child, sib)) child = sib;
            if (!greater(parent, child)) break;
            Key swap = pq[parent];
            pq[parent] = pq[child];
            pq[child] = swap;
            parent = child;
            child = (child << p) + 1;
        }
    }

    /**
     * *********************************************************************
     * Helper functions for compares and swaps.
     **********************************************************************
     */
    // Also profiled with return and a conditional expression, but the if ()
    // statement performed better in tests.
    private boolean greater(int i, int j) {
        if (comparator == null)
            // Use < for less() as helper for MaxPQ.
            // Use > for greater() as helper for MinPQ. 
            return ((Comparable<Key>) pq[i]).compareTo(pq[j]) > 0;
        else return comparator.compare(pq[i], pq[j]) > 0;
    }

    // Is pq[0..N-1] a min heap?
    private boolean isMinHeap() {
        return isMinHeap(0);
    }

    // Is subtree of pq[0..N-1] rooted at k a min heap?
    private boolean isMinHeap(int parent) {
        if (parent > N - 1) return true; // Default to true in vacuous case.
        // In binary case, we only had 2 children to worry about.
        // Now we have to consider all 1<<p children of k:
        int child = (parent << p) + 1;
        // If any child of k is in the array and breaks heap order with k,
        // return false. Otherwise return true.
        for (int sib = child; sib < child + (1 << p); sib++)
            if (sib < N && greater(parent, sib) ||
                !isMinHeap(sib))
                return false;
        return true;
    }

    /**
     * *********************************************************************
     * Iterator
     **********************************************************************
     */
    /**
     * Returns an iterator that iterates over the keys on the priority queue in
     * descending order. The iterator doesn't implement <tt>remove()</tt> since
     * it's optional.
     *
     * @return an iterator that iterates over the keys in descending order
     */
    public Iterator<Key> iterator() {
        return new HeapIterator();
    }

    private class HeapIterator implements Iterator<Key> {

        // Create a new priority queue.
        private PowerOf2HeapMinPQ<Key> copy;

        // Add all items to the copy. This takes linear time since items are 
        // already in heap order.

        public HeapIterator() {
            if (comparator == null)
                copy = new PowerOf2HeapMinPQ<Key>(p, size());
            else copy = new PowerOf2HeapMinPQ<Key>(p, size(), comparator);
            for (int i = 0; i <= N - 1; i++)
                copy.insert(pq[i]);
        }

        public boolean hasNext() {
            return !copy.isEmpty();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Key next() {
            if (!hasNext()) throw new NoSuchElementException();
            return copy.delMin();
        }
    }

    // Returns a formatted string representation of the d-ary priority queue.
    private String prettyString() {
        StringBuilder sb = new StringBuilder();
        // To show clearly the "next gen" operation, we write out 0<<p:
        int child = (0 << p) + 1; // First child of next generation.
        sb.append('[');
        for (int i = 0; i < N; i++) {
            sb.append(String.format("%2s", i)).
                append(':').
                append(String.format("%2s", pq[i]));
            if (i != N - 1)
                if (i + 1 != child)
                    sb.append(i % (1 << p) == 0 ? "; " : ", ");
                else {
                    sb.append("]\n[");
                    child = (child << p) + 1;
                }
        }
        sb.append("]\n");
        return sb.toString();
    }

    /**
     * Unit tests the <tt>PowerOf2HeapMinDPQ</tt> data type.
     */
    public static void main(String[] args) {
        final boolean VERBOSE = true;
        final int LG_SIZE = 20;
        final int NUM_TRIALS = 1;

        // 2^^n + 1 makes a good heap size, with 1 spot for root.
        final int TEST_HEAP_SIZE = (1 << LG_SIZE) + 1;
        // Assume language = English, country = US for consistency with StdIn,
        // Sedgewick & Wayne's facade for Java I/O.
        final Locale US_LOCALE = new Locale("en", "US");
        // Force Unicode UTF-8 encoding; otherwise it's system dependent:
        final String CHARSET_NAME = "UTF-8";
        // Input source:
        Scanner scanner;
        scanner = new Scanner(new java.io.BufferedInputStream(System.in),
                              CHARSET_NAME);
        scanner.useLocale(US_LOCALE);

        // send output here
        PrintWriter out;
        try {
            out = new PrintWriter(
                new OutputStreamWriter(System.out, CHARSET_NAME), true);
            out.printf(
                "Sorting %d integers in %d trial(s).\n",
                TEST_HEAP_SIZE,
                NUM_TRIALS);
            for (int power0 = 1; power0 <= 5; power0++) {
                PowerOf2HeapMinPQ<Integer> pq;
                int[] results = new int[TEST_HEAP_SIZE];
                if (VERBOSE) out.printf("Test 2^^%d-heap (%d-heap):\n",
                                        power0,
                                        (1 << power0));
                Integer[] testVect = new Integer[TEST_HEAP_SIZE];
                long startTime = System.nanoTime();
                for (int t = 0; t < NUM_TRIALS; t++) {
                    for (int i = 0; i < TEST_HEAP_SIZE; i++)
                        testVect[i] = i;
                    StdRandom.shuffle(testVect);
                    // out.println(Arrays.deepToString(testVect));
                    pq = new PowerOf2HeapMinPQ<>(power0, testVect);
                    for (int i = TEST_HEAP_SIZE - 1; i >= 0; i--)
                        results[i] = pq.delMin();
                }
                long endTime = System.nanoTime();
                long duration = (endTime - startTime);
                out.printf(
                    "%d-heap took %d ms.\n",
                    (1 << power0),
                    duration / 1_000_000);
            }
            out.flush();
            // If args non-empty, parse lg(arity). Otherwise use default.
            int power1 = (args.length > 0) ?
                         Integer.parseInt(args[0]) :
                         POWER_DEFAULT;
            PowerOf2HeapMinPQ<String> dpqs = new PowerOf2HeapMinPQ<String>(
                power1);
            if (VERBOSE) {
                out.printf("Test min %d-ary priority queue of strings:\n",
                           (1 << power1));
                out.print("Enter items one at a time. \n");
                out.print("Special characters:\n");
                out.print("-       Remove item.\n");
                out.print("?, =    Output queue and exit.\n");
                out.print("..      Exit.\n");
                out.flush();
            }
INPUT_LOOP:
            while (scanner.hasNext()) {
                String input = scanner.next();
                String output;
                switch (input) {
                    case "?":
                    case "=":
                        StringBuilder tb = new StringBuilder();
                        tb.append("Queue:\n[");
                        for (String s : dpqs)
                            tb = tb.append(s.concat(", "));
                        tb = tb.delete(tb.length() - 2, tb.length()).
                            append("]");
                        out.print(tb + "\n");
                        break INPUT_LOOP;
                    case ".":
                    case "-":
                        output = dpqs.delMin();
                        if (VERBOSE)
                            out.print(output + "\n");
                        break;
                    case "..":
                        break INPUT_LOOP;
                    default:
                        dpqs.insert(input);
                        break;
                }
            }
            out.print("(" + dpqs.size() + " left on queue.)\n");
            out.flush();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
        }
    }
}

/*
 * Copyright 2014, 2015, Dean Elzinga
 *
 * Inspired by study of the book Algorithms, by taking the Princeton University 
 * Algorithms I and II courses through Coursera, and by Sedgewick's & Wayne's 
 * book site: 
 * Algorithms, 4th edition, by Robert Sedgewick and Kevin Wayne, 
 * Addison-Wesley Professional, 2011, ISBN 0-321-57351-X. 
 * http://algs4.cs.princeton.edu
 */
