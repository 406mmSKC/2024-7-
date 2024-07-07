package origin_daoh;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BFS {
    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, Down, Left, Right
    private static int[][] valueBytes;
    static {
        int N = RedisWR.getLength();
        valueBytes = new int[N][N];
    }
        public static List<String> findRouteBFS(int[][] map, int startX, int startY, int targetX, int targetY) throws InterruptedException {
            // ... 初始化部分保持不变 ...
            valueBytes = RedisWR.getMap("Bitmap2"); // 假设这个方法能正确获取并设置valueBytes
            System.out.println("Cal doing........................");
            int n = map.length;
            Node[][] node = new Node[n][n];
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    node[i][j] = new Node();

            // 使用LinkedHashSet替换LinkedList，以自动去除重复节点并保持插入顺序
            Set<Node> openSet = new LinkedHashSet<>();
            boolean[][] visited = new boolean[n][n];
            Node startNode = node[startX][startY];
            startNode.setNode(startX, startY, 0, 0, null);
            openSet.add(startNode); // 使用add方法代替offer

            while (!openSet.isEmpty()) {

                Iterator<Node> iterator = openSet.iterator();
                if (iterator.hasNext()) {
                    Node current = iterator.next();
                    iterator.remove(); // 移除当前节点，以模拟poll操作

                    int x = current.x;
                    int y = current.y;

                    if (x == targetX && y == targetY) {
                        System.out.println("the BFS path building complete");
                        return buildPath(current);
                    }


                    for (int[] dir : DIRS) {
                        int newX = x + dir[0];
                        int newY = y + dir[1];
                        if (newX >= 0 && newX < n && newY >= 0 && newY < n &&
                                !visited[newX][newY] && valueBytes[newX][newY] != 1) {
                            Node neighbor = new Node(newX, newY, current.g + 1, 0, current);
                            visited[newX][newY] = true;
                            openSet.add(neighbor);
                            //System.out.println("now x y = "+x+" "+y+" new x y = "+newX+" "+newY);
                        }
                    }
                }
            }

            return null; // No path found
        }



    public static int heuristic(int x1, int y1, int x2, int y2) {
        int l1 = Math.abs(x1 - x2);
        int l2 = Math.abs(y1 - y2);
        return (l1 + l2) / 2;
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
