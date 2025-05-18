import java.util.List;
import java.util.Map;

public class SimulationController {

    private informationManagement currentScenario;
    private AllocationSolver allocationSolver;
    private Map<Person ,Shelter> bestAssignmentFromAllocationSolver;

    public void LoadScenario() {
        //int rows = 100;
       // int cols = 100;
       // int shelters=7;
       // int peoples=40;
       // int obstacles=50;
       // int totalCapacity=35;
       // long seed = 12345L;
       // int maxSteps=90;
        //this.currentScenario = new informationManagement(rows, cols, peoples,shelters,totalCapacity,obstacles,seed,maxSteps);

        // עכשיו אפשר לגשת לנתונים דרך currentScenario
        GridMap map = this.currentScenario.getMap();
        List<Person> peopleList = this.currentScenario.getPeopleList();
        List<Shelter> sheltersList = this.currentScenario.getShelterList();
        Map<Person , List<Shelter>>optionalShelters = this.currentScenario.getOptionalShelters();


        //this.bestAssignmentFromAllocationSolver = allocationSolver.solve(sheltersList , map , maxSteps , peopleList , optionalShelters);




        // ... ולהשתמש בהם
        // visualizer.draw(map, people, this.currentScenario.getShelterList());
    }
}