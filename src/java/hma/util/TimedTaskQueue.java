/**
 * 
 */
package hma.util;

import java.util.Arrays;

/**
 * This class represents a timed task queue: a priority queue of TimedTasks,
 * ordered on nextExecutionTime. Internally this class uses a heap, which offers
 * log(n) performance for the add, removeMin and rescheduleMin operations, and
 * constant time performance for the getMin operation.
 * 
 * @author guoyezhi
 * 
 */
class TimedTaskQueue {

	/**
	 * Priority queue represented as a balanced binary heap: the two children of
	 * queue[n] are queue[2*n] and queue[2*n+1]. The priority queue is ordered
	 * on the nextExecutionTime field: The TimedTask with the lowest
	 * nextExecutionTime is in queue[1] (assuming the queue is nonempty). For
	 * each node n in the heap, and each descendant of n, d, n.nextExecutionTime
	 * <= d.nextExecutionTime.
	 */
	private TimedTask[] queue = new TimedTask[128];

	/**
	 * The number of tasks in the priority queue. (The tasks are stored in
	 * queue[1] up to queue[size]).
	 */
	private int size = 0;

	/**
	 * Returns the number of tasks currently on the queue.
	 */
	int size() {
		return size;
	}

	/**
	 * Adds a new task to the priority queue.
	 */
	void add(TimedTask task) {
		// Grow backing store if necessary
		if (size + 1 == queue.length)
			queue = Arrays.copyOf(queue, 2 * queue.length);

		queue[++size] = task;
		fixUp(size);
	}

	/**
	 * Return the "head task" of the priority queue. (The head task is an task
	 * with the lowest nextExecutionTime.)
	 */
	TimedTask getMin() {
		return queue[1];
	}

	/**
	 * Return the ith task in the priority queue, where i ranges from 1 (the
	 * head task, which is returned by getMin) to the number of tasks on the
	 * queue, inclusive.
	 */
	TimedTask get(int i) {
		return queue[i];
	}

	/**
	 * Remove the head task from the priority queue.
	 */
	void removeMin() {
		queue[1] = queue[size];
		queue[size--] = null; // Drop extra reference to prevent memory leak
		fixDown(1);
	}

	/**
	 * Removes the ith element from queue without regard for maintaining the
	 * heap invariant. Recall that queue is one-based, so 1 <= i <= size.
	 */
	void quickRemove(int i) {
		assert i <= size;

		queue[i] = queue[size];
		queue[size--] = null; // Drop extra ref to prevent memory leak
	}

	/**
	 * Sets the nextExecutionTime associated with the head task to the specified
	 * value, and adjusts priority queue accordingly.
	 */
	void rescheduleMin(long newTime) {
		queue[1].nextExecutionTime = newTime;
		fixDown(1);
	}

	/**
	 * Returns true if the priority queue contains no elements.
	 */
	boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Removes all elements from the priority queue.
	 */
	void clear() {
		// Null out task references to prevent memory leak
		for (int i = 1; i <= size; i++)
			queue[i] = null;

		size = 0;
	}

	/**
	 * Establishes the heap invariant (described above) assuming the heap
	 * satisfies the invariant except possibly for the leaf-node indexed by k
	 * (which may have a nextExecutionTime less than its parent's).
	 * 
	 * This method functions by "promoting" queue[k] up the hierarchy (by
	 * swapping it with its parent) repeatedly until queue[k]'s
	 * nextExecutionTime is greater than or equal to that of its parent.
	 */
	private void fixUp(int k) {
		while (k > 1) {
			int j = k >> 1;
			if (queue[j].nextExecutionTime <= queue[k].nextExecutionTime)
				break;
			TimedTask tmp = queue[j];
			queue[j] = queue[k];
			queue[k] = tmp;
			k = j;
		}
	}

	/**
	 * Establishes the heap invariant (described above) in the subtree rooted at
	 * k, which is assumed to satisfy the heap invariant except possibly for
	 * node k itself (which may have a nextExecutionTime greater than its
	 * children's).
	 * 
	 * This method functions by "demoting" queue[k] down the hierarchy (by
	 * swapping it with its smaller child) repeatedly until queue[k]'s
	 * nextExecutionTime is less than or equal to those of its children.
	 */
	private void fixDown(int k) {
		int j;
		while ((j = k << 1) <= size && j > 0) {
			if (j < size
					&& queue[j].nextExecutionTime > queue[j + 1].nextExecutionTime)
				j++; // j indexes smallest kid
			if (queue[k].nextExecutionTime <= queue[j].nextExecutionTime)
				break;
			TimedTask tmp = queue[j];
			queue[j] = queue[k];
			queue[k] = tmp;
			k = j;
		}
	}

	/**
	 * Establishes the heap invariant (described above) in the entire tree,
	 * assuming nothing about the order of the elements prior to the call.
	 */
	void heapify() {
		for (int i = size / 2; i >= 1; i--)
			fixDown(i);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
