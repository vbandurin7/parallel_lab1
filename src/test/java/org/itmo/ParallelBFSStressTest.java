package org.itmo;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.Z_Result;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

@JCStressTest
@Outcome(id = "true", expect = Expect.ACCEPTABLE, desc = "Correct traversal")
@Outcome(id = "false", expect = Expect.FORBIDDEN, desc = "Race detected, incorrect BFS result")
@State
public class ParallelBFSStressTest {
    private static final int V = 20;
    private static final int E = 30;
    private final Graph g = new RandomGraphGenerator().generateGraph(new Random(42), V, E);
    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    @Actor
    public void bfsActor(Z_Result r) {
        AtomicIntegerArray res = g.parallelBFS(0, pool, 4);
        Set<Integer> reachable = g.computeReachableFromZero();

        int cnt = 0;
        for (int i = 0; i < res.length(); i++) {
            cnt += res.get(i);
        }
        r.r1 = cnt == reachable.size();
    }

    @Arbiter
    public void arbiter() {
        pool.shutdownNow();
    }
}
