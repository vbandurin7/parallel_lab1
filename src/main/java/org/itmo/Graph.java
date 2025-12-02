package org.itmo;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerArray;

class Graph {
    private final int V;
    private final ArrayList<Integer>[] adjList;

    Graph(int vertices) {
        this.V = vertices;
        adjList = new ArrayList[vertices];
        for (int i = 0; i < vertices; ++i) {
            adjList[i] = new ArrayList<>();
        }
    }

    void addEdge(int src, int dest) {
        if (!adjList[src].contains(dest)) {
            adjList[src].add(dest);
        }
    }

    AtomicIntegerArray parallelBFS(int startVertex, ExecutorService threadPool, int parallelLevel) {
        AtomicIntegerArray visited = new AtomicIntegerArray(V);
        visited.set(startVertex, 1);

        // list of vertices on the current level
        List<Integer> frontier = new ArrayList<>();
        frontier.add(startVertex);

        while (!frontier.isEmpty()) {
            int tasks = Math.min(frontier.size(), parallelLevel);
            int verticesByTask = (int) Math.ceil((double) frontier.size() / tasks);;
            List<Integer> nextFrontier = new ArrayList<>();

            List<Future<List<Integer>>> futures = new ArrayList<>();
            for (int task = 0; task < tasks; task++) {
                int from = task * verticesByTask;
                // exclusive
                int to = Math.min(frontier.size(), from + verticesByTask);

                Future<List<Integer>> future = threadPool.submit(createVertexProcessingTask(frontier, from, to, visited));
                futures.add(future);
            }

            completeFutures(futures, nextFrontier);
            frontier = nextFrontier;
        }
        return visited;
    }

    private void completeFutures(List<Future<List<Integer>>> futures, List<Integer> nextFrontier) {
        for (Future<List<Integer>> future : futures) {
            try {
                List<Integer> nextFrontierPart = future.get(10, TimeUnit.SECONDS);
                nextFrontier.addAll(nextFrontierPart);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cancelAllFutures(futures);
                throw new RuntimeException("Thread interrupted while processing vertex", e);
            } catch (ExecutionException e) {
                System.err.println();
                throw new RuntimeException("Unexpected execution error during vertex processing", e);
            } catch (TimeoutException e) {
                throw new RuntimeException("Vertex processing timeout. Result is not available", e);
            }
        }
    }

    private void cancelAllFutures(List<Future<List<Integer>>> futures) {
        futures.forEach(future -> future.cancel(true));
    }

    public void printGraph() {
        System.out.println("Graph adjacency list:");
        for (int i = 0; i < V; i++) {
            System.out.print(i + " -> ");
            for (int v : adjList[i]) {
                System.out.print(v + " ");
            }
            System.out.println();
        }
    }

    private Callable<List<Integer>> createVertexProcessingTask(List<Integer> frontier, int from, int to, AtomicIntegerArray visited) {
        return () -> {
            List<Integer> neighbourVertices = new ArrayList<>();
            for (int i = from; i < to; i++) {
                Integer vertex = frontier.get(i);
                for (int v : adjList[vertex]) {
                    if (visited.get(v) == 0 && visited.compareAndSet(v, 0, 1)) {
                        neighbourVertices.add(v);
                    }
                }
            }
            return neighbourVertices;
        };
    }

    // -----------------------------------------------------
    // INCORRECT
    // -----------------------------------------------------

    boolean[] incorrectParallelBFS(int startVertex, ExecutorService threadPool, int parallelLevel) {
        boolean[] visited = new boolean[V];
        visited[startVertex] = true;

        // list of vertices on the current level
        List<Integer> frontier = new ArrayList<>();
        frontier.add(startVertex);

        while (!frontier.isEmpty()) {
            int tasks = Math.min(frontier.size(), parallelLevel);
            int verticesByTask = (int) Math.ceil((double) frontier.size() / tasks);;
            List<Integer> nextFrontier = new ArrayList<>();

            List<Future<?>> futures = new ArrayList<>();
            for (int task = 0; task < tasks; task++) {
                int from = task * verticesByTask;
                // exclusive
                int to = Math.min(frontier.size(), from + verticesByTask);

                Future<?> future = threadPool.submit(createIncorrectVertexProcessingTask(frontier, nextFrontier, from, to, visited));
                futures.add(future);
            }

            completeFutures(futures);
            frontier = nextFrontier;
        }
        return visited;
    }

    private void completeFutures(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                futures.forEach(f -> f.cancel(true));
                throw new RuntimeException("Thread interrupted while processing vertex", e);
            } catch (ExecutionException e) {
                System.err.println();
                throw new RuntimeException("Unexpected execution error during vertex processing", e);
            } catch (TimeoutException e) {
                throw new RuntimeException("Vertex processing timeout. Result is not available", e);
            }
        }
    }

    private Runnable createIncorrectVertexProcessingTask(List<Integer> frontier, List<Integer> nextFrontier, int from, int to, boolean[] visited) {
        return () -> {
            for (int i = from; i < to; i++) {
                Integer vertex = frontier.get(i);
                for (int v : adjList[vertex]) {
                    if (!visited[v]) {
                        visited[v] = true;
                        nextFrontier.add(v);
                    }
                }
            }
        };
    }

    //Generated by ChatGPT
    void bfs(int startVertex) {
        boolean[] visited = new boolean[V];

        LinkedList<Integer> queue = new LinkedList<>();

        visited[startVertex] = true;
        queue.add(startVertex);

        while (!queue.isEmpty()) {
            startVertex = queue.poll();

            for (int n : adjList[startVertex]) {
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }
    }

    Set<Integer> computeReachableFromZero() {
        boolean[] visited = new boolean[V];
        Queue<Integer> q = new LinkedList<>();
        q.add(0);
        visited[0] = true;

        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : adjList[u]) {
                if (!visited[v]) {
                    visited[v] = true;
                    q.add(v);
                }
            }
        }

        Set<Integer> reachable = new HashSet<>();
        for (int i = 0; i < V; i++) {
            if (visited[i]) reachable.add(i);
        }
        return reachable;
    }

}
