package com.uscc.ibeacon_navigation.algorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class AStar {

    public static Avertex [][] graph;
    public static PriorityQueue<Avertex> openList;
    public static boolean closedList[][];

    public static int startX;
    public static int startY;
    public static int graphX;
    public static int graphY;
    public static int endX;
    public static int endY;
    private static HashMap<Integer, Integer> block_graph;

    public AStar(int graph_x_size, int graph_y_size) {
        graphX = graph_x_size;
        graphY = graph_y_size;
        this.graph = new Avertex[graphX][graphY];
        // initialize blocked_graph here
        block_graph = new HashMap<Integer, Integer>();
        // add all blocked_space to blocked_graph
        initializeBlockGraph();
    }

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

    private void initializeBlockGraph() {
        // left bottom corner: 1
        for(int i = 0; i< 60; i++) {
            for (int j = 45; j<110;j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }

        // upper classrooms: 2
        for (int i = 6; i < 59; i++) {
            for (int j = 0; j< 4; j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }

        // middle exit lower part: 3
        for (int i = 60; i< 66;i++) {
            for (int j = 46; j < 110; j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }

        // that corner and the rectangular part: 4
        for (int i = 66; i< 81;i++) {
            for (int j = 43; j < 110; j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }

        // middle bottom triangle: 5
        for (int j = 43; j< 110;j++) {
            this.block_graph.put(new Integer(81), new Integer(j));
            //System.out.printf("%d, %d\n", 81, j);
        }
        for (int j = 48; j< 110;j++) {
            this.block_graph.put(new Integer(82), new Integer(j));
            //System.out.printf("%d, %d\n", 81, j);
        }
        for (int j = 51; j< 110;j++) {
            this.block_graph.put(new Integer(83), new Integer(j));
            //System.out.printf("%d, %d\n", 83, j);
        }
        for (int j = 55; j< 110;j++) {
            this.block_graph.put(new Integer(84), new Integer(j));
            //System.out.printf("%d, %d\n", 84, j);
        }
        for (int j = 59; j< 110;j++) {
            this.block_graph.put(new Integer(85), new Integer(j));
            //System.out.printf("%d, %d\n", 85, j);
        }
        for (int j = 62; j< 110;j++) {
            this.block_graph.put(new Integer(86), new Integer(j));
            //System.out.printf("%d, %d\n", 86, j);
        }
        for (int j = 66; j< 110;j++) {
            this.block_graph.put(new Integer(87), new Integer(j));
            //System.out.printf("%d, %d\n", 87, j);
        }
        for (int j = 71; j< 110;j++) {
            this.block_graph.put(new Integer(88), new Integer(j));
            //System.out.printf("%d, %d\n", 88, j);
        }
        for (int j = 75; j< 110;j++) {
            this.block_graph.put(new Integer(89), new Integer(j));
            //System.out.printf("%d, %d\n", 89, j);
        }
        for (int j = 79; j< 110;j++) {
            this.block_graph.put(new Integer(90), new Integer(j));
            //System.out.printf("%d, %d\n", 90, j);
        }
        for (int j = 84; j< 110;j++) {
            this.block_graph.put(new Integer(91), new Integer(j));
            //System.out.printf("%d, %d\n", 91, j);
        }
        for (int j = 88; j< 110;j++) {
            this.block_graph.put(new Integer(92), new Integer(j));
            //System.out.printf("%d, %d\n", 92, j);
        }
        for (int j = 92; j< 110;j++) {
            this.block_graph.put(new Integer(93), new Integer(j));
            //System.out.printf("%d, %d\n", 93, j);
        }
        for (int j = 96; j< 110;j++) {
            this.block_graph.put(new Integer(94), new Integer(j));
            //System.out.printf("%d, %d\n", 94, j);
        }
        for (int j = 100; j< 110;j++) {
            this.block_graph.put(new Integer(95), new Integer(j));
            //System.out.printf("%d, %d\n", 95, j);
        }

        // bottom right blocks
        for (int i = 97; i < 105;i++) {
            for (int j = 105; j < 110;j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }
        for (int j = 104; j< 110;j++) {
            this.block_graph.put(new Integer(106), new Integer(j));
            //System.out.printf("%d, %d\n", 106, j);
        }
        for (int j = 105; j< 110;j++) {
            this.block_graph.put(new Integer(107), new Integer(j));
            //System.out.printf("%d, %d\n", 107, j);
        }
        for (int j = 103; j< 110;j++) {
            this.block_graph.put(new Integer(108), new Integer(j));
            //System.out.printf("%d, %d\n", 108, j);
        }
        for (int i = 109; i < 132;i++) {
            for (int j = 107; j < 110;j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }

        // most right block
        for (int i = 132; i < 135;i++) {
            for (int j = 0; j < 110;j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }


        // borders for top-left classrooms(2)
        for (int j = 3; j < 33; j++) {
            this.block_graph.put(new Integer(6), new Integer(j));
            //System.out.printf("%d, %d\n", 6, j);
        }
        for (int j = 3; j < 33; j++) {
            this.block_graph.put(new Integer(32), new Integer(j));
            //System.out.printf("%d, %d\n", 32, j);
        }
        for (int j = 3; j < 33; j++) {
            this.block_graph.put(new Integer(58), new Integer(j));
            //System.out.printf("%d, %d\n", 58, j);
        }
        for (int i = 10; i < 29; i++) {
            this.block_graph.put(new Integer(i), new Integer(33));
            //System.out.printf("%d, %d\n", i, 33);
        }
        for (int i = 35; i < 55; i++) {
            this.block_graph.put(new Integer(i), new Integer(33));
            //System.out.printf("%d, %d\n", i, 33);
        }

        // borders for top-right spaces
        for (int i = 100; i < 132; i++) {
            for (int j = 0; j < 3; j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }
        for (int i = 104; i < 132; i++) {
            for (int j = 3; j < 25; j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }
        for (int i = 108; i < 132; i++) {
            for (int j = 25; j < 46; j++) {
                this.block_graph.put(new Integer(i), new Integer(j));
                //System.out.printf("%d, %d\n", i, j);
            }
        }

        // right-bottom classrooms
        for (int j = 46; j < 48; j++) {
            this.block_graph.put(new Integer(118), new Integer(j));
            //System.out.printf("%d, %d\n", 118, j);
        }
        for (int j = 46; j < 48; j++) {
            this.block_graph.put(new Integer(119), new Integer(j));
            //System.out.printf("%d, %d\n", 119, j);
        }
        for (int j = 46; j < 52; j++) {
            this.block_graph.put(new Integer(120), new Integer(j));
            //System.out.printf("%d, %d\n", 120, j);
        }
        for (int j = 46; j < 56; j++) {
            this.block_graph.put(new Integer(121), new Integer(j));
            //System.out.printf("%d, %d\n", 121, j);
        }
        for (int j = 46; j < 60; j++) {
            this.block_graph.put(new Integer(122), new Integer(j));
            //System.out.printf("%d, %d\n", 122, j);
        }
        for (int j = 46; j < 64; j++) {
            this.block_graph.put(new Integer(123), new Integer(j));
            //System.out.printf("%d, %d\n", 123, j);
        }
        for (int j = 46; j < 68; j++) {
            this.block_graph.put(new Integer(124), new Integer(j));
            //System.out.printf("%d, %d\n", 124, j);
        }
        for (int j = 46; j < 72; j++) {
            this.block_graph.put(new Integer(125), new Integer(j));
            //System.out.printf("%d, %d\n", 125, j);
        }
        for (int j = 46; j < 76; j++) {
            this.block_graph.put(new Integer(126), new Integer(j));
            //System.out.printf("%d, %d\n", 126, j);
        }
        for (int j = 46; j < 80; j++) {
            this.block_graph.put(new Integer(127), new Integer(j));
            //System.out.printf("%d, %d\n", 127, j);
        }
        for (int j = 46; j < 84; j++) {
            this.block_graph.put(new Integer(128), new Integer(j));
            //System.out.printf("%d, %d\n", 128, j);
        }
        for (int j = 46; j < 88; j++) {
            this.block_graph.put(new Integer(129), new Integer(j));
            //System.out.printf("%d, %d\n", 129, j);
        }
        for (int j = 46; j < 90; j++) {
            this.block_graph.put(new Integer(130), new Integer(j));
            //System.out.printf("%d, %d\n", 130, j);
        }
        for (int j = 46; j < 94; j++) {
            this.block_graph.put(new Integer(131), new Integer(j));
            //System.out.printf("%d, %d\n", 131, j);
        }


        // right-bottom classrooms' borders
        this.block_graph.put(new Integer(92), new Integer(56));
        this.block_graph.put(new Integer(93), new Integer(56));
        this.block_graph.put(new Integer(94), new Integer(56));
        this.block_graph.put(new Integer(95), new Integer(55));
        this.block_graph.put(new Integer(96), new Integer(55));
        this.block_graph.put(new Integer(97), new Integer(55));
        this.block_graph.put(new Integer(98), new Integer(54));
        this.block_graph.put(new Integer(99), new Integer(54));
        this.block_graph.put(new Integer(100), new Integer(54));
        this.block_graph.put(new Integer(101), new Integer(53));
        this.block_graph.put(new Integer(102), new Integer(53));
        this.block_graph.put(new Integer(103), new Integer(53));
        this.block_graph.put(new Integer(104), new Integer(52));
        this.block_graph.put(new Integer(105), new Integer(52));
        this.block_graph.put(new Integer(106), new Integer(52));
        this.block_graph.put(new Integer(107), new Integer(51));
        this.block_graph.put(new Integer(108), new Integer(51));
        this.block_graph.put(new Integer(109), new Integer(51));
        this.block_graph.put(new Integer(110), new Integer(50));
        this.block_graph.put(new Integer(111), new Integer(50));
        this.block_graph.put(new Integer(112), new Integer(50));
        this.block_graph.put(new Integer(113), new Integer(49));
        this.block_graph.put(new Integer(114), new Integer(49));
        this.block_graph.put(new Integer(115), new Integer(49));
        this.block_graph.put(new Integer(116), new Integer(48));
        this.block_graph.put(new Integer(117), new Integer(48));

        this.block_graph.put(new Integer(98), new Integer(81));
        this.block_graph.put(new Integer(99), new Integer(81));
        this.block_graph.put(new Integer(100), new Integer(80));
        this.block_graph.put(new Integer(101), new Integer(80));
        this.block_graph.put(new Integer(102), new Integer(80));
        this.block_graph.put(new Integer(103), new Integer(79));
        this.block_graph.put(new Integer(104), new Integer(79));
        this.block_graph.put(new Integer(105), new Integer(79));
        this.block_graph.put(new Integer(106), new Integer(78));
        this.block_graph.put(new Integer(107), new Integer(78));
        this.block_graph.put(new Integer(108), new Integer(78));
        this.block_graph.put(new Integer(109), new Integer(77));
        this.block_graph.put(new Integer(110), new Integer(77));
        this.block_graph.put(new Integer(111), new Integer(77));
        this.block_graph.put(new Integer(112), new Integer(76));
        this.block_graph.put(new Integer(113), new Integer(76));
        this.block_graph.put(new Integer(114), new Integer(76));
        this.block_graph.put(new Integer(115), new Integer(75));
        this.block_graph.put(new Integer(116), new Integer(75));
        this.block_graph.put(new Integer(117), new Integer(75));
        this.block_graph.put(new Integer(118), new Integer(74));
        this.block_graph.put(new Integer(119), new Integer(74));
        this.block_graph.put(new Integer(120), new Integer(74));
        this.block_graph.put(new Integer(121), new Integer(73));
        this.block_graph.put(new Integer(122), new Integer(73));
        this.block_graph.put(new Integer(123), new Integer(73));

        this.block_graph.put(new Integer(93), new Integer(58));
        this.block_graph.put(new Integer(93), new Integer(59));
        this.block_graph.put(new Integer(94), new Integer(60));
        this.block_graph.put(new Integer(94), new Integer(61));
        this.block_graph.put(new Integer(94), new Integer(62));
        this.block_graph.put(new Integer(94), new Integer(63));
        this.block_graph.put(new Integer(95), new Integer(64));
        this.block_graph.put(new Integer(95), new Integer(65));
        this.block_graph.put(new Integer(95), new Integer(66));
        this.block_graph.put(new Integer(95), new Integer(67));
        this.block_graph.put(new Integer(96), new Integer(68));
        this.block_graph.put(new Integer(96), new Integer(69));
        this.block_graph.put(new Integer(96), new Integer(70));
        this.block_graph.put(new Integer(96), new Integer(71));
        this.block_graph.put(new Integer(97), new Integer(72));
        this.block_graph.put(new Integer(97), new Integer(73));
        this.block_graph.put(new Integer(97), new Integer(74));
        this.block_graph.put(new Integer(97), new Integer(75));

        this.block_graph.put(new Integer(99), new Integer(84));
        this.block_graph.put(new Integer(99), new Integer(85));
        this.block_graph.put(new Integer(99), new Integer(86));
        this.block_graph.put(new Integer(99), new Integer(87));
        this.block_graph.put(new Integer(100), new Integer(88));
        this.block_graph.put(new Integer(100), new Integer(89));
        this.block_graph.put(new Integer(100), new Integer(90));
        this.block_graph.put(new Integer(100), new Integer(92));
        this.block_graph.put(new Integer(101), new Integer(93));
        this.block_graph.put(new Integer(101), new Integer(94));
        this.block_graph.put(new Integer(101), new Integer(95));
        this.block_graph.put(new Integer(102), new Integer(96));
        this.block_graph.put(new Integer(102), new Integer(97));
        this.block_graph.put(new Integer(102), new Integer(98));
        this.block_graph.put(new Integer(103), new Integer(99));
        this.block_graph.put(new Integer(103), new Integer(100));
        this.block_graph.put(new Integer(103), new Integer(101));

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

    public static void AStarAlgorithm() {
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
                updateCost(current, temp, current.finalCost + 2);

                if( (current.y -1) >= 0) {
                    temp = graph[current.x-1][current.y-1];
                    // 15 is the fixed diagonal cost
                    updateCost(current, temp, current.finalCost + 4);
                }

                if (current.y+1 < graph[0].length) {
                    temp = graph[current.x-1][current.y+1];
                    updateCost(current, temp, current.finalCost + 4);
                }
            }

            if ((current.y -1) >= 0) {
                temp = graph[current.x][current.y-1];
                updateCost(current, temp, current.finalCost + 2);
            }

            if ((current.y + 1) < graph[0].length ) {
                temp = graph[current.x][current.y + 1];
                updateCost(current, temp, current.finalCost + 2);
            }

            if ( (current.x + 1) < graph.length) {
                temp = graph[current.x+1][current.y];
                updateCost(current, temp, current.finalCost + 2);

                if( (current.y -1) >= 0) {
                    temp = graph[current.x+1][current.y-1];
                    // 15 is the fixed diagonal cost
                    updateCost(current, temp, current.finalCost + 4);
                }

                if (current.y+1 < graph[0].length) {
                    temp = graph[current.x+1][current.y+1];
                    updateCost(current, temp, current.finalCost + 4);
                }
            }

        }
    }

    public static Map<Integer, Integer> executeAStar(int x, int y, int startX, int startY, int endX, int endY) {
        //System.out.println("start A* algorithm...\n");

        graph = new Avertex[x][y];
        closedList = new boolean[x][y];

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

        // set blocked vertices
        for (Map.Entry<Integer, Integer> entry : block_graph.entrySet())
        {
            // TODO: not sure about the type of the data
            setBlocked(entry.getKey(), entry.getValue());
        }

        // run algorithm
        AStarAlgorithm();

        // print out the scores for cells
        //System.out.println("\nScores: \n");
        for (int i = 0; i< x; ++i) {
            for (int j = 0;j<y;++j) {
                if (graph[i][j] != null){
                    //System.out.printf("%-3d ", graph[i][j].finalCost);
                } else {
                    //System.out.print("BL ");
                }
            }
            //System.out.println();
        }
        //System.out.println();

        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        // trace back the path
        if (closedList[endX][endY]) {
            Avertex current = graph[endX][endY];
            result.put(current.x, current.y);
            while(current.parent != null) {
                result.put(current.parent.x, current.parent.y);
                current = current.parent;
            }
        }

        return result;
    }

}
