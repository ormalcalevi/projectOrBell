import java.util.*;

public class PathFinderAStar {
    private GridMap map;

    public PathFinderAStar(GridMap map) {
        setMap(map);
    }

    public void setMap(GridMap map) {
        this.map = map;
    }

    public List<Cell> findPath(Cell startCell , Cell goalCell){
        if(startCell == null || goalCell == null || this.map == null || !startCell.isWalkable() || !goalCell.isWalkable()){
            return new ArrayList<>();
        }

        if(startCell.equals(goalCell)) {
            List<Cell>path=new ArrayList<>();
            path.add(startCell);
            return path;
        }


        PriorityQueue<Node> open=new PriorityQueue<>();
        Map<Cell,Node> close=new HashMap<>();

        Node startNode = new Node(startCell , 0 , manhattanDistance(startCell , goalCell) ,null);

        open.add(startNode);


        while(!open.isEmpty()){
            Node currentNode=open.poll();
            Cell currentCell=currentNode.getCell();

            if (!close.containsKey(currentCell)) {///If Cell is not on the closed list: insert
                close.put(currentCell, currentNode);
            }

            if( currentCell.equals(goalCell)){
                return(reverseParentsPath(currentNode));
            }

            List<Cell>neighborsListToCurrentCell=map.getWalkableNeighbors(currentCell);
            for (Cell neighbor : neighborsListToCurrentCell){
                if(!close.containsKey(neighbor)){

                    int potentialGScoreForNeighbor = currentNode.getG()+1;
                    Node neighborNode = new Node(neighbor,potentialGScoreForNeighbor,manhattanDistance(neighbor,goalCell),currentNode);
                    open.add(neighborNode);

                }
            }

        }

        return new ArrayList<>();
    }

    public List<Cell> reverseParentsPath(Node current){
        List<Cell> path=new ArrayList<>();

        while (current !=  null){
            path.add(current.getCell());
            current=current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private int manhattanDistance (Cell c1 , Cell c2){
        if(c1 == null || c2 == null){
            return Integer.MAX_VALUE;
        }
        return Math.abs(c1.getRow() - c2.getRow()) + Math.abs(c1.getCol() - c2.getCol());

    }
}
