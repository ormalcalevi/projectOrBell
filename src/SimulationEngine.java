import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimulationEngine {
    private GridMap map;
    private List<Person> personList;
    private List<Shelter> shelterList;
    private Map<Person, Shelter> assignment;
    private PathFinderAStar pathFinder;
    private int currentTimeStep;
    private int maxSimulationTime;
    private boolean simulationFinished;


    public SimulationEngine(GridMap map, List<Person> personList, List<Shelter> shelterList, int maxTime) {
        setMap(map);
        setPersonList(personList);
        setShelterList(shelterList);
        setMaxSimulationTime(maxTime);
        setPathFinder(new PathFinderAStar(this.map));
        setCurrentTimeStep(0);
        setSimulationFinished(false);
    }


    public void setAssignment(Map<Person, Shelter> assignment) { this.assignment = assignment; }
    public int getCurrentTimeStep() { return currentTimeStep; }
    public void setCurrentTimeStep(int currentTimeStep) { this.currentTimeStep = currentTimeStep; }
    public void setMap(GridMap map) { this.map = map; }
    public void setMaxSimulationTime(int maxSimulationTime) { this.maxSimulationTime = maxSimulationTime; }
    public void setPathFinder(PathFinderAStar pathFinder) { this.pathFinder = pathFinder; }
    public List<Person> getPersonList() { return personList; }
    public void setPersonList(List<Person> personList) { this.personList = personList; }
    public List<Shelter> getShelterList() { return shelterList; }
    public void setShelterList(List<Shelter> shelterList) { this.shelterList = shelterList; }
    public boolean isSimulationFinished() { return simulationFinished; }
    public void setSimulationFinished(boolean simulationFinished) { this.simulationFinished = simulationFinished; }


    //לוקח את ההקצאה מאפס את התוצאות בשביל בטיחות ואז מקצה מחדש
    public void initializeSimulation(Map<Person, Shelter> assignmentParam) {
        resetPeople();
        setAssignment(assignmentParam);
        setCurrentTimeStep(0);
        setSimulationFinished(false);

        if (this.personList == null || this.assignment == null ||  this.pathFinder == null) {
            // System.err.println("SimulationEngine Error: personList is null / Assignment map is null / PathFinder is null ");
            this.simulationFinished = true;
            return;
        }

        for (Person p : this.personList) {
            Shelter assignedShelter = this.assignment.get(p);
            if (assignedShelter != null) {
                p.setAssignedShelter(assignedShelter);
                Cell startCell = p.getCurrentLocation();
                Cell goalCell = assignedShelter.getLocation();
////////////////////קריאה לפונקציה עבור יצירת הA* עבור כל אדם
                if (startCell != null && goalCell != null) {
                    List<Cell> calculatedPath = this.pathFinder.findPath(startCell, goalCell);
                    p.setPath(calculatedPath);

                    if (p.isPathAssigned()) {
                        p.setStatus(PersonStatus.ASSIGNED);
                    } else {
                        p.setStatus(PersonStatus.STUCK);
                    }
                } else {
                    System.err.println("Error for Person " + p.getId() + ": Start or Goal cell is null. Person status set to STUCK.");
                    p.setStatus(PersonStatus.STUCK);
                    p.setPath(null);
                    p.setPathIndex(-1);
                }
            } else {
                p.setStatus(PersonStatus.UNASSIGNED);
                p.setPath(null);
                p.setPathIndex(-1);
                p.setAssignedShelter(null);
            }
        }
    }

    public void resetPeople() {
        if (this.personList == null) {
            System.err.println("Cannot reset people, personList is null");
            return;
        }
        for (Person p : this.personList) {
            p.setPathIndex(-1);
            p.setPath(null);
            p.setStatus(PersonStatus.IDLE);
            p.setAssignedShelter(null);
        }
    }


    ////Advances the simulation by a single time step.
    public boolean runSingleStep() {
        if (this.simulationFinished) {
            // System.out.println("Simulation has already finished.");
            return false;
        }

        if (this.currentTimeStep >= this.maxSimulationTime) {
            //System.out.println("Simulation ended: Reached max simulation time (" + this.maxSimulationTime + ").");
            finishSimulationDueToTimeout();
            return false;
        }

        this.currentTimeStep++;
        System.out.println("\n--- Step: " + this.currentTimeStep + " ---");

        //stage 1 : Save the next step for each person.
        Map<Person, Cell> plannedMoves = planIndividualMoves();

        // stage 2 : Avoid collisions and take the next step
        List<Person> peopleMovingThisStep = preventCollisionsAndExecuteNextStep(plannedMoves);

        // stage 3 : Performing the movements for the approved people
        executeMoves(peopleMovingThisStep);

        // stage 4 : Checking if the simulation is finished
        checkAndSetSimulationEndCondition();

        return !this.simulationFinished; // החזר true אם הסימולציה *לא* הסתיימה
    }


    //סורק את האנשים וקובע לאן כל אחד מתכנן ללכת בצעד הנוכחי .  אנשים שאין להם צעד הם הגיעו ליעדם .
    private Map<Person, Cell> planIndividualMoves() {
        Map<Person, Cell> plannedMoves = new HashMap<>();
        if (this.personList == null) return plannedMoves;

        for (Person p : this.personList) {
            if (p.getStatus() == PersonStatus.ASSIGNED || p.getStatus() == PersonStatus.MOVING) {
                if (p.getStatus() == PersonStatus.ASSIGNED) {
                    p.setStatus(PersonStatus.MOVING);
                }

                Cell nextStep = p.getNextStepInPath();

                if (nextStep != null) {
                    plannedMoves.put(p, nextStep);
                } else { // אין צעד הבא בנתיב
                    if (p.getStatus() == PersonStatus.MOVING) { // אם הוא היה בתנועה
                        if (p.getAssignedShelter() != null && p.getCurrentLocation().equals(p.getAssignedShelter().getLocation())) {
                            p.setStatus(PersonStatus.REACHED_SHELTER);
                            //p.getAssignedShelter().addOccupant();
                        } else if (p.getAssignedShelter() != null && !p.getCurrentLocation().equals(p.getAssignedShelter().getLocation())) {
                            // עדיין בתנועה, אין צעד הבא, והוא לא ביעד - תקוע.
                            //System.err.println("Person " + p.getId() + " is STUCK: MOVING but no next step and not at goal " + p.getAssignedShelter().getLocation() + ". Current: " + p.getCurrentLocation());
                            p.setStatus(PersonStatus.STUCK);
                        } else if (p.getAssignedShelter() == null) {
                            System.err.println("Person " + p.getId() + " is STUCK: MOVING but has no assigned shelter.");
                            p.setStatus(PersonStatus.UNASSIGNED);
                        }
                    }
                }
            }
        }
        return plannedMoves;
    }


    private List<Person> preventCollisionsAndExecuteNextStep(Map<Person, Cell> plannedMoves) {
        List<Person> peopleActuallyMovingThisStep = new ArrayList<>();
        Map<Cell, List<Person>> targetCellContenders = new HashMap<>();

        // שלב ראשון : בדיקה עבור כל אדם אם התא אליו הוא מיועד קיים כבר או לא
        // אם כן : מכניסים את האדם לרשימה של האנשים עבור התא הזה
        // אם לא : יוצרים רשימה חדשה של אנשים אשר ייצגו את כניסתם לתא
        for (Map.Entry<Person, Cell> entry : plannedMoves.entrySet()) {
            Person person = entry.getKey();
            Cell targetCell = entry.getValue();
            targetCellContenders.computeIfAbsent(targetCell, k -> new ArrayList<>()).add(person);
        }

        // 2. טיפול בהתנגשויות והחלטה מי זז
        for (Map.Entry<Cell, List<Person>> entry : targetCellContenders.entrySet()) {
            Cell targetCell = entry.getKey();
            List<Person> listPeopleCell = entry.getValue();

            if (listPeopleCell.size() == 1) {
                // אין התנגשות
                peopleActuallyMovingThisStep.add(listPeopleCell.get(0));
            } else if (listPeopleCell.size() > 1) {
                // יש התנגשות
                Person prioritizedPerson = selectPrioritizedPersonFromContenders(listPeopleCell , targetCell);
                peopleActuallyMovingThisStep.add(prioritizedPerson);
            }
        }
        return peopleActuallyMovingThisStep;
    }

    ///return Prioritized Person From Contenders
    public Person selectPrioritizedPersonFromContenders(List<Person>listPeopleCell , Cell cellCompetition ){
        Person prioritizedPerson = null;
        int DistanceToShelter = Integer.MAX_VALUE;
        Cell target = null;

        for (Person person : listPeopleCell) {
            if((person.getAssignedShelter() != null && person.getAssignedShelter().getLocation() != null)){
                target = person.getAssignedShelter().getLocation();
            }
            else if(person.getAlternativeGoalCell() != null ){
                target = person.getAlternativeGoalCell();
            }


            int currentDistance = manhattanDistance(target, cellCompetition);

            if (prioritizedPerson == null || currentDistance < DistanceToShelter) {
                DistanceToShelter = currentDistance;
                prioritizedPerson = person;
            }

        }
        return prioritizedPerson  ;
    }


    /**
     * מבצע בפועל את התנועה עבור האנשים שאושרו לזוז.
     * @param peopleToMove רשימת האנשים שיש להזיז.
     */
    private void executeMoves(List<Person> peopleToMove) {
        System.out.println("Executing moves for " + peopleToMove.size() + " people.");
        for (Person person : peopleToMove) {
            // ודא שהאדם עדיין במצב תנועה ולא הגיע/נתקע בינתיים
            if (person.getStatus() == PersonStatus.MOVING) {
                Cell oldLocation = person.getCurrentLocation(); // אופציונלי לדיבאג
                person.advanceOnPath(); // מקדם את האדם בנתיב שלו (מעדכן מיקום ו-pathIndex)

                // אם advanceOnPath לא מעדכנת תפוסת מקלט, עשה זאת כאן:
                if (person.getStatus() == PersonStatus.REACHED_SHELTER) {
                    if (person.getAssignedShelter() != null) {
                        // לפני שמעדכנים, אפשר לבדוק אם כבר עדכנו כדי למנוע ספירה כפולה
                        // (למשל, אם הסטטוס *רק עכשיו* השתנה ל-REACHED_SHELTER)
                        // אבל אם addOccupant מטפל בזה, זה בסדר.
                        // בדרך כלל, מעדכנים תפוסה רק פעם אחת כשהסטטוס משתנה.
                        person.getAssignedShelter().addOccupant(); // או incrementOccupancy()
                        System.out.println("Person " + person.getId() + " has officially reached shelter " +
                                person.getAssignedShelter().getId() + ". Shelter occupancy updated.");
                    }
                }
            }
        }
    }

    /**
     * בודק אם הסימולציה הסתיימה (כל האנשים המשובצים הגיעו או נתקעו).
     * אם כן, מעדכן את this.simulationFinished ל-true.
     */
    private void checkAndSetSimulationEndCondition() {
        if (this.personList == null || this.personList.isEmpty()) {
            this.simulationFinished = true; // אין אנשים, הסימולציה הסתיימה
            return;
        }

        boolean allAssignedAreDoneOrStuck = true;
        for (Person person : this.personList) {
            // בדוק רק אנשים שהיו אמורים לזוז (ASSIGNED או MOVING בתחילת הצעד)
            // או כאלה ששובצו (כלומר, לא UNASSIGNED מלכתחילה)
            if (person.getAssignedShelter() != null) { // רק אנשים ששובצו למקלט
                if (person.getStatus() == PersonStatus.ASSIGNED || person.getStatus() == PersonStatus.MOVING) {
                    allAssignedAreDoneOrStuck = false; // מצאנו מישהו שעדיין בתהליך
                    break;
                }
            }
        }

        if (allAssignedAreDoneOrStuck) {
            //System.out.println("Simulation ended: All assigned people have reached shelters or are stuck.");
            this.simulationFinished = true;
        }
    }

    /**
     * מעדכן את הסטטוס של כל האנשים שעדיין בתנועה ל-STUCK (או TIMED_OUT)
     * כאשר הסימולציה מסתיימת בגלל מגבלת זמן.
     */
    private void finishSimulationDueToTimeout() {
        if (this.personList != null) {
            for (Person person : this.personList) {
                if (person.getStatus() == PersonStatus.MOVING || person.getStatus() == PersonStatus.ASSIGNED) {
                    person.setStatus(PersonStatus.STUCK); // או סטטוס חדש כמו TIMED_OUT
                    System.out.println("Person " + person.getId() + " set to STUCK due to simulation timeout.");
                }
            }
        }
    }


    // פונקציית עזר ל-manhattanDistance (יכולה להיות גם סטטית או במחלקה אחרת)
    private int manhattanDistance(Cell c1, Cell c2) {
        if (c1 == null || c2 == null) return Integer.MAX_VALUE;
        return Math.abs(c1.getRow() - c2.getRow()) + Math.abs(c1.getCol() - c2.getCol());
    }

}