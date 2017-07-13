package org.mkonchady.myyatra;


import android.util.Log;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

/**
 * Genetic Algorithm Manager -- called from PlaceActivity
 */
public class GAManager {

    // constants
    private final int MAX_RUN_TIME = 100;               // maximum time in seconds for the run
    private int POPSIZE;	       	                    // population size

    // ga arrays
    private int[][] routes = null;                      // computer routes
    private float[] dist = null;                        // distance for each route
    private float[] tdist = null;                       // temp. route distances
    private int gcount = 0;                             // generation count
    private int GENERATION_LIMIT;                       // limit the no. of generations
    private long startTime;                             // starting time of GA run in milliseconds

    // passed initialization variables
    private int DIM = 0;                                // number of cities
    private float[][] distances = null;                 // pair wise distances

    // best computed route, accessed from PlaceActivity
    public int[] bestRoute = null;                      // best computer route
    public float bestDistance = 0;                      // best distance
    final String TAG = "GAManager";

    // accept size of problem and matrix of pair wise distances
    public GAManager(int DIM, float[][] distances, int computer_time) {

        this.DIM = DIM;
        this.distances = distances;
        startTime = System.currentTimeMillis();

        // allocate space for the tables
        POPSIZE = DIM  * computer_time;           // allocate a larger population for longer comp. times
        GENERATION_LIMIT = DIM * computer_time;   // maximum number of generations
        routes = new int[POPSIZE][DIM+1];   // computer routes
        dist = new float[POPSIZE];          // distance for each route
        tdist = new float[POPSIZE];         // temporary route distances
        bestRoute = new int[DIM+1];         // best route so far


        // initial best route
        for (int i = 0; i < DIM; i++)
            bestRoute[i] = i;
        bestRoute[DIM] = 0;

        // generate a bunch of random numbers
        int rlimit = POPSIZE * 3 * DIM;
        int[] rand = new int[rlimit];
        for (int i = 0; i < rlimit; i++)
            rand[i] = (int) (Math.random() * DIM);

        // populate POPSIZE routes
        for (int i = 0; i < POPSIZE; i++) {
            int sind = i * (DIM / 2);
            int j = 0;
            while (j != DIM)
                if (!(dup(i,j,rand[++sind])))
                    routes[i][j++] = rand[sind];
            routes[i][j] = routes[i][0];
        }

        // for each route compute the round trip distance
        for (int i = 0; i < POPSIZE; i++) {
            dist[i] = 0.0f;
            for (int j = 0; j < DIM; j++)
                dist[i] += distances[routes[i][j]][routes[i][j + 1]];
        }

    }

    // make sure the route contains unique nodes
    boolean dup(int i, int j, int checkNode) {
        int x;
        boolean duplicate = false;
        for (x = 0; x < j; x++)
            if (routes[i][x] == checkNode)
                duplicate = true;
        return duplicate;
    }

    // run the genetic algorithm for GENERATION_LIMIT generations
    public void run_ga() {
        while (gcount < GENERATION_LIMIT) {
            ga();
            ++gcount; // increment the generation counter
            if ( (System.currentTimeMillis() - startTime) > MAX_RUN_TIME * 1000) {
                gcount = GENERATION_LIMIT;      // if more than 100 seconds, then terminate
            }
        }
    }

    // run a generation and save the best route
    public void  ga() {
        int i, j, k;
        float minval, maxdist;
        int bestind = 0;
        int last = 0;
        int para, parb;
        final int TOPSIZE = (int) (0.60 * POPSIZE);	// number of top routes
        final int MUTSIZE = (int) (0.60 * POPSIZE);	// number of mutations
        final int EPSILON = 1;			            // min. route distance difference
        float mindist;                              // least distance
        int[] tloc = new int[TOPSIZE*2];            // temporary route numbers

        // loop thru a few iterations
        for (i = 0; i < (POPSIZE / 2); i++) {

            // find the top TOPSIZE*2 routes
            for (j = 0; j < POPSIZE; j++)
                tdist[j] = dist[j];
            for (j = 0; j < TOPSIZE*2; j++) {
                for (k = 0, minval = 1000000; k < POPSIZE; k++)
                    if ( (minval > tdist[k]) && (tdist[k] != 0) ) {
                        minval = tdist[k];
                        tloc[j] = k;
                    }
                tdist[tloc[j]] = 0;
            }

            // eliminate duplicates to maintain diversity
            for (j = 0, k = TOPSIZE; j < (TOPSIZE - 1); j++)
                if ( (dist[tloc[j+1]] - dist[tloc[j]]) < EPSILON) {
                  zap(tloc[k], tloc[j]);
                  k++;
                }

            // pick two parents at random for crossover
            for (k = 0; k < TOPSIZE; k++) {
                do {
                    para = (int) (Math.random() * TOPSIZE);
                    parb = (int) (Math.random() * TOPSIZE);
                } while (para == parb);

                // cross over para and parb, find the worst route
                for (j = 0, maxdist = 0; j < POPSIZE; j++)
                    if (dist[j] > maxdist) {
                        maxdist = dist[j];
                        last = j;
                    }
                crossover( tloc[para], tloc[parb], last);
            }

            // mutate some individuals
            for (j = 0; j < MUTSIZE; j++)
                mutate( tloc[(int) (Math.random() * TOPSIZE)]);

        }

        checkRoutes();

        // find and save the best route
        for (i = 0, mindist = 1000000.0f; i < POPSIZE; i++) {
            if (dist[i] < mindist) {
                bestind = i;
                mindist = dist[i];
            }
        }

        // do not allow caller to read best route during update
        synchronized(this) {
            bestDistance = mindist;
            for (i = 0; i < (DIM + 1); i++)
                bestRoute[i] = routes[bestind][i];
        }

    }

    /*
       use the parents to create a new offspring. The route is
       copied from l-r in one parent and r-l in the other until
       a duplicate. Then the other nodes are filled.
     */
    public void crossover (int ga, int gb, int g) {
        boolean fa = true, fb = true;
        int x = 0, y = 0, t;
        int i,j;

        // pick a random node for crossover
        t = (int) (Math.random() * DIM);

        //set x and y where gax = t and gby = t
        for (i = 0; i < DIM; i++)
            if (routes[ga][i] == t) {
                x = i;
                break;
            }

        for (i = 0; i < DIM; i++)
            if (routes[gb][i] == t) {
                y = i;
                break;
            }

        // initialize the offspring
        for (i = 0; i < DIM; i++)
            routes[g][i] = -1;
        routes[g][0] = t;

        // fill in the offpring alternately from parents
        do {
            x = (x == 0) ? (DIM - 1): (x - 1); // find location for r-l copy
            y = (y + 1) % DIM;                 // find location for l-r copy

            if (fa) {
                // check if gax is part of g
                for (i = 0; i < DIM; i++)
                    if (routes[g][i] == routes[ga][x]) {
                        fa = false;
                        break;
                    }
                if (fa)
                    linsert(g, routes[ga][x]);
            }

            if (fb) {
                // check if gby is part of g
                for (i = 0; i < DIM; i++)
                    if (routes[g][i] == routes[gb][y]) {
                        fb = false;
                        break;
                    }
                if (fb)
                    rinsert(g, routes[gb][y]);
            }

        } while ( (fa) || (fb) );

        // fill in the remaining stuff
        outer: for (i = 0; i < DIM; i++) {
            for (j = 0; j < DIM; j++)
                if (routes[g][j] == i)
                    continue outer;
            for (j = 0; j < DIM; j++)
                if (routes[g][j] == -1) {
                    routes[g][j] = i;
                    break;
                }
        }
        routes[g][DIM] = routes[g][0];


        // re-compute the distance for g
        for (i = 0, dist[g] = 0; i < DIM; i++)
            dist[g] += distances[routes[g][i]][routes[g][i+1]];

    }

    //  switch two nodes at random and  re-calculate the route
    public void mutate(int g) {
        int a, b;
        float de;

        // select two random interior vertices
        do {
            a = (int) ( Math.random() * (DIM-1) );
            b = (int) ( Math.random() * (DIM-1) );
        } while ( (a == 0) || (b == 0) || (a == b) );

        // calculate difference de
        if (b == DIM - 1)
            de = (distances[ routes[g][a] ][ routes[g][a+1] ] + distances[ routes[g][b-1] ][ routes[g][b] ] ) -
                 (distances[ routes[g][b] ][ routes[g][a+1] ] + distances[ routes[g][b-1] ][ routes[g][a] ] );
        else if (a == DIM - 1)
            de = (distances[ routes[g][b] ][ routes[g][b+1] ] + distances[ routes[g][a-1] ][ routes[g][a] ] ) -
                 (distances[ routes[g][a] ][ routes[g][b+1] ] + distances[ routes[g][a-1] ][ routes[g][b] ] );
        else if (  Math.abs(a - b) > 1 )
        //if (  Math.abs(a - b) > 1 )
            de = ( distances[ routes[g][a-1] ][ routes[g][a] ] + distances[ routes[g][a] ][ routes[g][a+1] ]   +
                   distances[ routes[g][b-1] ][ routes[g][b] ] + distances[ routes[g][b] ][ routes[g][b+1] ] ) -
                 ( distances[ routes[g][a-1] ][ routes[g][b] ] + distances[ routes[g][b] ][ routes[g][a+1] ]   +
                   distances[ routes[g][b-1] ][ routes[g][a] ] + distances[ routes[g][a] ][ routes[g][b+1] ] ) ;
        else {
            if (a > b) a = swap(b, b = a);
            de = ( distances[ routes[g][a-1] ][ routes[g][a] ] + distances[ routes[g][b] ][ routes[g][b+1] ] ) -
                 ( distances[ routes[g][a-1] ][ routes[g][b] ] + distances[ routes[g][a] ][ routes[g][b+1] ] );
        }

        if (de > 0) {
           routes[g][a] = swap(routes[g][b], routes[g][b] = routes[g][a]);
           dist[g] -= de;
        }

    }

    public int swap(int a, int b) {
        return a;
    }

    public void checkRoutes() {
        for (int[] route: routes)
            if (!isValidRoute(route))
                Log.e(TAG, "Invalid route: " + dumpRoute(route));
    }

   public boolean isValidRoute(int[] route) {

       ArrayList<Integer> seen = new ArrayList<>();
       for (int i = 0; i < route.length - 1; i++) {
           int j = route[i];
           if (seen.contains(j)) {
               Log.e(TAG, "Duplicate node" + j);
               return false;
           }
           else seen.add(j);
       }
       int firstNode = route[0];
       int lastNode = route[route.length - 1];
       if (!(firstNode == lastNode)) {
           Log.e(TAG, "Last and first do not match: ");
           return false;
       }
       return true;
   }

    public String dumpRoute(int[] route) {
        StringBuilder out = new StringBuilder();
        for (int node: route) {
            out.append(node);
            out.append(" ");
        }
        return out.toString();
    }

    //  insert into routes[g] from the left
    public void linsert( int g, int val ) {
        // shift array elements to the right
        for (int i = (DIM-1); i >= 0; i--)
            routes[g][i+1] = routes[g][i];
        routes[g][0] = val;
    }

    // insert into routes[g] from the right
    public void rinsert( int g, int val ) {
        int i = 0;
        // insert val into the first element with -1
        do {
            i++;
        } while (routes[g][i] != -1);
        routes[g][i] = val;
    }

    // copy from one route to another
    public void zap( int from, int to ) {
        for (int i = 0; i < DIM+1; i++)
            routes[to][i] = routes[from][i];
        dist[to] = dist[from];
    }

    //public int[] getBestRoute() {
    //    return bestRoute;
    //}
}