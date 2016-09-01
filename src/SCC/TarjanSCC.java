package SCC;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import Util.util;

/**
 * The <tt>TarjanSCC</tt> class represents a data type for
 * determining the strong components in a digraph.
 * The <em>id</em> operation determines in which strong component
 * a given vertex lies; the <em>areStronglyConnected</em> operation
 * determines whether two vertices are in the same strong component;
 * and the <em>count</em> operation determines the number of strong
 * components.
 * 
 * The <em>component identifier</em> of a component is one of the
 * vertices in the strong component: two vertices have the same component
 * identifier if and only if they are in the same strong component.
 * 
 * <p>
 * This implementation uses Tarjan's algorithm.
 * The constructor takes time proportional to <em>V</em> + <em>E</em>
 * (in the worst case),
 * where <em>V</em> is the number of vertices and <em>E</em> is the number of
 * edges.
 * Afterwards, the <em>id</em>, <em>count</em>, and
 * <em>areStronglyConnected</em>
 * operations take constant time.
 * For alternate implementations of the same API, see
 * {@link KosarajuSharirSCC} and {@link GabowSCC}.
 * <p>
 * For additional documentation,
 * see <a href="http://algs4.cs.princeton.edu/42digraph">Section 4.2</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * 
 *         The original class has been modified to meet the HPA tool needs.
 * @author Yue Ben
 */
public class TarjanSCC {
	private int V; // total number of nodes
	public boolean[] marked; // marked[v] = has v been visited?
	private int[] low; // low[v] = low number of v
	private int pre; // pre-order number counter
	private int count; // number of strongly-connected components
	private Stack<Integer> stack;
	public int[] id; // id[v] = id of strong component containing v

	/**
	 * Computes all strong components of the DGraph G,
	 * including those unreachable from initial state(s)
	 * 
	 * @param adj_out_int
	 * @param NV: the number of states.
	 *            NV = adj_out_int.length if determinized adding ERROR, or else
	 *            NV = adj_out_int.length -1
	 */
	public TarjanSCC(int NV) {
		V = NV;
		marked = new boolean[V];
		stack = new Stack<Integer>();
		id = new int[V];
		low = new int[V];
	}

	/**
	 * Obtain all SCCs even containing the unreachable from initial state.
	 */
	public void dfsAll(HashSet<Integer>[] adj) {
		for (int v = 0; v < V; v++) {
			if (!marked[v]) {
				dfs(adj, v);
			}
		}
		orderId();
	}

	public void dfs_set(HashSet<Integer>[] adj, Set<Integer> nodes) {
		for (int v : nodes) {
			if (!marked[v]) {
				dfs(adj, v);
			}
		}
	}

	/**
	 * DFS from a node v. Unmarked states are unreachable from q0. Return the
	 * number of states visited.
	 */
	public int dfs(HashSet<Integer>[] adj_out_int, int v) {
		int visited = 1;
		marked[v] = true;
		low[v] = pre++;
		int min = low[v];
		stack.push(v);
		for (int q1 : adj_out_int[v]) {
			int w = q1;
			if (!marked[w])
				visited += dfs(adj_out_int, w);
			if (low[w] < min)
				min = low[w];
		}
		if (min < low[v]) {
			low[v] = min;
			return visited;
		}
		int w;
		do {
			w = stack.pop();
			id[w] = count;
			low[w] = V;
		} while (w != v);
		count++;
		return visited;
	}

	/**
	 * Reassign the id numbers to each SCC s.t. SCC containing smaller
	 * node have smaller id. Easier for debugging.
	 */
	public void orderId() {
		int max = util.max(id);
		int minl = 0;
		for (int i = 0; i < id.length; i++) {
			if (id[i] > minl) {
				max++;
				util.changeSpecificValues(id, minl, max);
				util.changeSpecificValues(id, id[i], minl);
				minl++;
			}
			if (id[i] == minl)
				minl++;
		}

	}

	/**
	 * Are vertices <tt>v</tt> and <tt>w</tt> in the same strong component?
	 * 
	 * @param v one vertex
	 * @param w the other vertex
	 * @return <tt>true</tt> if vertices <tt>v</tt> and <tt>w</tt> are in the
	 *         same
	 *         strong component, and <tt>false</tt> otherwise
	 */
	public boolean stronglyConnected(int v, int w) {
		return id[v] == id[w];
	}

	/**
	 * Returns the number of strong components.
	 * 
	 * @return the number of strong components
	 */
	public int count() {
		return count;
	}

	public int[] id() {
		return this.id;
	}

	/**
	 * Returns the idx of the SCC containing vertex v
	 */
	public int id(int v) {
		return id[v];
	}

	/** extract sccs into an ArrayList, in SCC index ascending order */
	@SuppressWarnings("unchecked")
	public ArrayList<Integer>[] sccs() {
		ArrayList<Integer>[] sccs =
		        (ArrayList<Integer>[]) new ArrayList[this.count];
		for (int i = 0; i < this.count; i++) {
			sccs[i] = new ArrayList<Integer>();
		}
		for (int i = 0; i < V; i++) {
			sccs[id(i)].add(i);
		}
		return sccs;
	}

	/** Return an array containing head-nodes of the sccs nodes are in */
	public int[] head() {
		int[] head = new int[V];
		int[] sccHead = new int[this.count]; // head of each scc
		for (int i = 0; i < this.count; i++)
			sccHead[i] = -1;
		for (int i = 0; i < V; i++) {
			if (sccHead[id(i)] == -1)
				sccHead[id(i)] = i;
			head[i] = sccHead[id(i)];
		}

		return head;
	}
}
