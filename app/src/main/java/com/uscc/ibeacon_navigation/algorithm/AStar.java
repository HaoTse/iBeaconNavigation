package com.uscc.ibeacon_navigation.algorithm;

import android.util.Log;

import com.google.common.collect.HashMultimap;

import java.util.HashMap;
import java.util.PriorityQueue;

public class AStar {

    public static final int DIAGONAL_COST = 7;
    public static final int VH_COST = 5;

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
        // initializeBlockGraph();
        initBlock();
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

    // new initialization function
    private static void initBlock() {
        // 左邊牆壁
        for (int j= 20; j < graphY; j++) {
            graph[5][j] = null;
        }
        for (int j= 24; j < graphY; j++) {
            graph[6][j] = null;
        }
        for (int j= 28; j < graphY; j++) {
            graph[7][j] = null;
        }
        for (int j= 32; j < graphY; j++) {
            graph[8][j] = null;
        }
        for (int j= 36; j < graphY; j++) {
            graph[9][j] = null;
        }
        for (int j= 40; j < graphY; j++) {
            graph[10][j] = null;
        }
        for (int j= 44; j < graphY; j++) {
            graph[11][j] = null;
        }
        for (int j= 48; j < graphY; j++) {
            graph[12][j] = null;
        }
        for (int j= 52; j < graphY; j++) {
            graph[13][j] = null;
        }
        for (int j= 56; j < graphY; j++) {
            graph[14][j] = null;
        }
        for (int j= 60; j < graphY; j++) {
            graph[15][j] = null;
        }
        for (int j= 64; j < graphY; j++) {
            graph[16][j] = null;
        }
        for (int j= 68; j < graphY; j++) {
            graph[17][j] = null;
        }
        for (int j= 72; j < graphY; j++) {
            graph[18][j] = null;
        }
        for (int j= 76; j < graphY; j++) {
            graph[19][j] = null;
        }
        for (int j= 80; j < graphY; j++) {
            graph[20][j] = null;
        }

        // 下方牆壁
        for (int i = 23; i< 32; i++) {
            graph[i][graphY - 2] = null;
            graph[i][graphY - 1] = null;
        }

        // 教室上方牆壁
        graph[17][36] = null;
        graph[17][35] = null;
        graph[18][36] = null;
        graph[18][35] = null;
        graph[19][36] = null;
        graph[19][35] = null;
        graph[20][35] = null;
        graph[20][34] = null;
        graph[21][35] = null;
        graph[21][34] = null;
        graph[22][34] = null;
        graph[22][33] = null;
        graph[23][34] = null;
        graph[23][33] = null;
        graph[24][34] = null;
        graph[24][33] = null;
        graph[25][33] = null;
        graph[25][32] = null;
        graph[26][33] = null;
        graph[26][32] = null;
        graph[27][33] = null;
        graph[27][32] = null;
        graph[28][32] = null;
        graph[28][31] = null;
        graph[29][32] = null;
        graph[29][31] = null;
        graph[30][31] = null;
        graph[30][30] = null;
        graph[31][31] = null;
        graph[31][30] = null;
        graph[32][30] = null;
        graph[32][29] = null;
        graph[33][30] = null;
        graph[33][29] = null;
        graph[34][30] = null;
        graph[34][29] = null;
        graph[35][29] = null;
        graph[35][28] = null;
        graph[36][29] = null;
        graph[36][28] = null;
        graph[37][29] = null;
        graph[37][28] = null;
        graph[38][28] = null;
        graph[38][27] = null;
        graph[39][28] = null;
        graph[39][27] = null;
        graph[40][28] = null;
        graph[40][27] = null;
        graph[41][27] = null;
        graph[41][26] = null;
        graph[42][27] = null;
        graph[42][26] = null;
        graph[43][27] = null;
        graph[43][26] = null;
        graph[44][26] = null;
        graph[44][25] = null;
        graph[45][26] = null;
        graph[45][25] = null;

        // 教室左邊1
        graph[18][37] = null;
        graph[18][38] = null;
        graph[18][39] = null;
        graph[18][40] = null;
        graph[19][41] = null;
        graph[19][42] = null;
        graph[19][43] = null;
        graph[19][44] = null;
        graph[20][45] = null;
        graph[20][46] = null;
        graph[20][47] = null;
        graph[21][48] = null;
        graph[21][49] = null;
        graph[21][50] = null;
        graph[21][51] = null;
        graph[22][52] = null;
        graph[22][53] = null;
        graph[22][54] = null;
        graph[23][55] = null;
        graph[23][56] = null;
        graph[23][57] = null;

        // 教室中間間隔
        graph[24][60] = null;
        graph[24][59] = null;
        graph[25][60] = null;
        graph[25][59] = null;
        graph[26][60] = null;
        graph[26][59] = null;
        graph[27][60] = null;
        graph[27][59] = null;
        graph[28][60] = null;
        graph[28][59] = null;
        graph[29][59] = null;
        graph[29][58] = null;
        graph[30][59] = null;
        graph[30][58] = null;
        graph[31][59] = null;
        graph[31][58] = null;
        graph[32][59] = null;
        graph[32][58] = null;
        graph[33][58] = null;
        graph[33][57] = null;
        graph[34][58] = null;
        graph[34][57] = null;
        graph[35][58] = null;
        graph[35][57] = null;
        graph[36][58] = null;
        graph[36][57] = null;
        graph[37][57] = null;
        graph[37][56] = null;
        graph[38][57] = null;
        graph[38][56] = null;
        graph[39][57] = null;
        graph[39][56] = null;
        graph[40][57] = null;
        graph[40][56] = null;
        graph[41][56] = null;
        graph[41][55] = null;
        graph[42][56] = null;
        graph[42][55] = null;
        graph[43][56] = null;
        graph[43][55] = null;
        graph[44][55] = null;
        graph[44][54] = null;
        graph[45][55] = null;
        graph[45][54] = null;
        graph[46][55] = null;
        graph[46][54] = null;
        graph[47][54] = null;
        graph[47][53] = null;
        graph[48][54] = null;
        graph[48][53] = null;
        graph[49][54] = null;
        graph[49][53] = null;
        graph[50][53] = null;
        graph[50][52] = null;
        graph[51][53] = null;
        graph[51][52] = null;
        graph[52][53] = null;
        graph[52][52] = null;

        // 教室左邊2
        graph[25][64] = null;
        graph[25][65] = null;
        graph[25][66] = null;
        graph[26][67] = null;
        graph[26][68] = null;
        graph[26][69] = null;
        graph[27][70] = null;
        graph[27][71] = null;
        graph[27][72] = null;
        graph[27][73] = null;
        graph[28][74] = null;
        graph[28][75] = null;
        graph[28][76] = null;
        graph[29][77] = null;
        graph[29][78] = null;
        graph[29][79] = null;
        graph[30][80] = null;
        graph[30][81] = null;
        graph[30][82] = null;
        graph[30][83] = null;

        // 門1
        graph[34][0] = null;
        graph[34][1] = null;
        graph[34][2] = null;
        graph[34][3] = null;
        graph[35][2] = null;
        graph[35][3] = null;
        graph[35][4] = null;
        graph[35][5] = null;
        graph[36][5] = null;
        graph[36][6] = null;
        graph[36][7] = null;
        graph[34][8] = null;
        graph[37][8] = null;
        graph[37][9] = null;
        graph[37][10] = null;

        // 門2
        graph[37][15] = null;
        graph[37][16] = null;
        graph[37][17] = null;
        graph[38][17] = null;
        graph[38][18] = null;
        graph[38][19] = null;
        graph[39][19] = null;
        graph[39][20] = null;
        graph[39][21] = null;
        graph[40][21] = null;
        graph[40][22] = null;
        graph[40][23] = null;
    }


    private static void initializeBlockGraph() {
        // left bottom corner
        for(int i = 0; i< 87; i++) {
            for (int j = 50; j< graphY ;j++) {
                block_graph.put(new Integer(i), new Integer(j));
            }
        }

        // that corner and the rectangular
        for (int i = 73; i< 86;i++) {
            for (int j = 47; j < 50; j++) {
                block_graph.put(new Integer(i), new Integer(j));
            }
        }

        // upper part bar
        for (int i = 7; i < graphX; i++) {
            for (int j = 0; j< 4; j++) {
                block_graph.put(new Integer(i), new Integer(j));
            }
        }

        // middle bottom triangle
        for (int i = 87; i < 104; i++) {
            for (int j = 50; j< 117; j = j + 4) {
                block_graph.put(new Integer(i), new Integer(j));
            }
        }

        // borders for top-left classrooms
        for (int j = 4; j < 36; j++) {
            block_graph.put(new Integer(7), new Integer(j));
            //System.out.printf("%d, %d\n", 6, j);
        }
        for (int j = 4; j < 36; j++) {
            block_graph.put(new Integer(34), new Integer(j));
            //System.out.printf("%d, %d\n", 32, j);
        }
        for (int j = 4; j < 36; j++) {
            block_graph.put(new Integer(62), new Integer(j));
            //System.out.printf("%d, %d\n", 58, j);
        }
        for (int i = 10; i < 31; i++) {
            block_graph.put(new Integer(i), new Integer(36));
        }
        for (int i = 38; i < 59; i++) {
            block_graph.put(new Integer(i), new Integer(36));
        }

        block_graph.put(new Integer(99), new Integer(61));
        block_graph.put(new Integer(99), new Integer(60));
        block_graph.put(new Integer(100), new Integer(61));
        block_graph.put(new Integer(100), new Integer(60));
        block_graph.put(new Integer(101), new Integer(61));
        block_graph.put(new Integer(101), new Integer(60));
        block_graph.put(new Integer(102), new Integer(61));
        block_graph.put(new Integer(102), new Integer(60));
        block_graph.put(new Integer(103), new Integer(60));
        block_graph.put(new Integer(103), new Integer(59));
        block_graph.put(new Integer(104), new Integer(60));
        block_graph.put(new Integer(104), new Integer(59));
        block_graph.put(new Integer(105), new Integer(60));
        block_graph.put(new Integer(105), new Integer(59));
        block_graph.put(new Integer(104), new Integer(60));
        block_graph.put(new Integer(105), new Integer(59));
        block_graph.put(new Integer(105), new Integer(59));
        block_graph.put(new Integer(104), new Integer(58));
        block_graph.put(new Integer(105), new Integer(59));
        block_graph.put(new Integer(105), new Integer(58));
        block_graph.put(new Integer(104), new Integer(59));
        block_graph.put(new Integer(105), new Integer(58));
        block_graph.put(new Integer(105), new Integer(58));
        block_graph.put(new Integer(104), new Integer(57));
        block_graph.put(new Integer(105), new Integer(58));
        block_graph.put(new Integer(105), new Integer(57));
        block_graph.put(new Integer(106), new Integer(58));
        block_graph.put(new Integer(106), new Integer(57));
        block_graph.put(new Integer(107), new Integer(58));
        block_graph.put(new Integer(107), new Integer(57));
        block_graph.put(new Integer(108), new Integer(58));
        block_graph.put(new Integer(108), new Integer(57));
        block_graph.put(new Integer(109), new Integer(57));
        block_graph.put(new Integer(109), new Integer(56));
        block_graph.put(new Integer(110), new Integer(57));
        block_graph.put(new Integer(110), new Integer(56));
        block_graph.put(new Integer(111), new Integer(57));
        block_graph.put(new Integer(111), new Integer(56));
        block_graph.put(new Integer(112), new Integer(57));
        block_graph.put(new Integer(112), new Integer(56));
        block_graph.put(new Integer(113), new Integer(56));
        block_graph.put(new Integer(113), new Integer(55));
        block_graph.put(new Integer(114), new Integer(56));
        block_graph.put(new Integer(114), new Integer(55));
        block_graph.put(new Integer(115), new Integer(56));
        block_graph.put(new Integer(115), new Integer(55));
        block_graph.put(new Integer(116), new Integer(55));
        block_graph.put(new Integer(116), new Integer(54));
        block_graph.put(new Integer(117), new Integer(55));
        block_graph.put(new Integer(117), new Integer(54));
        block_graph.put(new Integer(118), new Integer(55));
        block_graph.put(new Integer(118), new Integer(54));
        block_graph.put(new Integer(119), new Integer(55));
        block_graph.put(new Integer(119), new Integer(54));
        block_graph.put(new Integer(120), new Integer(54));
        block_graph.put(new Integer(120), new Integer(53));
        block_graph.put(new Integer(121), new Integer(54));
        block_graph.put(new Integer(121), new Integer(53));
        block_graph.put(new Integer(122), new Integer(54));
        block_graph.put(new Integer(122), new Integer(53));
        block_graph.put(new Integer(123), new Integer(54));
        block_graph.put(new Integer(123), new Integer(53));
        block_graph.put(new Integer(124), new Integer(54));
        block_graph.put(new Integer(124), new Integer(53));
        block_graph.put(new Integer(125), new Integer(54));
        block_graph.put(new Integer(125), new Integer(53));
        block_graph.put(new Integer(126), new Integer(54));
        block_graph.put(new Integer(126), new Integer(53));
        block_graph.put(new Integer(127), new Integer(54));
        block_graph.put(new Integer(127), new Integer(53));

        block_graph.put(new Integer(106), new Integer(87));
        block_graph.put(new Integer(106), new Integer(86));
        block_graph.put(new Integer(107), new Integer(87));
        block_graph.put(new Integer(107), new Integer(86));
        block_graph.put(new Integer(108), new Integer(87));
        block_graph.put(new Integer(108), new Integer(86));
        block_graph.put(new Integer(109), new Integer(87));
        block_graph.put(new Integer(109), new Integer(86));
        block_graph.put(new Integer(110), new Integer(86));
        block_graph.put(new Integer(110), new Integer(85));
        block_graph.put(new Integer(111), new Integer(86));
        block_graph.put(new Integer(111), new Integer(85));
        block_graph.put(new Integer(112), new Integer(86));
        block_graph.put(new Integer(112), new Integer(85));
        block_graph.put(new Integer(113), new Integer(86));
        block_graph.put(new Integer(113), new Integer(85));
        block_graph.put(new Integer(114), new Integer(85));
        block_graph.put(new Integer(114), new Integer(84));
        block_graph.put(new Integer(115), new Integer(85));
        block_graph.put(new Integer(115), new Integer(84));
        block_graph.put(new Integer(116), new Integer(85));
        block_graph.put(new Integer(116), new Integer(84));
        block_graph.put(new Integer(117), new Integer(85));
        block_graph.put(new Integer(117), new Integer(84));
        block_graph.put(new Integer(118), new Integer(84));
        block_graph.put(new Integer(118), new Integer(83));
        block_graph.put(new Integer(119), new Integer(84));
        block_graph.put(new Integer(119), new Integer(83));
        block_graph.put(new Integer(120), new Integer(84));
        block_graph.put(new Integer(120), new Integer(83));
        block_graph.put(new Integer(121), new Integer(84));
        block_graph.put(new Integer(121), new Integer(83));
        block_graph.put(new Integer(122), new Integer(83));
        block_graph.put(new Integer(122), new Integer(82));
        block_graph.put(new Integer(123), new Integer(83));
        block_graph.put(new Integer(123), new Integer(82));
        block_graph.put(new Integer(124), new Integer(83));
        block_graph.put(new Integer(124), new Integer(82));
        block_graph.put(new Integer(125), new Integer(83));
        block_graph.put(new Integer(125), new Integer(82));
        block_graph.put(new Integer(126), new Integer(82));
        block_graph.put(new Integer(126), new Integer(81));
        block_graph.put(new Integer(127), new Integer(82));
        block_graph.put(new Integer(127), new Integer(81));
        block_graph.put(new Integer(128), new Integer(82));
        block_graph.put(new Integer(128), new Integer(81));
        block_graph.put(new Integer(129), new Integer(82));
        block_graph.put(new Integer(129), new Integer(81));
        block_graph.put(new Integer(130), new Integer(82));
        block_graph.put(new Integer(130), new Integer(81));
        block_graph.put(new Integer(131), new Integer(81));
        block_graph.put(new Integer(131), new Integer(80));
        block_graph.put(new Integer(132), new Integer(81));
        block_graph.put(new Integer(132), new Integer(80));
        block_graph.put(new Integer(133), new Integer(81));
        block_graph.put(new Integer(133), new Integer(80));
        block_graph.put(new Integer(134), new Integer(81));
        block_graph.put(new Integer(134), new Integer(80));

        block_graph.put(new Integer(100), new Integer(65));
        block_graph.put(new Integer(100), new Integer(66));
        block_graph.put(new Integer(100), new Integer(67));
        block_graph.put(new Integer(101), new Integer(68));
        block_graph.put(new Integer(101), new Integer(69));
        block_graph.put(new Integer(101), new Integer(70));
        block_graph.put(new Integer(102), new Integer(71));
        block_graph.put(new Integer(102), new Integer(72));
        block_graph.put(new Integer(102), new Integer(73));
        block_graph.put(new Integer(103), new Integer(74));
        block_graph.put(new Integer(103), new Integer(75));
        block_graph.put(new Integer(103), new Integer(76));
        block_graph.put(new Integer(104), new Integer(77));
        block_graph.put(new Integer(104), new Integer(78));
        block_graph.put(new Integer(104), new Integer(79));
        block_graph.put(new Integer(105), new Integer(80));
        block_graph.put(new Integer(105), new Integer(81));
        block_graph.put(new Integer(105), new Integer(82));

        block_graph.put(new Integer(107), new Integer(91));
        block_graph.put(new Integer(107), new Integer(92));
        block_graph.put(new Integer(107), new Integer(93));
        block_graph.put(new Integer(107), new Integer(94));
        block_graph.put(new Integer(108), new Integer(95));
        block_graph.put(new Integer(108), new Integer(96));
        block_graph.put(new Integer(108), new Integer(97));
        block_graph.put(new Integer(109), new Integer(98));
        block_graph.put(new Integer(109), new Integer(98));
        block_graph.put(new Integer(109), new Integer(100));
        block_graph.put(new Integer(110), new Integer(101));
        block_graph.put(new Integer(110), new Integer(102));
        block_graph.put(new Integer(110), new Integer(103));
        block_graph.put(new Integer(111), new Integer(104));
        block_graph.put(new Integer(111), new Integer(105));
        block_graph.put(new Integer(111), new Integer(106));
        block_graph.put(new Integer(112), new Integer(107));
        block_graph.put(new Integer(112), new Integer(108));
        block_graph.put(new Integer(112), new Integer(109));
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
                updateCost(current, temp, current.finalCost + VH_COST);

                if( (current.y -1) >= 0) {
                    temp = graph[current.x-1][current.y-1];
                    // 15 is the fixed diagonal cost
                    updateCost(current, temp, current.finalCost + DIAGONAL_COST);
                }

                if (current.y+1 < graph[0].length) {
                    temp = graph[current.x-1][current.y+1];
                    updateCost(current, temp, current.finalCost + DIAGONAL_COST);
                }
            }

            if ((current.y -1) >= 0) {
                temp = graph[current.x][current.y-1];
                updateCost(current, temp, current.finalCost + VH_COST);
            }

            if ((current.y + 1) < graph[0].length ) {
                temp = graph[current.x][current.y + 1];
                updateCost(current, temp, current.finalCost + VH_COST);
            }

            if ( (current.x + 1) < graph.length) {
                temp = graph[current.x+1][current.y];
                updateCost(current, temp, current.finalCost + VH_COST);

                if( (current.y -1) >= 0) {
                    temp = graph[current.x+1][current.y-1];
                    // 15 is the fixed diagonal cost
                    updateCost(current, temp, current.finalCost + DIAGONAL_COST);
                }

                if (current.y+1 < graph[0].length) {
                    temp = graph[current.x+1][current.y+1];
                    updateCost(current, temp, current.finalCost + DIAGONAL_COST);
                }
            }

        }
    }

    public static HashMultimap<Integer, Integer> executeAStar(int startX, int startY, int endX, int endY) {
        graph = new Avertex[graphX][graphY];
        closedList = new boolean[graphX][graphY];
        openList = new PriorityQueue<>();
        // 0, 0 by default
        setStartVertex(startX, startY);
        setEndVertex(endX, endY);

        for (int i = 0; i< graphX; ++i) {
            for (int j = 0; j< graphY; ++j) {
                graph[i][j] = new Avertex(i, j);
                graph[i][j].heuristicCost = Math.abs(i - endX) + Math.abs(j - endY);
            }
        }
        // initialize final cost to 0
        graph[startX][startY].finalCost = 0;

        initBlock();

        // run algorithm
        AStarAlgorithm();

        System.out.println("\nScores for cells: ");
        for(int i=0;i<graphX;++i){
            for(int j=0;j<graphY;++j){
                if(i == startX && j == startY) System.out.print("src\t"); //Source
                else if(i == endX && j == endY) System.out.print("end\t");  //Destination
                if(graph[i][j]!=null)System.out.printf("%-3d\t", graph[i][j].finalCost);
                else System.out.print("BL\t");
            }
            System.out.println();
        }
        System.out.println();

        HashMultimap<Integer, Integer> result = HashMultimap.create();
        // trace back the path
        System.out.print("Score:\n");
        if (closedList[endX][endY]) {
            Avertex current = graph[endX][endY];
            result.put(current.x, current.y);
            System.out.print(current.x + " " + current.y);
            while(current.parent != null) {
                Log.e("s", " -> " + current.parent.x + "," + current.parent.y);
                result.put(current.parent.x, current.parent.y);
                current = current.parent;
            }
            return result;
        } else {
            Log.e("no path", "no path");
            return result;
        }
    }
}
