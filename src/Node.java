import java.util.Objects;

public class Node  implements Comparable<Node> {

    private  Cell cell;
    private  Node parent;
    private  int g;
    private  int f;
    private  int h;

    public Node(Cell cell, int g, int h, Node parent) {
        setCell(cell);
        setG(g);
        setH(h);
        setF(this.g+this.h);
        setParent(parent);
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    // Implement compareTo for PriorityQueue (sort by fScore, then by hScore as tie-breaker)
    @Override
    public int compareTo(Node other) {
        if (this.f < other.f) {
            return -1;
        }
        if (this.f  > other.f ) {
            return 1;
        }
        // If f s are equal, tie-break with h  (smaller h  is better)
        if (this.h  < other.h ) {
            return -1;
        }
        if (this.h  > other.h ) {
            return 1;
        }
        return 0; // Equal in terms of priority
    }

    // Implement equals and hashCode based on the cell for use in Sets/Maps
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(cell, node.cell);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cell);
    }
}
