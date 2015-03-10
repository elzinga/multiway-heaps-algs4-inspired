
import edu.princeton.cs.introcs.StdIn;
import edu.princeton.cs.introcs.StdOut;
import edu.princeton.cs.introcs.StdRandom;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * The <tt>MultiwayHeapMinPQ</tt> class represents a priority queue of generic
 * keys. It supports usual <em>insert</em> and <em>delete-minimum</em>
 operations. It is based heavily on ideas from Sedgewick's and Wayne's
 implementation of MaxPQ. Like that implementation, MultiwayHeapMinPQ also
 provides methods for peeking at the minimum key, testing if the priority
 queue is empty, and iterating through the keys.
 <p>
 * This implementation uses a d-way heap. Like the binary case this is patterned
 * after, the <em>insert</em> and <em>delete-the-minimum</em> operations take
 * logarithmic amortized time. The <em>min</em>, <em>size</em>, and
 * <em>is-empty</em> operations take constant time. Construction takes time
 * proportional to the specified capacity or the number of items used to
 * initialize the data structure.
 * <p>
 * For additional documentation Sedgewick & Wayne's original MaxPQ
 * implementation, see
 * <a href="http://algs4.cs.princeton.edu/24pq">Section 2.4</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Dean Elzinga
 */
public class MultiwayHeapMinPQ<Key> implements Iterable<Key> {
    private final int width;                 // Width  of the d-way heap.

    /**
     * Default width for d-way heap. 
     */
    static public final int WIDTH_DEFAULT = 6;    
    private Key[] heap;                    // Store items at indices 0 to N-1.
    private int N;                       // Number of items on d-way queue.
    private Comparator<Key> comparator;  // Optional Comparator.

    /**
     * Initializes an empty priority queue with the given initial capacity.
     *
     * @param width the width of the d-way heap
     * @param initCapacity the initial capacity of the priority queue
     */
    public MultiwayHeapMinPQ(int width, int initCapacity) {
        this.width = width;
        heap = (Key[]) new Object[initCapacity];
        N = 0;
    }

    /**
     * Initializes an empty priority queue using d-way heap.
     *
     * @param width the width of the d-way heap
     */
    public MultiwayHeapMinPQ(int width) {
        this(width, 1);
    }

    /**
     * Initializes an empty d-way priority queue using the default width.
     */
    public MultiwayHeapMinPQ() {
        this(WIDTH_DEFAULT, 1);
    }

    /**
     * Initializes an empty d-way priority queue with the given initial 
     * capacity, using the given comparator.
     *
     * @param width the width of the d-way heap
     * @param initCapacity the initial capacity of the priority queue
     * @param comparator the order in which to compare the keys
     */
    public MultiwayHeapMinPQ(int width, int initCapacity, Comparator<Key> comparator) {
        this.width = width;
        this.comparator = comparator;
        heap = (Key[]) new Object[initCapacity];
        N = 0;
    }

    /**
     * Initializes an empty d-way priority queue using the given comparator.
     *
     * @param width the width of the d-way heap
     * @param comparator the order in which to compare the keys
     */
    public MultiwayHeapMinPQ(int width, Comparator<Key> comparator) {
        this(width, 1, comparator);
    }

    /**
     * Initializes an empty d-way priority queue using the given comparator and
     * the default width.
     *
     * @param comparator the order in which to compare the keys
     */
    public MultiwayHeapMinPQ(Comparator<Key> comparator) {
        this(WIDTH_DEFAULT, 1, comparator);
    }

    /**
     * Initializes a d-way priority queue from the array of keys. Like Sedgewick
     * and Wayne's reference binary heap, this d-way generalization takes time
     * proportional to the number of keys, using sink-based heap construction.
     * Recall that in this implementation, the last item is at N-1, and the
     * parent of that item, the last parent, is at ((N-1) - 1)/D.
     *
     * @param width the width of the d-way heap
     * @param keys the array of keys
     */
    public MultiwayHeapMinPQ(int width, Key[] keys) {
        this.width = width;
        N = keys.length;
        heap = (Key[]) new Object[keys.length];
        for (int i = 0; i < N; i++)
            heap[i] = keys[i];
        for (int k = ((N-1) - 1) / width; k >= 0; k--)
            sink(k); // Sink from last parent to top.
        assert isMinHeap();
    }

    /**
     * Initializes a d-way priority queue from the array of keys using the
     * default width. Like Sedgewick and Wayne's reference binary heap, this
     * d-way generalization takes time proportional to the number of keys, using
     * sink-based heap construction. Recall that in this implementation, the
     * last item is at N-1, and the parent of that item, the last parent, is at
     * ((N-1) - 1)/D.
     *
     * @param keys the array of keys
     */
    public MultiwayHeapMinPQ(Key[] keys) {
        this(WIDTH_DEFAULT, keys);
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
     * Returns a lowest key on the priority queue.
     *
     * @return a lowest key on the priority queue
     * @throws java.util.NoSuchElementException if the priority queue is empty
     */
    public Key min() {
        if (isEmpty()) throw new NoSuchElementException(
                    "Priority queue underflow");
        return heap[0];
    }

    // helper function to double the size of the heap array
    private void resize(int capacity) {
        assert capacity > N;
        Key[] temp = (Key[]) new Object[capacity];
        for (int i = 0; i < N; i++) temp[i] = heap[i];
        heap = temp;
    }

    /**
     * Adds a new key to the priority queue.
     *
     * @param x the new key to add to the priority queue
     */
    public void insert(Key x) {
        // If size is already at capacity, double size of array.
        // Unlike most "2" occurrences, this one didn't have to change to D
        // when changing from the binary case to the D-way case;
        // 2 here pertains to array doubling rather than the binary heap.
        // In other words, we must have room for one more:
        // (N+1)<= pq.length or we better resize.
        if (N + 1 > heap.length) resize(2 * heap.length);

        // Add x, make it swim up to preserve heap conditions.
        heap[N] = x;
        swim(N);
        N++;
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
        Key min = heap[0];
        swap(0, N - 1);
        N--;
        sink(0);
        heap[N] = null;     // Avoid loiterig and help with garbage collection.
        if ((N > 0) && (N <= heap.length / 4)) // Replaced pq.length with             
            resize((heap.length + 1) / 2);     // pq.length+1 in both formulas.
        assert isMinHeap();
        return min;
    }

    /*
     * Helper functions to restore the heap invariant.
     */
    private void swim(int k) {
        while (k > 0 && greater((k - 1) / width, k)) {
            swap(k, (k - 1) / width);
            k = (k - 1) / width;
        }
    }

    /**
     * Moves the key at index k in the heap array "downward" to its proper
     * place in the heap hierarchy by swapping with 
     * sink(int) took more care than most methods to adapt from the binary case.
     * In the binary case, we only compared 1 sib with the first or left
     * left child. Now we have to compare D-1 sibs with the first child of k. */    
    private void sink(int k) {
        while (k*width + 1 <= N - 1) {  // While k has a child in the array,
            int first = k*width + 1;    // j gets the first child of k,
            int last = first + width - 1;
            last = (last <= N - 1) ? last : N - 1; // "min" idiom
            // Idiom to find the max
            for (int sibling = first + 1; sibling <= last; sibling++) 
                if (greater(first, sibling)) 
                    first = sibling;
            if (!greater(k, first)) // 
                break;
            swap(k, first);
            k = first;
        }
    }

    /*
     * Helper functions for compares and swaps.
     */
    private boolean greater(int i, int j) {
        if (comparator == null) {
            // For Min heap, use greater() and > operator.
            // For Max heap, use less() and < operator.
            return ((Comparable<Key>) heap[i]).compareTo(heap[j]) > 0;
        } else {
            return comparator.compare(heap[i], heap[j]) > 0;
        }
    }

    private void swap(int i, int j) {
        Key swap = heap[i];
        heap[i] = heap[j];
        heap[j] = swap;
    }

    // Is pq[0..N-1] a min heap?
    private boolean isMinHeap() {
        return isMinHeap(0);
    }

    // Is subtree of pq[0..N-1] rooted at k a min heap?
    private boolean isMinHeap(int k) {
        if (k > N - 1) return true; // Default to true in vacuous case.

        /* In binary case, we only had 2 children to worry about.
         * Now we have to consider all D children of k:
         */
        int firstChild = width * k + 1;

        /* If any child of k is in the array and breaks heap order with k,
         * return false. Otherwise return true.
         */
        for (int i = 0; i < width; i++) {
            if (firstChild + i <= N - 1 && greater(k, firstChild + i))
                return false;
            if (!isMinHeap(firstChild + i))
                return false;
        }
        return true;
    }

    /*
     * ITERATOR SECTION 
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
        private MultiwayHeapMinPQ<Key> copy;

        /* Add all items to the copy. This takes linear time since items are 
           already in heap order. */
        public HeapIterator() {
            if (comparator == null) copy = new MultiwayHeapMinPQ<Key>(width, size());
            else copy = new MultiwayHeapMinPQ<Key>(width, size(), comparator);
            for (int i = 0; i <= N - 1; i++)
                copy.insert(heap[i]);
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

    // Returns a formatted string representation of the d-way priority queue.
    private String prettyString() {
        StringBuilder sb = new StringBuilder();
        int nextGenFirstChild = 0*width + 1; // First child of next generation.
        sb.append('[');
        for (int i = 0; i < N; i++) {
            sb.append(String.format("%2s", i)).
                append(':').
                append(String.format("%2s", heap[i]));
            if (i != N - 1) {
                if (i + 1 != nextGenFirstChild) {
                    sb.append(i % width == 0 ? "| " : ", ");
                } else {
                    sb.append("]\n[");
                    nextGenFirstChild = nextGenFirstChild * width + 1;
                }
            }
        }
        sb.append("]\n");
        return sb.toString();
    }

    /**
     * Unit tests the <tt>MultiwayHeapMinPQ</tt> data type.
     */
    public static void main(String[] args) {
        final boolean VERBOSE = true;
        final int LG_SIZE = 20;
        // 2^^n + 1 makes a good heap size, with 1 spot for root.
        final int TEST_HEAP_SIZE = (1 << LG_SIZE) + 1;
        final int NUM_TRIALS = 1;
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

            for (int width = 2; width <= 16; width++) {
                MultiwayHeapMinPQ<Integer> pq;
                if (VERBOSE) StdOut.printf("Test %d-way heap:\n", width);
                Integer[] testArray = new Integer[TEST_HEAP_SIZE];
                int[] results = new int[TEST_HEAP_SIZE];
                long startTime = System.nanoTime();
                for (int t = 0; t < NUM_TRIALS; t++) {
                    for (int i = 0; i < TEST_HEAP_SIZE; i++) {
                        testArray[i] = i;
                    }
                    StdRandom.shuffle(testArray);
                    //StdOut.println(Arrays.deepToString(testArray));
                    pq = new MultiwayHeapMinPQ<>(width, testArray);
                    //StdOut.println(dpqi.prettyString());
                    for (int i = TEST_HEAP_SIZE - 1; i >= 0; i--) {
                        results[i] = pq.delMin();
                    }
                }
                long endTime = System.nanoTime();
                long duration = (endTime - startTime);
                out.printf(
                    "%d-heap took %d ms.\n",
                    (width),
                    duration / 1_000_000);
            }
            out.flush();
            int d = (args.length > 0) ?
                    Integer.parseInt(args[0]) :
                    WIDTH_DEFAULT;
            MultiwayHeapMinPQ<String> dpqs = new MultiwayHeapMinPQ<String>(d);
            if (VERBOSE) {
                StdOut.printf("Test min %d-way priority queue of strings:\n", d);
                StdOut.print("Enter items one at a time. \n");
                StdOut.print("Special characters:\n");
                StdOut.print("-       Remove item.\n");
                StdOut.print("?, =    Output queue and exit.\n");
                StdOut.print("..      Exit.\n");
            }
INPUT_LOOP:
            while (!StdIn.isEmpty()) {
                String input = StdIn.readString();
                String output;
                switch (input) {
                    case "?":
                    case "=":
                        StringBuilder tb = new StringBuilder();
                        tb.append("Queue:\n[");
                        for (String s : dpqs) {
                            tb = tb.append(s.concat(", "));
                        }
                        tb = tb.delete(tb.length() - 2, tb.length()).
                            append("]");
                        StdOut.print(tb + "\n");
                        break INPUT_LOOP;
                    case "":
                    case "-":
                        output = dpqs.delMin();
                        if (VERBOSE) {
                            StdOut.print(output + "\n");
                        }
                        break;
                    case "..":
                        break INPUT_LOOP;
                    default:
                        dpqs.insert(input);
                        break;
                }
            }
            StdOut.print("(" + dpqs.size() + " left on queue.)\n");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
        }
    }
}

/*
 * Copyright 2014, Dean Elzinga
 *
 * Inspired by study of the book, Coursera course, and book site: 
 * Algorithms, 4th edition, by Robert Sedgewick and Kevin Wayne, 
 * Addison-Wesley Professional, 2011, ISBN 0-321-57351-X. 
 * http://algs4.cs.princeton.edu
 */