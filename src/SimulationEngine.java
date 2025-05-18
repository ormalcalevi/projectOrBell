import java.util.List;
import java.util.Map;

public class SimulationEngine {
    private GridMap map ;
    private List<Person>personList;
    private List<Shelter> shelterList;
    private Map<Person,Shelter>assignment;
    private PathFinderAStar pathFinder;
    private int currentTimeStep;
    private int maxSimulationTime;
    private boolean simulationFinished;

    public SimulationEngine(GridMap map, List<Person> personList, List<Shelter> shelterList , int maxTime) {
        setMap(map);
        setPersonList(personList);
        setShelterList(shelterList);
        setMaxSimulationTime(maxTime);

        setPathFinder(new PathFinderAStar(this.map));
        setCurrentTimeStep(0);
        setSimulationFinished(false);

    }

    public Map<Person, Shelter> getAssignment() {
        return assignment;
    }

    public void setAssignment(Map<Person, Shelter> assignment) {
        this.assignment = assignment;
    }

    public int getCurrentTimeStep() {
        return currentTimeStep;
    }

    public void setCurrentTimeStep(int currentTimeStep) {
        this.currentTimeStep = currentTimeStep;
    }

    public GridMap getMap() {
        return map;
    }

    public void setMap(GridMap map) {
        this.map = map;
    }

    public int getMaxSimulationTime() {
        return maxSimulationTime;
    }

    public void setMaxSimulationTime(int maxSimulationTime) {
        this.maxSimulationTime = maxSimulationTime;
    }

    public PathFinderAStar getPathFinder() {
        return pathFinder;
    }

    public void setPathFinder(PathFinderAStar pathFinder) {
        this.pathFinder = pathFinder;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    public List<Shelter> getShelterList() {
        return shelterList;
    }

    public void setShelterList(List<Shelter> shelterList) {
        this.shelterList = shelterList;
    }

    public boolean isSimulationFinished() {
        return simulationFinished;
    }

    public void setSimulationFinished(boolean simulationFinished) {
        this.simulationFinished = simulationFinished;
    }
}
