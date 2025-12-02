package org.itmo;

import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BFSTest {

    @Test
    public void bfsTest() throws IOException {
        int[] sizes = new int[]{10, 100, 1000, 10_000, 10_000, 50_000, 100_000, 1_000_000, 1_500_000, 2_000_000};
        int[] connections = new int[]{50, 500, 5000, 50_000, 100_000, 1_000_000, 1_000_000, 10_000_000, 10_000_000, 10_000_000};
        int[] threadCounts = new int[]{1, 2, 4, 8, 12, 16};

        Random r = new Random(42);

        try (FileWriter fw = new FileWriter("tmp/results.csv")) {
            fw.append("Vertices,Edges,Threads,Time_ms,Type\n"); // заголовок

            for (int i = 0; i < sizes.length; i++) {
                int V = sizes[i];
                int E = connections[i];
                System.out.println("--------------------------");
                System.out.println("Generating graph: V=" + V + ", E=" + E + " ...");
                Graph g = new RandomGraphGenerator().generateGraph(r, V, E);
                System.out.println("Graph generated. Starting BFS measurements...");

                // Последовательный BFS
                long serialTime = executeSerialBfsAndGetTime(g);
                System.out.println("Serial BFS time: " + serialTime + " ms");
                fw.append(V + "," + E + ",1," + serialTime + ",Serial\n");

                // Параллельный BFS с разным числом потоков
                for (int threads : threadCounts) {
                    ExecutorService pool = Executors.newFixedThreadPool(threads);
                    long parallelTime = executeParallelBfsAndGetTime(g, pool, threads);
                    pool.shutdown();
                    System.out.println("Parallel BFS (" + threads + " threads): " + parallelTime + " ms");
                    fw.append(V + "," + E + "," + threads + "," + parallelTime + ",Parallel\n");
                }

                fw.flush();
            }
        }
    }

    private long executeSerialBfsAndGetTime(Graph g) {
        long startTime = System.currentTimeMillis();
        g.bfs(0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long executeParallelBfsAndGetTime(Graph g, ExecutorService pool, int parallelLevel) {
        long startTime = System.currentTimeMillis();
        g.parallelBFS(0, pool, parallelLevel);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}
