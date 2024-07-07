package origin_daoh;

public class Node {
    int x, y;
    int g, h;
    Node parent;

    public Node(int x, int y, int g, int h, Node parent) {
        this.x = x;
        this.y = y;
        this.g = g;
        this.h = h;
        this.parent = parent;
    }

    public Node() {
		// TODO Auto-generated constructor stub
	}

	public int getF() {
        return g + h;
    }

	public void setNode(int x, int y, int g, int h,  Node parent) {
		// TODO Auto-generated method stub
		this.x = x;
        this.y = y;
        this.g = g;
        this.h = h;
        this.parent = parent;
	}
    @Override
    public String toString() {
        return "Node{" +
                "x=" + x +
                ", y=" + y +
                ", g=" + g +
                ", h=" + h +
                ", parent=" + (parent != null ? "(" + parent.x + "," + parent.y + ")" : "null") +
                '}';
    }

}
