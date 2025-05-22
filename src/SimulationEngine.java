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
        this.map = map;
        this.personList = personList;
        this.shelterList = shelterList;
        this.maxSimulationTime = maxTime;
        this.pathFinder = new PathFinderAStar(this.map);
        this.currentTimeStep = 0;
        this.simulationFinished = false;
    }

    public Map<Person, Shelter> getAssignment() { return assignment; }
    public void setAssignment(Map<Person, Shelter> assignment) { this.assignment = assignment; }
    public int getCurrentTimeStep() { return currentTimeStep; }
    public void setCurrentTimeStep(int currentTimeStep) { this.currentTimeStep = currentTimeStep; }
    public GridMap getMap() { return map; }
    public void setMap(GridMap map) { this.map = map; }
    public int getMaxSimulationTime() { return maxSimulationTime; }
    public void setMaxSimulationTime(int maxSimulationTime) { this.maxSimulationTime = maxSimulationTime; }
    public PathFinderAStar getPathFinder() { return pathFinder; }
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

        if (this.personList == null) {
            System.err.println("SimulationEngine Error: personList is null. Cannot initialize simulation.");
            this.simulationFinished = true;
            return;
        }
        if (this.assignment == null) {
            System.err.println("SimulationEngine Error: Assignment map is null. Cannot calculate paths.");
            this.simulationFinished = true;
            return;
        }
        if (this.pathFinder == null) {
            System.err.println("SimulationEngine Error: PathFinder is null. Cannot calculate paths.");
            this.simulationFinished = true;
            return;
        }

        System.out.println("Initializing simulation: Calculating paths for assigned people...");
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
                        System.out.println("No path found for Person " + p.getId() + ". Status set to STUCK.");
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
        System.out.println("Simulation paths initialization complete.");
    }

    public void resetPeople() {
        if (this.personList == null) {
            System.err.println("Cannot reset people, personList is null!");
            return;
        }
        for (Person p : this.personList) {
            p.setPathIndex(-1);
            p.setPath(null);
            p.setStatus(PersonStatus.IDLE);
            p.setAssignedShelter(null);
        }
        System.out.println("All people have been reset to initial state.");
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
        System.out.println("\n--- Simulation Step: " + this.currentTimeStep + " ---");

        // 1. תכנון התנועות המבוקשות על ידי כל אדם
        Map<Person, Cell> plannedMoves = planIndividualMoves();

        // 2. זיהוי התנגשויות והחלטה מי באמת יזוז
        List<Person> peopleMovingThisStep = resolveCollisionsAndGetMovers(plannedMoves);

        // 3. ביצוע התנועות בפועל עבור האנשים שאושרו
        executeMoves(peopleMovingThisStep);

        // 4. בדיקה אם הסימולציה הסתיימה (למשל, כולם הגיעו או נתקעו)
        checkAndSetSimulationEndCondition();

        return !this.simulationFinished; // החזר true אם הסימולציה *לא* הסתיימה
    }

    /**
     * עובר על כל האנשים וקובע לאן כל אחד מהם מתכנן לזוז בצעד הנוכחי.
     * מטפל גם באנשים שמגיעים ליעדם בצעד זה (אם אין להם צעד הבא).
     * @return מפה של Person לתא היעד המתוכנן שלו.
     */

    private Map<Person, Cell> planIndividualMoves() {
        Map<Person, Cell> plannedMoves = new HashMap<>();
        if (this.personList == null) return plannedMoves; // הגנה

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
                            p.getAssignedShelter().addOccupant();
                        } else if (p.getAssignedShelter() != null && !p.getCurrentLocation().equals(p.getAssignedShelter().getLocation())) {
                            // עדיין בתנועה, אין צעד הבא, והוא לא ביעד - תקוע.
                            System.err.println("Person " + p.getId() + " is STUCK: MOVING but no next step and not at goal " + p.getAssignedShelter().getLocation() + ". Current: " + p.getCurrentLocation());
                            p.setStatus(PersonStatus.STUCK);
                        } else if (p.getAssignedShelter() == null) {
                            System.err.println("Person " + p.getId() + " is STUCK: MOVING but has no assigned shelter.");
                            p.setStatus(PersonStatus.STUCK);
                        }
                    }
                }
            }
        }
        return plannedMoves;
    }

    /**
     * מקבל את התנועות המתוכננות, מזהה התנגשויות, ומחליט מי מהאנשים יזוז בפועל.
     * @param plannedMoves מפה של Person לתא היעד המתוכנן שלו.
     * @return רשימה של Person שיזוזו בפועל בצעד זה.
     */
    private List<Person> resolveCollisionsAndGetMovers(Map<Person, Cell> plannedMoves) {
        List<Person> peopleActuallyMovingThisStep = new ArrayList<>();
        Map<Cell, List<Person>> targetCellContenders = new HashMap<>();

        // 1. אכלוס מפת המתחרים על כל תא יעד
        for (Map.Entry<Person, Cell> entry : plannedMoves.entrySet()) {
            Person person = entry.getKey();
            Cell targetCell = entry.getValue();
            targetCellContenders.computeIfAbsent(targetCell, k -> new ArrayList<>()).add(person);
        }

        // 2. טיפול בהתנגשויות והחלטה מי זז
        for (Map.Entry<Cell, List<Person>> entry : targetCellContenders.entrySet()) {
            Cell targetCell = entry.getKey();
            List<Person> contenders = entry.getValue();

            if (contenders.size() == 1) {
                // אין התנגשות
                peopleActuallyMovingThisStep.add(contenders.get(0));
            } else if (contenders.size() > 1) {
                // יש התנגשות
                Person prioritizedPerson = null;
                int minDistanceToOwnShelter = Integer.MAX_VALUE;

                for (Person personInContention : contenders) {
                    if (personInContention.getAssignedShelter() != null && personInContention.getAssignedShelter().getLocation() != null) {
                        int distance = manhattanDistance(targetCell, personInContention.getAssignedShelter().getLocation());
                        if (distance < minDistanceToOwnShelter) {
                            minDistanceToOwnShelter = distance;
                            prioritizedPerson = personInContention;
                        }
                        // (אופציונלי: שובר שוויון אם המרחקים זהים)
                    }
                }

                if (prioritizedPerson != null) {
                    peopleActuallyMovingThisStep.add(prioritizedPerson);
                    // הדפסת דיבאג על מי זז ומי מחכה
                    // System.out.println("Collision at " + targetCell + ": Person " + prioritizedPerson.getId() + " moves. Others wait.");
                } else {
                    System.err.println("Collision at " + targetCell + " but no one could be prioritized. No one moves to this cell this step.");
                }
            }
        }
        return peopleActuallyMovingThisStep;
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
            System.out.println("Simulation ended: All assigned people have reached shelters or are stuck.");
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
