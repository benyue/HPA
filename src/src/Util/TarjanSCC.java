package Util;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;

import HPA.PA;

public class TarjanSCC {
	private int V;                   //total number of nodes
    private boolean[] marked;        // marked[v] = has v been visited?
    public int[] id;                // id[v] = id of strong component containing v
    private int[] low;               // low[v] = low number of v
    private int pre;                 // pre-order number counter
    private int count;               // number of strongly-connected components
    private Stack<Integer> stack;
    public int unReachableState = -1; //mark whether every state is reachable from the initial state


    /**
     * Computes all strong components of the DGraph G,
     * including those unreachable from initial state(s)
     * @param NV = G.V.size();
     */
    public TarjanSCC(ArrayList<Integer>[] adj_out_int) {
    	int NV = adj_out_int.length;
    	V = NV;
        marked = new boolean[NV];
        stack = new Stack<Integer>();
        id = new int[NV]; 
        low = new int[NV];
        
        //Obtain all SCCs even containing the unreachable from initial states
        for (int v = 0; v < NV; v++) {
            if (!marked[v]){
            	dfs(adj_out_int, v);
            }
        }
        
        rearrangeId();
    }

    private void dfs(ArrayList<Integer>[] adj_out_int, int v) { 
    	int NV = adj_out_int.length;
        marked[v] = true;
        low[v] = pre++;
        int min = low[v];
        stack.push(v);
        for (int q1:adj_out_int[v]) {
        	int w = q1;
            if (!marked[w]) dfs(adj_out_int, w);
            if (low[w] < min) min = low[w];
        }
        if (min < low[v]) { low[v] = min; return; }
        int w;
        do {
            w = stack.pop();
            id[w] = count;
            low[w] = NV;
        } while (w != v);
        count++;
    }


    /**
     * Reassign the id numbers to each SCC s.t. SCC containing smaller smallest node have smaller id
     * */
    private void rearrangeId(){
    	int max = util.max(id);
    	int minl = 0;    	
    	for(int i=0; i<id.length; i++){
    		if(id[i] > minl){
    			max ++;
    			util.changeSpecificValues(id, minl, max);
    			util.changeSpecificValues(id, id[i],minl);
    			minl ++;
    		}
    		if(id[i] == minl) minl++;
    	}

    }


    /**
     * Are vertices <tt>v</tt> and <tt>w</tt> in the same strong component?
     * @param v one vertex
     * @param w the other vertex
     * @return <tt>true</tt> if vertices <tt>v</tt> and <tt>w</tt> are in the same
     *     strong component, and <tt>false</tt> otherwise
     */
    public boolean stronglyConnected(int v, int w) {
        return id[v] == id[w];
    }

    /**
	 * Returns the number of strong components.
	 * @return the number of strong components
	 */
	public int count() {
	    return count;
	}

	public int[] id(){
		return this.id;
	}

	/**
     * Returns the idx of the SCC containing vertex v
     */
    public int id(int v) {
        return id[v];
    }
    
    /**extract sccs into an ArrayList*/
    @SuppressWarnings("unchecked")
	public ArrayList<Integer>[] sccs(){
        ArrayList<Integer>[] sccs = (ArrayList<Integer>[]) new ArrayList[this.count];
        
        for (int i = 0; i < this.count; i++) {
            sccs[i] = new ArrayList<Integer>();
        }
        
        for (int i = 0; i < V; i++) {
            sccs[this.id(i)].add(i);
        }
        return sccs;
    }
    
  /**Return an array containing head-nodes of the sccs nodes are in*/
    public int[] head(){
        int[] head = new int[V]; 
        int[] sccHead = new int[this.count]; //head of each scc
        for(int i=0; i<this.count;i++) sccHead[i] = -1;
        for (int i = 0; i < V; i++) {
        	if(sccHead[id(i)] == -1) sccHead[id(i)] = i;
        	head[i] = sccHead[id(i)];
        }
        
        return head;
    }


    /**
     * Unit tests the <tt>TarjanSCC</tt> data type.
     * @throws Exception 
     */
    @SuppressWarnings({ "unchecked", "unused" })
	public static void main () throws Exception {
    	File f = new File("data/test.txt");
    	edu.princeton.cs.introcs.In in = new edu.princeton.cs.introcs.In(f);
    	HPA.PA g = new HPA.PA(in);    	
        TarjanSCC tjscc = new TarjanSCC(g.adj_out_int);
        int M = tjscc.count();
        ArrayList<Integer>[] sccs = (ArrayList<Integer>[]) new ArrayList[M];

    }

}
