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

/* Uses default package, like the rest of Algorithms 4 course library, to
 * facilitate automated profiling and grading.
 */

// Imports essential to the implementation
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

// Imports for testing in main() method only
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Scanner;
import edu.princeton.cs.introcs.StdIn;
import edu.princeton.cs.introcs.StdOut;
import edu.princeton.cs.introcs.StdRandom;

/*
 * Compilation: javac MultiwayHeapMaxPQ.java
 * Execution: java MultiwayHeapMaxPQ < input.txt
 */

/**
 * The <tt>MultiwayHeapMaxPQ</tt> class implements a priority queue of generic
 * keys. It supports typical <em>insert</em> and <em>delete-maximum</em>
 * operations. It is based on ideas from Sedgewick's and Wayne's
 * Algorithms textbook implementation of MaxPQ. Like that implementation,
 * MultiwayHeapMaxPQ also provides methods for peeking at the maximum key,
 * testing if the priority queue is empty, and iterating through the keys.
 * <p>
 * This implementation uses a <em>d</em>-ary heap. Like the binary case, which
 * inspired this code, the <em>insert</em> and <em>delete-the-maximum</em>
 * operations take logarithmic amortized time. The <em>max</em>, <em>size</em>,
 * and <em>is-empty</em> operations take constant time. Construction takes time
 * proportional to the size (capacity or items used to initialize).
 * <p>
 * Style mimics Sedgewick & Wayne's Algorithms course library style: indentation
 * is 4 spaces, braces are omitted for single-statement blocks.
 * Note: Sedgewick and Wayne's course library avoids interfaces and uses
 * a very "flat" inheritance structure, I believe to keep it simple to teach.
 * If we were looking to refactor towards a more hierarchical inheritance
 * structure for improved Separation of Concerns, we could extract a MaxPQ and
 * MinPQ interface and implement them with classes MaxHeap, MinHeap,
 * MultiwayMaxHeap, and MultiwayMinHeap. Advantage: that approach separates the
 * priority queue API from the heap implementation,
 * whether it be a binary heap or multiway heap.
 * <p>
 * For additional documentation on Sedgewick & Wayne's original MaxPQ
 * implementation, see
 * <a href="http://algs4.cs.princeton.edu/24pq">Section 2.4</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Dean Elzinga
 */
public class MultiwayHeapMaxPQ<Key> implements Iterable<Key> {
    private final int width;                 // Width  of the d-way heap.

    /**
     * Default width D for d-way heap. D values of 5, 6, or 7 have shown
     * superior performance to MaxPQ and MinPQ binary heap in testing with heap
     * sort. Algorithms with different ratios of insert, delete operations are
     * expected to differ slightly in performance profiles. The maximum
     * execution time advantage observed over binary sort in testing so far was
     * greater than 30% but less than 50%.
     */
    static public final int DEFAULT_WIDTH = 6;
    private Key[] heap;                  // Key references at indices 0 to N-1.
    private int N;                       // Number of items on d-way queue.
    private Comparator<Key> comparator;  // Optional Comparator.

    /**
     * Initializes an empty priority queue with the given initial capacity.
     *
     * @param D the width of the d-way heap
     * @param initCapacity the initial capacity of the priority queue
     */
    public MultiwayHeapMaxPQ(int D, int initCapacity) {
        this.width = D;
        heap = (Key[]) new Object[initCapacity];
        N = 0;
    }

    /**
     * Initializes an empty priority queue using d-way heap.
     *
     * @param D the width of the d-way heap
     */
    public MultiwayHeapMaxPQ(int D) {
        this(D, 1);
    }

    /**
     * Initializes an empty d-way priority queue using the default width.
     */
    public MultiwayHeapMaxPQ() {
        this(DEFAULT_WIDTH, 1);
    }

    /**
     * Initializes an empty d-way priority queue with the given initial
     * capacity, using the given comparator.
     *
     * @param D the width of the d-way heap
     * @param initCapacity the initial capacity of the priority queue
     * @param comparator the order in which to compare the keys
     */
    public MultiwayHeapMaxPQ(int D, int initCapacity, Comparator<Key> comparator) {
        this.width = D;
        this.comparator = comparator;
        heap = (Key[]) new Object[initCapacity];
        N = 0;
    }

    /**
     * Initializes an empty d-way priority queue using the given comparator.
     *
     * @param D the width of the d-way heap
     * @param comparator the order in which to compare the keys
     */
    public MultiwayHeapMaxPQ(int D, Comparator<Key> comparator) {
        this(D, 1, comparator);
    }

    /**
     * Initializes an empty d-way priority queue using the given comparator and
     * the default width.
     *
     * @param comparator the order in which to compare the keys
     */
    public MultiwayHeapMaxPQ(Comparator<Key> comparator) {
        this(DEFAULT_WIDTH, 1, comparator);
    }

    /**
     * Initializes a d-way priority queue from the array of keys. Like Sedgewick
     * and Wayne's reference binary heap, this d-way generalization takes time
     * proportional to the number of keys, using sink-based heap construction.
     * Recall that in this implementation, the last item is at N-1, and the
     * parent of that item, the last parent, is at ((N-1) - 1)/D.
     *
     * @param D the width of the d-way heap
     * @param keys the array of keys
     */
    public MultiwayHeapMaxPQ(int D, Key[] keys) {
        this.width = D;
        N = keys.length;
        heap = (Key[]) new Object[keys.length];
        for (int i = 0; i < N; i++)
            heap[i] = keys[i];
        for (int k = ((N - 1) - 1) / D; k >= 0; k--)
            sink(k); // Sink from last parent to top.
        assert isMaxHeap();
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
    public MultiwayHeapMaxPQ(Key[] keys) {
        this(DEFAULT_WIDTH, keys);
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
    public Key max() {
        if (isEmpty())
            throw new NoSuchElementException(
                "Priority queue underflow");
        return heap[0];
    }

    // helper function to double the size of the heap array
    private void resize(int capacity) {
        assert capacity > N;
        Key[] temp = (Key[]) new Object[capacity];
        for (int i = 0; i < N; i++)
            temp[i] = heap[i];
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
        // when changing from the binary case to the d-way case;
        // two here pertains to array doubling rather than the binary heap.
        // In other words, we must have room for one more:
        // (N+1)<= pq.length or we better resize.
        if (N + 1 > heap.length)
            resize(2 * heap.length);

        // Add x, make it swim up to preserve heap conditions.
        heap[N] = x;
        swim(N);
        N++;
        assert isMaxHeap();
    }

    /**
     * Removes and returns a largest key on the priority queue.
     *
     * @return a largest key on the priority queue
     * @throws java.util.NoSuchElementException if priority queue is empty.
     */
    public Key delMax() {
        if (isEmpty())
            throw new NoSuchElementException(
                "Priority queue underflow");
        Key max = heap[0];
        swap(0, N - 1);
        N--;
        sink(0);
        heap[N] = null;     // Avoid loiterig and help with garbage collection.
        if ((N > 0) && (N <= heap.length / 4)) // Replaced pq.length with

            resize((heap.length + 1) / 2); // pq.length+1 in both formulas.
        assert isMaxHeap();
        return max;
    }

    /*
     * Helper functions to restore the heap invariant.
     */
    private void swim(int k) {
        while (k > 0 && less((k - 1) / width, k)) {
            swap(k, (k - 1) / width);
            k = (k - 1) / width;
        }
    }

    /* sink(int) took more care than most methods to adapt from the binary case.
     * In the binary case, we only compared 1 sib with the first or left
     * left child. Now we have to compare D-1 sibs with the first child of k.
     */
    private void sink(int k) {
        while (k * width + 1 <= N - 1) {  // While k has a child in the array,
            int first = k * width + 1;    // j gets the first child of k.
            int last = first + width - 1; // Find last child of k.
            last = (last <= N - 1) ?      // Idiom for "min".
                   last : N - 1;
            // Find the max sibling. If it's not greater than the item at k,
            // break otherwise.
            for (int sibling = first + 1; sibling <= last; sibling++)
                if (less(first, sibling))
                    first = sibling;
            if (!less(k, first))
                break;
            swap(k, first);
            k = first;
        }
    }

    /*
     * Helper functions for compares and swaps.
     */
    private boolean less(int i, int j) {
        if (comparator == null)
            return ((Comparable<Key>) heap[i]).compareTo(heap[j]) < 0;
        else
            return comparator.compare(heap[i], heap[j]) < 0;
    }

    private void swap(int i, int j) {
        Key swap = heap[i];
        heap[i] = heap[j];
        heap[j] = swap;
    }

    /* Tests the main heap invariant: is pq[0..N-1] a max heap?
     */
    private boolean isMaxHeap() {
        return isMaxHeap(0);
    }

    // Is subtree of pq[0..N-1] rooted at k a max heap?
    private boolean isMaxHeap(int k) {
        if (k > N - 1)
            return true; // Default to true in vacuous case.        
        // In binary case, we only had 2 children to worry about.
        // Now we have to consider all D children of k:
        int firstChild = width * k + 1;
        // If any child of k is in the array and breaks heap order with k,
        // return false. Otherwise return true.
        for (int i = 0; i < width; i++) {
            if (firstChild + i <= N - 1 && less(k, firstChild + i))
                return false;
            if (!isMaxHeap(firstChild + i))
                return false;
        }
        return true;
    }

    /* ITERATOR SECTION
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
        private MultiwayHeapMaxPQ<Key> copy;

        // Add all items to the copy.
        // This takes linear time since items are already in heap order.
        public HeapIterator() {
            if (comparator == null)
                copy = new MultiwayHeapMaxPQ<Key>(width, size());
            else
                copy = new MultiwayHeapMaxPQ<Key>(width, size(), comparator);
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
            if (!hasNext())
                throw new NoSuchElementException();
            return copy.delMax();
        }
    }

    /**
     * Returns a formatted string representation of the d-way priority queue,
     * to aid in testing and debugging.
     */
    private String prettyString() {
        StringBuilder sb = new StringBuilder();
        int nextGenFirstChild = 0 * width + 1; // First child of next generation.
        sb.append('[');
        for (int i = 0; i < N; i++) {
            sb.append(String.format("%2s", i)).
                append(':').
                append(String.format("%2s", heap[i]));
            if (i != N - 1)
                if (i + 1 != nextGenFirstChild)
                    sb.append(i % width == 0 ? "| " : ", ");
                else {
                    sb.append("]\n[");
                    nextGenFirstChild = nextGenFirstChild * width + 1;
                }
        }
        sb.append("]\n");
        return sb.toString();
    }

    /**
     * Unit tests the <tt>MultiwayHeapMaxPQ</tt> data type.
     */
    public static void main(String[] args) {
        final boolean VERBOSE = true;
        final int LG_SIZE = 16;
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

            for (int width = 1; width <= 8; width++) {
                MultiwayHeapMaxPQ<Integer> pq;
                if (VERBOSE)
                    StdOut.printf("Test %d-way heap:\n", width);
                Integer[] testArray = new Integer[TEST_HEAP_SIZE];
                int[] results = new int[TEST_HEAP_SIZE];
                long startTime = System.nanoTime();
                for (int t = 0; t < NUM_TRIALS; t++) {
                    for (int i = 0; i < TEST_HEAP_SIZE; i++)
                        testArray[i] = i;
                    StdRandom.shuffle(testArray);
                    //StdOut.println(Arrays.deepToString(testArray));
                    pq = new MultiwayHeapMaxPQ<>(width, testArray);
                    //StdOut.println(dpqi.prettyString());
                    for (int i = TEST_HEAP_SIZE - 1; i >= 0; i--)
                        results[i] = pq.delMax();
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
                    DEFAULT_WIDTH;
            MultiwayHeapMaxPQ<String> dpqs = new MultiwayHeapMaxPQ<String>(d);
            if (VERBOSE) {
                StdOut.printf("Test max %d-way priority queue of strings:\n", d);
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
                        for (String s : dpqs)
                            tb = tb.append(s.concat(", "));
                        tb = tb.delete(tb.length() - 2, tb.length()).
                            append("]");
                        StdOut.print(tb + "\n");
                        break INPUT_LOOP;
                    case "":
                    case "-":
                        output = dpqs.delMax();
                        if (VERBOSE)
                            StdOut.print(output + "\n");
                        break;
                    case "..":
                        break INPUT_LOOP;
                    default:
                        dpqs.insert(input);
                        break;
                }
            }
            StdOut.print("(" + dpqs.size() + " items.)\n");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
        }
    }
}

/*
 * Copyright 2014, 2015, Dean Elzinga
 *
 * Inspired by study of the book, Coursera course, and book site:
 * Algorithms, 4th edition, by Robert Sedgewick and Kevin Wayne,
 * Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 * http://algs4.cs.princeton.edu
 */
