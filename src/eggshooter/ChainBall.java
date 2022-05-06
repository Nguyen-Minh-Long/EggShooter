package eggshooter;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Group Javascript
 * Leader: Nguyen Vuong Khang Hy
 * Tester: Nguyen Minh Long
 * Designer: Nguyen Tan Vu
 * Class IA1401
 * Game Egg Shooter
 */
public class ChainBall {
    // declare array row
    private int[] row;
    // declare array column
    private int[] col;
    // declare number of row
    private final int R;
    // declare number of column
    private final int C;
    // declare number of counting ball area
    private int countArea;

    public ChainBall(int R, int C) {
        this.R = R;
        this.C = C;
        this.countArea = 1;
    }

    class pair {

        int first, second;

        public pair(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    /**
     * A function to check if a given cell  
     * (u, v) can be included in DFS  
     * @param mat
     * @param i
     * @param j
     * @param visited
     * @param color
     * @return 
     */
    public boolean isSafe(int mat[][], int i, int j, int visited[][], int color) {
        return (i >= 0) && (i < R)
                && (j >= 0) && (j < C)
                && (mat[i][j] == color && visited[i][j] == 0);
    }
    /**
     * Breath first search
     * @param mat
     * @param visited
     * @param x
     * @param y
     * @param color 
     */
    public void BFS(int mat[][], int visited[][], int x, int y, int color) {

        // These arrays are used to get row and  
        // column numbers of 8 neighbours of  
        // a given cell  
        // Simple BFS first step, we enqueue  
        // source and mark it as visitedited  
        Queue<pair> q = new LinkedList<>();
        q.add(new pair(x, y));
        visited[x][y] = 1;

        // Next step of BFS. We take out  
        // items one by one from queue and  
        // enqueue their univisitedited adjacent  
        while (!q.isEmpty()) {

            int i = q.peek().first;
            int j = q.peek().second;
            q.remove();

            // Go through all 8 adjacent  
            if (i % 2 == 0) {
                row = new int[]{-1, 0, 0, 1, -1, 1};
                col = new int[]{0, -1, 1, 0, 1, 1};
            } else {
                row = new int[]{-1, 0, 0, 1, -1, 1};
                col = new int[]{0, -1, 1, 0, -1, -1};
            }
            for (int k = 0; k < 6; k++) {
                if (isSafe(mat, i + row[k], j + col[k], visited, color)) {
                    visited[i + row[k]][j + col[k]] = 1;
                    q.add(new pair(i + row[k], j + col[k]));
                    countArea++;
                }
            }
        }
    }

    /**
     * This function returns number islands (connected  
     * components) in a graph. It simply works as  
     * BFS for disconnected graph and returns count  
     * of BFS calls.  
     * @param mat
     * @param color
     * @param x
     * @param y
     * @return 
     */
    public int[][] countIslands(int mat[][], int color, int x, int y) {
        setCountArea(1);
        // Mark all cells as not visitedited 
        int[][] visited = new int[R][C];
        BFS(mat, visited, x, y, color);
        return visited;
    }
    /**
     * getter 
     * @return number of counting ball area
     */
    public int getCountArea() {
        return countArea;
    }
    /**
     * setter
     * @param countArea number of counting ball area
     */
    public void setCountArea(int countArea) {
        this.countArea = countArea;
    }

    public static void main(String[] args) {
        int mat[][] = {{2, 3, 2, 3, 1, 3, 3, 3, 2},
        {2, 2, 3, 3, 1, 1, 2, 2, 1},
        {1, 3, 3, 1, 1, 1, 2, 1, 2},
        {3, 3, 2, 1, 3, 2, 3, 2, 2},
        {1, 1, 1, 2, 1, 3, 1, 3, 3},
        {1, 3, 3, 3, 3, 3, 1, 2, 3},
        {1, 2, 1, 1, 1, 3, 1, 1, 2},
        {2, 3, 2, 3, 3, 3, 1, 2, 3},
        {3, 3, 3, 2, 2, 1, 2, 3, 3},
        {1, 3, 2, 2, 3, 1, 2, 1, 1},
        {1, 1, 2, 3, 2, 2, 1, 2, 2},
        {3, 2, 2, 2, 3, 2, 1, 2, 3},
        {2, 2, 1, 1, 2, 1, 2, 3, 1},
        {1, 2, 1, 1, 2, 3, 1, 2, 2},
        {3, 2, 2, 2, 2, 1, 1, 2, 2},
        {2, 3, 3, 2, 2, 1, 3, 2, 3},
        {3, 0, 3, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0}};
        ChainBall obj = new ChainBall(30, 9);
        for (int[] is : (obj.countIslands(mat, 3, 16, 2))) {
            for (int i : is) {
                System.out.print(i);
            }
            System.out.println("");
        }
        System.out.println(obj.getCountArea());
    }
}
