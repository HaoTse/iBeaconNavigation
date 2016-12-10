package com.uscc.ibeacon_navigation.algorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Created by Oslo on 12/11/16.
 */
public class AStar {

    public static Avertex [][] graph = new Avertex[10][10];
    public static PriorityQueue<Avertex> openList;
    public static boolean closedList[][];

    public static int startX;
    public static int startY;
    public static int endX;
    public static int endY;

    // set blocked area in graph
    public static void setBlocked(int x, int y) {
        graph[x][y] = null;
    }

    public static void setStartVertex(int x, int y) {
        startX = x;
        startY = y;
    }

    public static void setEndVertex(int x, int y) {
        endX = x;
        endY = y;
    }

    public static void updateCost(Avertex currentVertex, Avertex newVertex, int cost) {
        if (newVertex == null || closedList[newVertex.x][newVertex.y]) {
            return;
        }
        int new_final_Cost = newVertex.heuristicCost + cost;
        boolean isOpen = openList.contains(newVertex);
        if (!isOpen || new_final_Cost < newVertex.finalCost) {
            // update cost
            newVertex.finalCost = new_final_Cost;
            newVertex.parent = currentVertex;
            if (!isOpen) {
                // add to open list
                openList.add(newVertex);
            }
        }
    }

    public static void AStar() {
        // add starting point
        openList.add(graph[startX][startY]);

        Avertex current;
        while(true) {
            current = openList.poll();
            if (current == null) {
                break;
            }
            closedList[current.x][current.y] = true;

            Avertex temp;
            if ((current.x -1) >= 0) {
                temp = graph[current.x-1][current.y];
                // 10 is the fixed cost of new vertex
                updateCost(current, temp, current.finalCost + 10);

                if( (current.y -1) >= 0) {
                    temp = graph[current.x-1][current.y-1];
                    // 15 is the fixed diagonal cost
                    updateCost(current, temp, current.finalCost + 15);
                }

                if (current.y+1 < graph[0].length) {
                    temp = graph[current.x-1][current.y+1];
                    updateCost(current, temp, current.finalCost + 15);
                }
            }

            if ((current.y -1) >= 0) {
                temp = graph[current.x][current.y-1];
                updateCost(current, temp, current.finalCost + 10);
            }

            if ((current.y + 1) < graph[0].length ) {
                temp = graph[current.x][current.y + 1];
                updateCost(current, temp, current.finalCost + 10);
            }

            if ( (current.x + 1) < graph.length) {
                temp = graph[current.x+1][current.y];
                updateCost(current, temp, current.finalCost + 10);

                if( (current.y -1) >= 0) {
                    temp = graph[current.x+1][current.y-1];
                    // 15 is the fixed diagonal cost
                    updateCost(current, temp, current.finalCost + 15);
                }

                if (current.y+1 < graph[0].length) {
                    temp = graph[current.x+1][current.y+1];
                    updateCost(current, temp, current.finalCost + 15);
                }
            }

        }
    }

    public static Map<String, String> executeAStar(int x, int y, int startX, int startY, int endX, int endY, int[][] blocked) {
        System.out.println("start A* algorithm...\n");

        graph = new Avertex[x][y];
        closedList = new boolean[x][y];
//        openList = new PriorityQueue<>((Object one, Object two) -> {
//            AVertex v1 = (AVertex) one;
//            AVertex v2 = (AVertex) two;
//            return v1.finalCost < v2.finalCost ? -1: v1.finalCost > v2.finalCost ? 1: 0;
//        });
        openList = new PriorityQueue<>();
        // 0, 0 by default
        setStartVertex(startX, startY);
        setEndVertex(endX, endY);

        for (int i = 0; i< x; ++i) {
            for (int j = 0; j< y; ++j) {
                graph[i][j] = new Avertex(i, j);
                graph[i][j].heuristicCost = Math.abs(i - endX) + Math.abs(j - endY);
            }
        }
        // initialize final cost to 0
        graph[startX][startY].finalCost = 0;

        // set blocked vertexes
        for (int i = 0;i<blocked.length; i++) {
            setBlocked(blocked[i][0], blocked[i][1]);
        }

        // run algorithm
        AStar();

        // print out the scores for cells
        System.out.println("\nScores: \n");
        for (int i = 0; i< x; ++i) {
            for (int j = 0;j<y;++j) {
                if (graph[i][j] != null){
                    System.out.printf("%-3d ", graph[i][j].finalCost);
                } else {
                    System.out.print("BL  ");
                }
            }
            System.out.println();
        }
        System.out.println();

        Map<String, String> result = new HashMap<String, String>();
        // trace back the path
        if (closedList[endX][endY]) {
            System.out.println("Path: ");
            Avertex current = graph[endX][endY];
//            System.out.print(current);
            result.put(String.valueOf(current.x), String.valueOf(current.y));
            while(current.parent != null) {
//                System.out.print(" -> " + current.parent);
                result.put(String.valueOf(current.parent.x), String.valueOf(current.parent.y));
                current = current.parent;
            }
//            System.out.println();
        } else {
//            System.out.println("No path.");
        }
        return result;
    }

}
