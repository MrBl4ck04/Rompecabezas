package com.example.rompe_carvajalfranz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Solver A* para 15-puzzle con heurística Manhattan.
 * Representación: arreglo de 9 enteros con valores 0..8 (0 es vacío).
 * Devuelve una lista de índices (posiciones lineales 0..8) a donde mover el vacío en secuencia.
 */
public class FifteenPuzzleSolver {

    private static class Node implements Comparable<Node> {
        int[] state;
        int zeroIdx;
        int g; // costo acumulado
        int h; // heurística
        Node parent;
        int moveFrom; // índice que se movió al vacío para llegar aquí

        Node(int[] state, int zeroIdx, int g, int h, Node parent, int moveFrom) {
            this.state = state;
            this.zeroIdx = zeroIdx;
            this.g = g;
            this.h = h;
            this.parent = parent;
            this.moveFrom = moveFrom;
        }

        int f() { return g + h; }

        @Override
        public int compareTo(Node o) {
            int cf = Integer.compare(this.f(), o.f());
            if (cf != 0) return cf;
            return Integer.compare(this.h, o.h);
        }
    }

    public static List<Integer> solve(int[] start) {
        int size = detectSize(start);
        if (size < 2) return new ArrayList<>();
        int[] goal = goal(size);
        if (Arrays.equals(start, goal)) return new ArrayList<>();

        int zeroStart = indexOfZero(start);
        PriorityQueue<Node> open = new PriorityQueue<>();
        HashMap<String, Integer> bestG = new HashMap<>();

        Node startNode = new Node(start.clone(), zeroStart, 0, manhattan(start, size), null, -1);
        open.add(startNode);
        bestG.put(key(start), 0);

        while (!open.isEmpty()) {
            Node cur = open.poll();
            if (Arrays.equals(cur.state, goal)) {
                return reconstructMoves(cur);
            }

            for (int nei : neighbors(cur.zeroIdx, size)) {
                int[] nextState = cur.state.clone();
                swap(nextState, cur.zeroIdx, nei);
                int g = cur.g + 1;
                String k = key(nextState);
                Integer known = bestG.get(k);
                if (known == null || g < known) {
                    int h = manhattan(nextState, size);
                    Node n = new Node(nextState, nei, g, h, cur, nei);
                    open.add(n);
                    bestG.put(k, g);
                }
            }
        }
        return new ArrayList<>(); // sin solución (no debería pasar si es resoluble)
    }

    private static List<Integer> reconstructMoves(Node goal) {
        ArrayList<Integer> path = new ArrayList<>();
        Node cur = goal;
        while (cur.parent != null) {
            path.add(0, cur.moveFrom);
            cur = cur.parent;
        }
        return path;
    }

    private static int manhattan(int[] state, int size) {
        int dist = 0;
        for (int i = 0; i < state.length; i++) {
            int v = state[i];
            if (v == 0) continue;
            int targetRow = (v - 1) / size;
            int targetCol = (v - 1) % size;
            int r = i / size;
            int c = i % size;
            dist += Math.abs(r - targetRow) + Math.abs(c - targetCol);
        }
        return dist;
    }

    private static int[] goal(int size) {
        int[] g = new int[size * size];
        for (int i = 0; i < g.length - 1; i++) g[i] = i + 1;
        g[g.length - 1] = 0;
        return g;
    }

    private static int indexOfZero(int[] arr) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == 0) return i;
        return -1;
    }

    private static String key(int[] arr) {
        StringBuilder sb = new StringBuilder(arr.length * 2);
        for (int v : arr) sb.append((char) (v + 65));
        return sb.toString();
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    private static List<Integer> neighbors(int zeroIdx, int size) {
        int r = zeroIdx / size;
        int c = zeroIdx % size;
        ArrayList<Integer> ns = new ArrayList<>(4);
        if (r > 0) ns.add(zeroIdx - size);
        if (r < size - 1) ns.add(zeroIdx + size);
        if (c > 0) ns.add(zeroIdx - 1);
        if (c < size - 1) ns.add(zeroIdx + 1);
        return ns;
    }

    private static int detectSize(int[] arr) {
        int len = arr != null ? arr.length : 0;
        if (len == 0) return -1;
        int s = (int) Math.round(Math.sqrt(len));
        return s * s == len ? s : -1;
    }
}


