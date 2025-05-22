import java.util.List;
import java.util.Map;
// ודאי שכל המחלקות האחרות (Person, Shelter, GridMap, Cell, PathfinderAStar, Node, PersonStatus, CellType, Visualizer) מיובאות או נגישות
// import your.package.Visualizer; // אם Visualizer בחבילה אחרת

public class SimulationController {

    private informationManagement infoManager;
    private AllocationSolver allocationSolver;
    private SimulationEngine simulationEngine;
    private Visualizer visualizerInstance; // שדה שיחזיק את מופע ה-Visualizer

    private GridMap currentMap;
    private List<Person> currentPeopleList;
    private List<Shelter> currentShelterList;
    private Map<Person, List<Shelter>> currentOptionalShelters;
    private int currentMaxStepsForAllocation;
    private Map<Person, Shelter> bestAssignment;

    private static final int DEFAULT_ROWS = 100;
    private static final int DEFAULT_COLS = 100;
    private static final int DEFAULT_NUM_SHELTERS = 7;
    private static final int DEFAULT_NUM_PEOPLE = 40;
    private static final int DEFAULT_NUM_OBSTACLES = 50;
    private static final int DEFAULT_TOTAL_CAPACITY = 35; // ודאי שזה capacity per shelter כפי שדיברנו
    private static final long DEFAULT_RANDOM_SEED = 12345L;
    private static final int DEFAULT_MAX_STEPS_ALLOCATION = 50; // עדכנת ל-50, מצוין!
    private static final int DEFAULT_MAX_SIMULATION_TIME = 100;

    /**
     * בנאי של SimulationController.
     * מקבל מופע של Visualizer כדי שיוכל לעדכן את ה-GUI.
     *
     * @param visualizer מופע של Visualizer
     */
    public SimulationController(Visualizer visualizer) {
        this.visualizerInstance = visualizer;
        this.allocationSolver = new AllocationSolver();

    }

    //עבור הגרפיקה טעינת תרחישinformationManagement
    public void loadAndSetupScenario() {
        System.out.println("--- Loading and Setting up Scenario ---");
        if (this.visualizerInstance != null) {
            this.visualizerInstance.updateStatusLabel("טוען תרחיש...");
        }
////////////////////////////////
        this.infoManager = new informationManagement(
                DEFAULT_ROWS, DEFAULT_COLS, DEFAULT_NUM_PEOPLE, DEFAULT_NUM_SHELTERS,
                DEFAULT_TOTAL_CAPACITY, DEFAULT_NUM_OBSTACLES, DEFAULT_RANDOM_SEED,
                DEFAULT_MAX_STEPS_ALLOCATION
        );


        this.currentMap = this.infoManager.getMap();
        this.currentPeopleList = this.infoManager.getPeopleList();
        this.currentShelterList = this.infoManager.getShelterList();
        this.currentOptionalShelters = this.infoManager.getOptionalShelters();
        this.currentMaxStepsForAllocation = this.infoManager.getMaxSteps();
///////////////////////////////////
        if (this.currentMap == null || this.currentPeopleList == null || this.currentShelterList == null || this.currentOptionalShelters == null) {
            System.err.println("Error: Scenario data not loaded correctly from InformationManagement!");
            if (this.visualizerInstance != null) {
                this.visualizerInstance.updateStatusLabel("שגיאה בטעינת התרחיש!");
            }
            return;
        }
        System.out.println("Scenario loaded: " + this.currentPeopleList.size() + " people, " +
                this.currentShelterList.size() + " shelters on a " +
                this.currentMap.getRows() + "x" + this.currentMap.getCols() + " map.");
        System.out.println("Max steps for allocation: " + this.currentMaxStepsForAllocation);


        if (this.visualizerInstance != null) {
            this.visualizerInstance.initializeVisuals(this.currentMap, this.currentPeopleList, this.currentShelterList);
            this.visualizerInstance.updateStatusLabel("תרחיש נטען. מוכן להקצאה.");
        }
    }

    ///אחראית על הרצת אלגוריתם השיבוץallocationSolver
    public void runAllocationAlgorithm() {
        if (this.currentPeopleList == null
                || this.currentMap == null
                || this.currentShelterList == null
                || this.currentOptionalShelters == null) {
            System.err.println("Cannot run allocation: Scenario data is not loaded or is invalid.");
            if (this.visualizerInstance != null) {
                this.visualizerInstance.updateStatusLabel("שגיאה: לא ניתן להריץ הקצאה, נתונים חסרים.");
            }
            return;
        }

        System.out.println("\n--- Running Allocation Algorithm (Backtracking) ---");
        if (this.visualizerInstance != null) {
            this.visualizerInstance.updateStatusLabel("מריץ אלגוריתם הקצאה...");
        }
///////////////////////////////////
        this.bestAssignment = this.allocationSolver.solve(
                this.currentShelterList, this.currentMap, this.currentMaxStepsForAllocation,
                this.currentPeopleList, this.currentOptionalShelters
        );
//////////////////////////////////
        System.out.println("\n--- Allocation Result ---");
        if (this.bestAssignment == null || this.bestAssignment.isEmpty()) {
            System.out.println("No optimal assignment found or an error occurred.");
            if (this.visualizerInstance != null) {
                this.visualizerInstance.updateStatusLabel("הקצאה הסתיימה: לא נמצא שיבוץ אופטימלי.");
                // רענון התצוגה כדי להראות אנשים לא משובצים (אם צבעם משתנה)
                this.visualizerInstance.refreshDisplay(this.currentPeopleList, "הקצאה הסתיימה: לא נמצא שיבוץ", 0, 0);
            }
        } else {
            int peopleSavedInAllocation = this.allocationSolver.getMaxPeopleSavedSoFar();
            System.out.println("Optimal assignment found for " + peopleSavedInAllocation + " people.");
            System.out.println("Total Manhattan distance for this assignment: " + this.allocationSolver.getBestTotalManhattanDistance());
            if (this.visualizerInstance != null) {
                this.visualizerInstance.updateStatusLabel("הקצאה הסתיימה: " + peopleSavedInAllocation + " אנשים שובצו.");
                // עדכון סטטוס האנשים במערכת המרכזית (currentPeopleList) אם AllocationSolver לא עושה זאת
                // ורענון התצוגה כדי לשקף את השיבוצים (למשל, שינוי צבע של אנשים משובצים)
                this.visualizerInstance.refreshDisplay(this.currentPeopleList, "הקצאה הסתיימה", 0, peopleSavedInAllocation);
            }
            for (Map.Entry<Person, Shelter> entry : this.bestAssignment.entrySet()) {
                System.out.println("Person " + entry.getKey().getId() + " -> Shelter " + entry.getValue().getId());
            }
        }
    }

    //.simulationEngine

    public void initializeSimulationEngine() {
        if (this.bestAssignment == null) {
            System.err.println("Cannot initialize simulation engine: No assignment available.");
            if (this.visualizerInstance != null) {
                this.visualizerInstance.updateStatusLabel("שגיאה: לא ניתן לאתחל מנוע סימולציה ללא הקצאה.");
            }
            return;
        }


        System.out.println("\n--- Initializing Simulation Engine ---");
        if (this.visualizerInstance != null) {
            this.visualizerInstance.updateStatusLabel("מאתחל מנוע סימולציה...");
        }
/////////////////////
        this.simulationEngine = new SimulationEngine(
                this.currentMap, this.currentPeopleList, this.currentShelterList,
                DEFAULT_MAX_SIMULATION_TIME
        );
        this.simulationEngine.initializeSimulation(this.bestAssignment);
        System.out.println("Simulation Engine Initialized. Paths calculated.");
///////////////////


        if (this.visualizerInstance != null) {
            this.visualizerInstance.updateStatusLabel("מנוע סימולציה אותחל. מסלולים חושבו.");
            // רענון תצוגה ראשוני לאחר חישוב מסלולים (אם יש שינוי ויזואלי בסטטוס אנשים)
            // ייתכן ש-simulationEngine.getPeopleList() יחזיר רשימה עם סטטוסים מעודכנים
            this.visualizerInstance.refreshDisplay(
                    this.simulationEngine.getPersonList(), // ייתכן שצריך לקבל את הרשימה המעודכנת מהמנוע
                    "מנוע סימולציה אותחל",
                    this.simulationEngine.getCurrentTimeStep(), // צריך להיות 0 כאן
                    0 // עדיין לא ניצלו אנשים בסימולציה
            );
        }
    }


    ///////////אחראית על הרצת הסימולציה הדינמית צעד אחר צעד עד לסיומה
    public void runFullSimulation() {

        if (this.simulationEngine == null) {
            System.err.println("Cannot run simulation: Simulation Engine not initialized.");
            if (this.visualizerInstance != null) {
                this.visualizerInstance.updateStatusLabel("שגיאה: מנוע סימולציה לא אותחל.");
            }
            return;
        }

        System.out.println("\n--- Starting Full Simulation ---");
        if (this.visualizerInstance != null) {
            this.visualizerInstance.updateStatusLabel("מתחיל סימולציה...");
        }

///////////runSingleStep
        while (!this.simulationEngine.isSimulationFinished()) {
            boolean canContinue = this.simulationEngine.runSingleStep();

            if (this.visualizerInstance != null && canContinue) {
                int peopleReachedShelter = 0;
                for (Person p : this.simulationEngine.getPersonList()) {
                    if (p.getStatus() == PersonStatus.REACHED_SHELTER) peopleReachedShelter++;
                }

                this.visualizerInstance.refreshDisplay(
                        this.simulationEngine.getPersonList(),
                        "סימולציה רצה...",
                        this.simulationEngine.getCurrentTimeStep(),
                        peopleReachedShelter
                );
            }

            if (!canContinue) {
                break;
            }

            // השהייה קטנה אם רוצים לראות את הצעדים לאט יותר (רק אם יש GUI)
            if (this.visualizerInstance != null) {
                try {
                    Thread.sleep(100); // למשל 100 מילישניות
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // חשוב לשחזר את מצב ה-interrupt
                    System.err.println("Simulation thread interrupted.");
                    break;
                }
            }
        }

        System.out.println("--- Full Simulation Ended ---");
        if (this.visualizerInstance != null) {
            this.visualizerInstance.updateStatusLabel("הסימולציה הסתיימה. זמן: " + this.simulationEngine.getCurrentTimeStep());
            String summary = getSimulationSummaryText(); // מתודה חדשה ליצירת מחרוזת סיכום
            this.visualizerInstance.appendInfoTextArea("\n--- סיכום סופי ---");
            this.visualizerInstance.appendInfoTextArea(summary);
        } else {
            printSimulationSummary(); // אם אין GUI, הדפס לקונסולה
        }
    }




    private String getSimulationSummaryText() {
        if (this.simulationEngine == null || this.currentPeopleList == null) {
            return "לא ניתן להציג סיכום: הסימולציה לא רצה או שנתונים חסרים.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Simulation Summary (After ").append(this.simulationEngine.getCurrentTimeStep()).append(" steps) ---\n");
        int reachedShelterCount = 0;
        int stuckCount = 0;
        int unassignedCount = 0;
        int stillMovingCount = 0;


        List<Person> finalPeopleList = this.simulationEngine.getPersonList();

        for (Person person : finalPeopleList) {
            sb.append("Person ").append(person.getId()).append(": Status = ").append(person.getStatus());
            sb.append(person.getAssignedShelter() != null ? ", Assigned to = " + person.getAssignedShelter().getId() : ", Not Assigned");
            sb.append(person.getPath() != null && !person.getPath().isEmpty() ? ", Path Length = " + person.getPath().size() : ", No Path");
            sb.append(", Current Location = ").append(person.getCurrentLocation()).append("\n");

            switch (person.getStatus()) {
                case REACHED_SHELTER:
                    reachedShelterCount++;
                    break;
                case STUCK:
                    stuckCount++;
                    break;
                case UNASSIGNED:
                    unassignedCount++;
                    break;
                case MOVING:
                case ASSIGNED:
                    stillMovingCount++;
                    break;
                default:
                    break;
            }
        }
        sb.append("\nTotal People Reached Shelter: ").append(reachedShelterCount).append("\n");
        sb.append("Total People Stuck: ").append(stuckCount).append("\n");
        sb.append("Total People Unassigned: ").append(unassignedCount).append("\n");
        if (stillMovingCount > 0) {
            sb.append("Total People Still Moving/Assigned: ").append(stillMovingCount).append("\n");
        }

        sb.append("\nShelter Occupancies:\n");
        List<Shelter> finalShelterList = this.simulationEngine.getShelterList();
        if (finalShelterList != null) {
            for (Shelter shelter : finalShelterList) {
                sb.append("Shelter ").append(shelter.getId()).append(" (Capacity: ").append(shelter.getTotalCapacity());
                sb.append("): Occupancy = ").append(shelter.getCurrentOccupancy()).append("\n");
            }
        }
        return sb.toString();
    }

    //הדפסה כאשר אין גרפיקה
    public void printSimulationSummary() {
        System.out.println(getSimulationSummaryText());
    }


    public static void main(String[] args) {
        System.out.println("--- Running SimulationController WITHOUT GUI ---");

        // יצירת SimulationController והעברת null בתור ה-Visualizer
        SimulationController controllerNoGui = new SimulationController(null);

        // הרצת שלבי הסימולציה כרגיל
        controllerNoGui.loadAndSetupScenario();

        // (אופציונלי) בדיקה אם התרחיש נטען בהצלחה לפני שממשיכים
        // if (controllerNoGui.isScenarioLoadedSuccessfully()) { // אם הוספת מתודה כזו
        controllerNoGui.runAllocationAlgorithm();
        // if (controllerNoGui.isAllocationDoneSuccessfully()) {
        controllerNoGui.initializeSimulationEngine();
        // if (controllerNoGui.isEngineInitializedSuccessfully()) {
        controllerNoGui.runFullSimulation();

        System.out.println("\n--- SimulationController run (without GUI) finished. ---");
    }
}