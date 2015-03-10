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


// Imports essential to the implementation.
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

// Imports for testing in main() method only.
import edu.princeton.cs.introcs.StdRandom;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Scanner;

/*
 * Compilation: javac PowerOf2HeapMaxPQ.java
 * Execution: java PowerOf2HeapMaxPQ < input.txt
 *
 * Generic max priority queue implementation with a d-ary heap. This variation
 * specifically supports d equal to a power of 2 (or 1<<p) to optimize
 * multiplication
 * by d as leftward bit shift (<< p) and division by d as rightward bit shift
 * (>> p).
 *
 * This implementation works even for p==0, which makes a unary heap,
 * approximately equivalent to insertion sort stored in an array list. Because
 * of the O(N) copying required for each sink() and swim() operation when
 * inserting into a sorted array, the p==0 case is not very useful in practice.
 * But it makes a neat edge case, and it's somewhat interesting that it works at
 * all, albeit at O(N^^2) when performing tasks requiring O(N) operations.
 *
 * Notation: We write 2^^p for "2 to the power p". This avoids confusion with
 * 2^p which means "2 bitwise-XOR with p" in Java, and 2**p, which is awkward
 * notation for programmers steeped in C pointers. Using Java bit-shift
 * operations, raising 2 to the power p is (1<<p). Multiplying k times 2 to the
 * p is (k << p). So the information notation 2^^p is written in Java as (1<<p).
 *
 * As in Robert Sedgewick & Kevin Wayne's binary heap implementation, this can
 * be used with a comparator instead of the natural order, but the generic Key
 * type must still be Comparable.
 *
 * We go back to zero-based array, because Sedgewick and Wayne's elegant
 * one-based array simplification for parent-child calculations doesn't provide
 * quite the same stark elegance in the generalized d-ary case, where we're
 * multiplying and dividing by 2^^p, as in the textbook implementation where
 * they are multiplying and dividing by 2.
 *
 * One elegant nugget remains. Inspired by Sedgewick and Wayne, we do use simple
 * division or multiplication of an index (plus or minus 1) by the width D, or
 * in this power-of-2 case, bit-shift operations by p, to find the index of its
 * parent or child in in the flat array we use for our heap store.
 *
 * About that plus or minus one... Which is it? For a fully packed zero-based
 * array for our heap, use these concise and appealing facts:
 *
 * *Fact 1:* The parent of the item at index i is at index (i-1)>>p.
 *
 * *Fact 2:* Inversely, the first child of the item at index j is found at
 * (j<<p)+1 (if this is in range).
 *
 * *Fact 3:* This first child has ((1<<p)-1) other subsequent siblings, in the
 * range [((j<<p)+1 + 1) ... ((j<<p)+1 + ((1<<p)-1))]. In a (1<<p)-ary heap,
 * the sink operation must exchange with the max of all D siblings at each
 * depth, one of the main tradeoffs for decreasing the depth of the heap. This
 * is the primary difference between this implementation and Sedgewick & Wayne's
 * inspiration: here when we sink a key, to maintain the heap invariant we must
 * execute a for-loop to find the max of the children.
 *
 * *Fact 4:* The last item is at N-1, and so the parent of the last item, the
 * last parent, is at ((N-1)-1)>>p, equal to (N-2)>>p.
 *
 * Note: We *could* still start the heap at array index 1, but then we'd have
 * to use for parent index, (i+((1<<p)-1))>>p, equal to ((i-1)>>p) + 1, neither
 * of which is as pretty. We gain little in the d-ary case by starting at 1.
 * (Kevin Wayne tells me in email that starting with the root element at
 * location D-1 helps with caching or something, especially in the D == 1<<p case.
 * But he isn't sure, and I think it's just for the elegant beauty of the binary
 * case. If you know the answer, please email me.
 *
 * ILLUSTRATIVE INDICES AND THEIR D-ARY PARENTS
 * Index:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 ...
 * 2-ary:  -  0  0  1  1  2  2  3  3  4  4  5  5  6  6  7  7  8  8  9  9 10 ...
 * 4-ary:  -  0  0  0  0  1  1  1  1  2  2  2  2  3  3  3  3  4  4  4  4  5 ...
 * 8-ary:  -  0  0  0  0  0  0  0  0  1  1  1  1  1  1  1  1  2  2  2  2  2 ...
 *
 * EXAMPLES
 * 2-ary parent of item 14 is at (14-1)>>1 == 6.
 * 4-ary parent of item 17 is at (17-1)>>2 == 4.
 * 8-ary parent of item 21 is at (21-1)>>3 == 2.
 * These all match with Fact 1.
 *
 * Wayne and Sedgewick point out this can be optimized by replacing full
 * exchanges with half exchanges (ala insertion sort).
 *
 */
/**
 * The <tt>PowerOf2HeapMaxPQ</tt> class represents a priority queue of generic
 * keys. It supports usual <em>insert</em> and <em>delete-maximum</em>
 * operations. It is based heavily on ideas from Sedgewick's and Wayne's
 * implementation of MaxPQ. Like that implementation, MaxDPQ also provides
 * methods for peeking at the maximum key, testing if the priority queue is
 * empty, and iterating through the keys.
 * <p>
 * This implementation uses a d-ary heap. Like the binary case this is patterned
 * after, the <em>insert</em> and <em>delete-the-maximum</em> operations take
 * logarithmic amortized time. The <em>max</em>, <em>size</em>, and
 * <em>is-empty</em> operations take constant time. Construction takes time
 * proportional to the specified capacity or the number of items used to
 * initialize the data structure.
 * <p>
 * For additional documentation, see
 * <a href="http://algs4.cs.princeton.edu/24pq">Section 2.4</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Dean Elzinga
 */
public class PowerOf2HeapMaxPQ<Key> implements Iterable<Key> {

    /**
     * Default width for 2^^p-wide heap. At time of writing, p is 3, an 8-wide
     * heap, namely a heap where the root node and every other parent node have
     * 8 child nodes, if they are in the heap index range, [0..N-1]; more
     * briefly called an 8-heap.
     */
    private final int p;
    static public final int POWER_DEFAULT = 3;
    private Key[] heap;                  // Store items at indices 0 to N-1.
    private int N;                       // Number of items on d-ary queue.
    private Comparator<Key> comparator;  // Optional Comparator.

    /**
     * Initializes an empty priority queue with the given initial capacity.
     *
     * @param p the power of 2 that is the width of the d-ary heap
     * @param initCapacity the initial capacity of the priority queue
     */
    public PowerOf2HeapMaxPQ(int p, int initCapacity) {
        assert 0 <= p && p <= Integer.SIZE - 1;
        this.p = p;
        heap = (Key[]) new Object[initCapacity];
        N = 0;
    }

    /**
     * Initializes an empty priority queue using d-ary heap.
     *
     * p the power of 2 which is the width of the _d_-ary heap.
     */
    public PowerOf2HeapMaxPQ(int p) {
        this(p, 1);
    }

    /**
     * Initializes an empty d-ary priority queue using the default width.
     */
    public PowerOf2HeapMaxPQ() {
        this(POWER_DEFAULT, 1);
    }

    /**
     * Initializes an empty d-ary priority queue with the given initial
     * capacity, using the given comparator.
     *
     * @param p the power of 2 that is the width of this d-ary heap
     * @param initCapacity the initial capacity of the priority queue
     * @param comparator the order in which to compare the keys
     */
    public PowerOf2HeapMaxPQ(int p,
                             int initCapacity,
                             Comparator<Key> comparator) {
        assert 0 <= p && p <= Integer.SIZE - 1;
        this.p = p;
        this.comparator = comparator;
        heap = (Key[]) new Object[initCapacity];
        N = 0;
    }

    /**
     * Initializes an empty 2^^p-wide heap using the given comparator.
     *
     * @param p the power of 2 that is the width of this d-ary heap
     * @param comparator the order by which to compare the keys
     */
    public PowerOf2HeapMaxPQ(int p, Comparator<Key> comparator) {
        this(p, 1, comparator);
    }

    /**
     * Initializes an empty d-ary priority queue using the given comparator and
     * the default 2-power width.
     *
     * @param comparator the order by which to compare the keys
     */
    public PowerOf2HeapMaxPQ(Comparator<Key> comparator) {
        this(POWER_DEFAULT, 1, comparator);
    }

    /**
     * Initializes a d-ary priority queue from the array of keys. Like Sedgewick
     * and Wayne's reference binary heap, this d-ary generalization takes time
     * proportional to the number of keys, using sink-based heap construction.
     * Recall that in this implementation, the last item is at N-1, and the
     * parent of that item, the last parent, is at ((N-1) - 1)/D.
     *
     * @param p the power of 2 that is the width of this d-ary heap
     * @param keys the array of keys
     */
    public PowerOf2HeapMaxPQ(int p, Key[] keys) {
        this.p = p;
        N = keys.length;
        heap = (Key[]) new Object[keys.length];
        for (int i = 0; i < N; i++)
            heap[i] = keys[i];

        // Sink from last parent to top. Last parent is ((N-1) - 1) >> p
        for (int k = (N - 2) >> p; k >= 0; k--)
            sink(k);
        assert isMaxHeap();
    }

    /**
     * Initializes a d-ary priority queue from the array of keys using the
     * default width. Like Sedgewick and Wayne's reference binary heap, this
     * d-ary generalization takes time proportional to the number of keys, using
     * sink-based heap construction. Recall that in this implementation, the
     * last item is at N-1, and the parent of that item, the last parent, is at
     * ((N-1) - 1)/width, which equals (N-2)>>POWER_DEFAULT .
     *
     * @param keys the array of keys
     */
    public PowerOf2HeapMaxPQ(Key[] keys) {
        this(POWER_DEFAULT, keys);
    }

    /**
     * Is the priority queue empty?
     *
     * @return true if the priority queue is empty; false otherwise.
     */
    public boolean isEmpty() {
        return N == 0;
    }

    /**
     * Returns the number of keys on the priority queue.
     *
     * @return the number of keys on the priority queue.
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
            throw new NoSuchElementException("Priority queue underflow");
        return heap[0];
    }

    // helper function to double the size of the heap array
    private void resize(int capacity) {
        assert capacity >= N;
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
        // Unlike most "2" occurrences, this one didn't have to change to p
        // when changing from the binary case to the d-ary case;
        // two here pertains to array doubling rather than the binary heap.
        // In other words, we must have room for one more:
        // (N+1)<= pq.length or we better resize.
        if (N >= heap.length) resize((heap.length << 1));

        // Add x, make it swim up to preserve heap conditions.
        heap[N] = x;
        swim(N++);
        assert isMaxHeap();
    }

    /**
     * Removes and returns a largest key on the priority queue.
     *
     * @return a largest key on the priority queue
     * @throws java.util.NoSuchElementException if priority queue is empty.
     */
    public Key delMax() {
        if (isEmpty()) throw new NoSuchElementException(
                "Priority queue underflow");
        Key max = heap[0];
        // Key swap = pq[0];
        heap[0] = heap[--N];
        // pq[N] = swap;
        sink(0);
        heap[N] = null;     // Avoid loitering and help with garbage collection.
        if (N > 0 && N < (heap.length + 1) >> 2) // Replaced pq.length with
            resize((heap.length) >> 1); // pq.length+1 in both formulas.
        assert isMaxHeap();
        return max;
    }

    /**
     * *********************************************************************
     * Helper functions to restore the heap invariant.
     * ******************************************************************
     */
    // For sink() and swim(), recall the basic facts about a heap of width
    // 2-to-the-power-p, based at index 0:  the children of an
    // item at index j are from index (j<<p)+1...
    // To (j<<p)+1+(1<<p)-1, inclusive. This equals (j+1)<<p.
    // This is because there are exactly 2-to-the-p or (1<<p) children.
    //
    // Inversely the parent of an item at k is at (k-1)>>p.
    private void swim(int child) {
        int parent = (child - 1) >> p;
        while (child > 0 && less(parent, child)) {
            Key swap = heap[child];
            heap[child] = heap[parent];
            heap[parent] = swap;
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
            for (int sibling = child + 1, last = (parent + 1) << p;
                 sibling < N && sibling <= last;
                 sibling++)
                if (less(child, sibling)) child = sibling;
            if (!less(parent, child)) break;
            Key swap = heap[parent];
            heap[parent] = heap[child];
            heap[child] = swap;
            parent = child;
            child = (child << p) + 1;
        }
    }

    /**
     * *********************************************************************
     * Helper functions for compares and swaps.
     **********************************************************************
     */
    private boolean less(int i, int j) {
        // Also tested with return <conditional expression>, but the if ()
        // statement tested faster. I have no idea why.
        if (comparator == null)
            return ((Comparable<Key>) heap[i]).compareTo(heap[j]) < 0;
        else
            return comparator.compare(heap[i], heap[j]) < 0;
    }

    // Is pq[0..N-1] a max heap?
    private boolean isMaxHeap() {
        return isMaxHeap(0);
    }

    // Is subtree of pq[0..N-1] rooted at k a max heap?
    private boolean isMaxHeap(int parent) {
        if (parent > N - 1) return true; // Default to true in vacuous case.
        // In binary case, we only had 2 children to worry about.
        // Now we have to consider all 1<<p children of k:
        int child = (parent << p) + 1;
        // If any child of k is in the array and breaks heap order with k,
        // return false. Otherwise return true.
        for (int sib = child; sib < child + (1 << p); sib++)
            if (sib < N && less(parent, sib) ||
                !isMaxHeap(sib))
                return false;
        return true;
    }

    /*
     * Iterator
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
        private PowerOf2HeapMaxPQ<Key> copy;

        // Add all items to the copy. This takes linear time since items are
        // already in heap order.

        public HeapIterator() {
            if (comparator == null)
                copy = new PowerOf2HeapMaxPQ<Key>(p, size());
            else copy = new PowerOf2HeapMaxPQ<Key>(p, size(), comparator);
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
            return copy.delMax();
        }
    }

    // Returns a formatted string representation of the PowerOf2HeapMaxPQ.
    private String prettyString() {
        StringBuilder sb = new StringBuilder();
        // To show clearly the "next gen" operation, we write out 0<<p:
        int child = (0 << p) + 1; // First child of next generation.
        sb.append('[');
        for (int i = 0; i < N; i++) {
            sb.append(String.format("%2s", i)).
                append(':').
                append(String.format("%2s", heap[i]));
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
     * Unit tests the <tt>PowerOf2HeapMaxPQ</tt> data type. Some clutter here
     * from dealing directly with I/O.
     */
    public static void main(String[] args) {
        final boolean VERBOSE = true;
        final int LG_SIZE = 24;
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
                PowerOf2HeapMaxPQ<Integer> pq;
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
                    pq = new PowerOf2HeapMaxPQ<>(power0, testVect);
                    for (int i = TEST_HEAP_SIZE - 1; i >= 0; i--)
                        results[i] = pq.delMax();
                }
                long endTime = System.nanoTime();
                long duration = (endTime - startTime);
                out.printf(
                    "%d-heap took %d ms.\n",
                    (1 << power0),
                    duration / 1_000_000);
            }
            out.flush();

            // If args non-empty, parse the power = lg(width). Otherwise use
            // default.
            int power1 = (args.length > 0) ?
                         Integer.parseInt(args[0]) :
                         POWER_DEFAULT;
            PowerOf2HeapMaxPQ<String> dpqs =
                new PowerOf2HeapMaxPQ<String>(power1);
            if (VERBOSE) {
                out.printf("Test max %d-ary priority queue of strings:\n",
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
                        output = dpqs.delMax();
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