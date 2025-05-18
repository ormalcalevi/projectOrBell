import java.util.List;

public class Person {

    private String id;
    private Cell currentLocation;
    private Shelter assignedShelter;
    private PersonStatus status;
    private List<Cell> path;
    private int pathIndex=-1;

    public Person(String id) {
        setId(id);
        setCurrentLocation(null);
        setAssignedShelter(null);
        setStatus(PersonStatus.IDLE);
        setPath(null);

    }

    public Shelter getAssignedShelter() {
        return assignedShelter;
    }

    public void setAssignedShelter(Shelter assignedShelter) {
        if (assignedShelter==null){
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

    public Cell getNextStepInPath(){
        if(this.path==null
                || this.path.isEmpty()
                || this.pathIndex < 0
                || this.pathIndex >= this.path.size()){
            return null;
        }
        return this.path.get(getPathIndex());
    }

    public void advanceOnPath(){
        if(this.path == null || this.path.isEmpty()){
            return;
        }
        if(this.pathIndex >= this.path.size()-1){
            if (status != PersonStatus.REACHED_SHELTER) {
                setStatus(PersonStatus.REACHED_SHELTER);
            }
            return;
        }
        this.pathIndex++ ;
        setCurrentLocation(this.path.get(getPathIndex()));
        if(this.pathIndex==this.path.size()-1){
            setStatus(PersonStatus.REACHED_SHELTER);
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

    // דוגמה ל-toString אפשרית
    @Override
    public String toString() {
        String locationStr = (currentLocation != null) ? "(" + currentLocation.getRow() + "," + currentLocation.getCol() + ")" : "Not placed";
        String shelterStr = (assignedShelter != null) ? assignedShelter.getId() : "None";
        return "Person " + id + " at " + locationStr + " | Status: " + status + " | Target: " + shelterStr;
    }

}
