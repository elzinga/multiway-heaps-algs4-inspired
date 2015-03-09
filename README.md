# multiway-heaps-algs4-inspired
Priority queues implemented with multiway heaps. Includes max, min orientations; generic, indexed-generic flavors.
 Generic max priority queue implementation with a d-ary heap. This
 variation specifically supports d equal to any positive integer.
 Implementation note: A version was written using all bit-shift operations
 instead of multiplication and division to reach child and parent
 nodes, but empirical tests showed this worsened performance. I'm not sure why, 
 and haven't traced the bytecode yet.

Degenerate case: This implementation works even for width==1, which makes a
degenerate "unary heap", approximately equivalent to insertion sort stored
in an array list. Because of the O(N) copying required for each sink() and
swim() operation when inserting into a sorted array, the width==1 case is
not useful in practice, as it takes O(N^^2) for tasks that should require
O(NlgN). But it makes a neat edge case, and like the proverbial dancing
pig, it's less interesting how well it performs than that it performs at
all.

Notation: We write informally N^^2 for "N to the power 2". This avoids confusion with
2^p which means "2 bitwise-XOR with p" in Java, and 2**p, which is FORTRAN-friendly but awkward
notation for programmers steeped in C pointers. Using Java bit-shift
operations, raising 2 to the power p is D. Multiplying k times 2 to the
p is (k << p). So the informal notation 2^^p is written in Java as D.

As in Robert Sedgewick & Kevin Wayne's binary heap implementation, this can
be used with a comparator instead of the natural order, but the generic Key
type must still be Comparable.

We go back to zero-based array, because Sedgewick and Wayne's elegant
one-based array simplification for parent-child calculations doesn't provide
quite the same stark elegance in the generalized D-ary case, where we're
multiplying and dividing by D, as in the textbook implementation where
they are multiplying and dividing by 2.

One elegant nugget remains. Inspired by Sedgewick and Wayne, we do use simple
division or multiplication of an index (plus or minus 1) by the width D, to find
the index of its parent or child in in the flat array we use for our heap store.

About that plus or minus one... Which is it? For a fully packed zero-based
array for our heap, use these concise and appealing facts:

*Fact 1:* The parent of the item at index i is at index (i-1)/D.

*Fact 2:* Inversely, the first child of the item at index j is found at
j*D+1 (if this is in range).

*Fact 3:* This first child has (D-1) other subsequent siblings, in the
range [(j*D+1 + 1) ... (j*D+1 + (D-1))]. In a D-ary heap,
the sink operation must exchange with the max of all D siblings at each
depth, one of the main tradeoffs for decreasing the depth of the heap. This
is the primary difference between this implementation and Sedgewick & Wayne's
inspiration: here when we sink a key, to maintain the heap invariant we must
execute a for-loop to find the max of the children.

*Fact 4:* The last item is at N-1, and so the parent of the last item, the
last parent, is at ((N-1)-1)/D, equal to (N-2)/D.

Note: We *could* still start the heap at array index 1, but then we'd have
to use for parent index, (i+(D-1))/D, equal to ((i-1)/D) + 1, neither
of which is as pretty. We gain little in the d-ary case by starting at 1.
(Kevin Wayne tells me in email that starting with the root element at
location D-1 helps with caching or something, especially in the D == 1<<p case.
But he isn't sure, and I think it's just for the elegant beauty of the binary
case. If you know the answer, please email me.

ILLUSTRATIVE INDICES AND THEIR D-ARY PARENTS
Index:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 ...
2-ary:  -  0  0  1  1  2  2  3  3  4  4  5  5  6  6  7  7  8  8  9  9 10 ...
4-ary:  -  0  0  0  0  1  1  1  1  2  2  2  2  3  3  3  3  4  4  4  4  5 ...
8-ary:  -  0  0  0  0  0  0  0  0  1  1  1  1  1  1  1  1  2  2  2  2  2 ...

EXAMPLES
2-ary parent of item 14 is at (14-1)>>1 == 6.
4-ary parent of item 17 is at (17-1)>>2 == 4.
8-ary parent of item 21 is at (21-1)>>3 == 2.
These all match with Fact 1.

Wayne and Sedgewick point out this can be optimized by replacing full
exchanges with half exchanges (ala insertion sort).

