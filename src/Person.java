import java.util.List;

public class Person {

    private String id;
    private Cell currentLocation;
    private Shelter assignedShelter;
    private Cell alternativeGoalCell;
    private PersonStatus status;
    private List<Cell> path;
    private int pathIndex;


    public Person(String id) {
        setId(id);
        setCurrentLocation(null);
        setAssignedShelter(null);
        setAlternativeGoalCell(null);
        setStatus(PersonStatus.IDLE);
        setPath(null);
        setPathIndex(-1);

    }

    public Shelter getAssignedShelter() {
        return assignedShelter;
    }

    public void setAssignedShelter(Shelter assignedShelter) {
        if (assignedShelter == null){
            setStatus(PersonStatus.UNASSIGNED);
        }
        else{setStatus(PersonStatus.ASSIGNED);}
        this.assignedShelter = assignedShelter;
    }

    public Cell getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Cell currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Cell getAlternativeGoalCell() {
        return alternativeGoalCell;
    }

    public void setAlternativeGoalCell(Cell alternativeGoalCell) {
        this.alternativeGoalCell = alternativeGoalCell;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Cell> getPath() {
        return path;
    }

    public void setPath(List<Cell> path) {
        this.path = path;
        //Correct route
        if(this.path !=null && (!this.path.isEmpty())){
            this.pathIndex = 0;
            if (this.status == PersonStatus.ASSIGNED) {
                this.status = PersonStatus.MOVING;
            }
        }
        //Invalid route
        else {
            this.pathIndex = -1;
            if (this.status == PersonStatus.ASSIGNED) {
                this.status = PersonStatus.STUCK;

            }

        }
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(int pathIndex) {
        this.pathIndex = pathIndex;
    }

    public PersonStatus getStatus() {
        return status;
    }

    public void setStatus(PersonStatus status) {
        this.status = status;
    }

    public Cell getNextStepInPath() {
        if (isPathAssigned() && pathIndex < path.size() - 1) { // אם יש נתיב ויש עוד צעד ללכת
            return path.get(pathIndex + 1); // החזר את התא הבא בנתיב
        }
        return null; // אין צעד הבא
    }

    public void advanceOnPath() {
        if (isPathAssigned() && pathIndex < path.size() - 1) {
            pathIndex++; // קדם את האינדקס לצעד הבא
            setCurrentLocation(path.get(pathIndex)); // עדכן את המיקום הנוכחי

            if (isPathFinished()) { // בדוק אם הגיע לסוף הנתיב
                setStatus(PersonStatus.REACHED_SHELTER);
            }
        }
    }

    //A route is defined (assigned) only if it is neither null nor empty.
    public boolean isPathAssigned(){
        return this.path !=null && !this.path.isEmpty();

    }

    public boolean isPathFinished(){
        if(this.path==null || this.path.isEmpty()){
            return false;
        }

        return this.pathIndex==this.path.size()-1;
    }


}