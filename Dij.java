package origin_daoh;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Dij{
    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, Down, Left, Right
    private static int[][] valueBytes;
    static {
        int N=RedisWR.getLength();
        valueBytes=new int[N][N];
    }

    public static List<String> findRoute(int[][] map, int startX, int startY, int targetX, int targetY) throws InterruptedException {
        valueBytes=RedisWR.getMap("Bitmap2");
        System.out.println("Cal doing........................");
        int n = map.length;
        Node[][] node=new Node[n][n];
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                node[i][j]=new Node();
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(nn -> nn.getF()));
        boolean[][] visited = new boolean[n][n];

        Node startNode=node[startX][startY];
        startNode.setNode(startX, startY, 0, heuristic(startX, startY, targetX, targetY), null);
        openSet.offer(startNode);
       // int i=1;
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            int x = current.x;
            int y = current.y;

            if (x == targetX && y == targetY) {
                return buildPath(current);
            }

            //System.out.println("x: "+x+" , y: "+y);
            visited[x][y] = true;
            for (int[] dir : DIRS) {
                int newX = x + dir[0];
                int newY = y + dir[1];
                if (newX >= 0 && newX < n && newY >= 0 && newY < n && !visited[newX][newY]) {
                    //System.out.println(newX+"   "+newY);

                    //int gScore = current.g + ( (map[newX][newY]==1) ? (2000+((valueBytes[newX][newY]==1)?4929 : 0)) : 0 );
                    //int gScore = current.g + ((map[newX][newY] == 1 || valueBytes[newX][newY] == 1) ? 4929 : 0);
                    int additionalCost = 0;
                    if (map[newX][newY] == 1) {
                        additionalCost += 50;
                    }
                    if (valueBytes[newX][newY] == 1&&map[newX][newY]==1) {
                        additionalCost =10000; // 确保同时为1时总增加为10000
                    }
                    int gScore = current.g + additionalCost;

                   // int hScore = heuristic(newX, newY, targetX, targetY);
                    int hScore=0;
                   // Node neighbor = new Node(newX, newY, gScore, hScore, current);
                    if (!openSet.contains(node[newX][newY])) {
                        node[newX][newY].setNode(newX, newY, gScore, hScore, current);
                        openSet.offer(node[newX][newY]);
                    } else if (gScore < node[newX][newY].g) {
                        openSet.remove(node[newX][newY]);
                        node[newX][newY].setNode(newX, newY, gScore, hScore, current);
                        openSet.offer(node[newX][newY]);
                    }
                }
            }

            try {
                Thread.sleep(1);
            }catch (Exception e) {
                // TODO: handle exception
            }
        }

        return null; // No path found
    }

    public static int heuristic(int x1, int y1, int x2, int y2) {
        int l1=Math.abs(x1 - x2);
        int l2=Math.abs(y1 - y2);
        return (l1 + l2)/2;
    }

    public static List<String> buildPath(Node node) {
        List<String> path = new ArrayList<>();
        while (node != null && node.parent != null) {
            int dx = node.x - node.parent.x;
            int dy = node.y - node.parent.y;

            if (dx == -1) {
                path.add("U");
            } else if (dx == 1) {
                path.add("D");
            } else if (dy == -1) {
                path.add("L");
            } else if (dy == 1) {
                path.add("R");
            }

            node = node.parent;
        }

        Collections.reverse(path);
        return path;
    }
}