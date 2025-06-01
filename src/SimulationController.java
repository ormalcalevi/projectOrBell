import java.util.List;
import java.util.Map;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;


public class SimulationController {

    private informationManagement infoManager;
    private AllocationSolver allocationSolver;
    private SimulationEngine simulationEngine;
    private Visualizer visualizer;

    private GridMap currentMap;
    private List<Person> currentPeople;
    private List<Shelter> currentShelters;
    private Map<Person, List<Shelter>> currentOptionalShelters;
    private int currentMaxStepsForAllocation;
    private Map<Person, Shelter> bestAssignment;
    private long BacktrackCallCount;

    // ערכי ברירת מחדל לתרחיש
    private static final int DEFAULT_ROWS = 75;
    private static final int DEFAULT_COLS = 75;
    private static final int DEFAULT_NUM_SHELTERS = 7;
    private static final int DEFAULT_NUM_PEOPLE = 40;
    private static final int DEFAULT_NUM_OBSTACLES = 200;
    private static final int DEFAULT_TOTAL_CAPACITY = 35;
    private static final long DEFAULT_RANDOM_SEED = 12345L;
    private static final int DEFAULT_MAX_STEPS_ALLOCATION = 50;
    private static final int DEFAULT_MAX_SIM_TIME = 100;

    /**
     * @param visualizer מופע Visualizer להצגת UI
     */
    public SimulationController(Visualizer visualizer) {
        this.visualizer = visualizer;
        this.allocationSolver = new AllocationSolver();
    }

    /**
     * טוען תרחיש ראשוני ומאתחל את ה-Visualizer
     */
    public void loadAndSetupScenario() {
        if (visualizer != null) {
            visualizer.updateStatusLabel("טוען תרחיש...");
        }
        infoManager = new informationManagement(
                DEFAULT_ROWS, DEFAULT_COLS,
                DEFAULT_NUM_PEOPLE, DEFAULT_NUM_SHELTERS,
                DEFAULT_TOTAL_CAPACITY, DEFAULT_NUM_OBSTACLES,
                DEFAULT_RANDOM_SEED, DEFAULT_MAX_STEPS_ALLOCATION
        );
        currentMap = infoManager.getMap();
        currentPeople = infoManager.getPeopleList();
        currentShelters = infoManager.getShelterList();
        currentOptionalShelters = infoManager.getOptionalShelters();
        currentMaxStepsForAllocation = infoManager.getMaxSteps();

        if (currentMap == null || currentPeople == null || currentShelters == null || currentOptionalShelters == null) {
            System.err.println("Error: Scenario data not loaded correctly.");
            if (visualizer != null) {
                visualizer.updateStatusLabel("שגיאה בטעינת התרחיש!");
            }
            return;
        }

        if (visualizer != null) {
            visualizer.initializeVisuals(currentMap, currentPeople, currentShelters);
            visualizer.updateStatusLabel("תרחיש נטען. מוכן להקצאה.");
        }
    }

    /**
     * מריץ את אלגוריתם ההקצאה עם הפרמטרים מה-informationManagement
     */
    public void runAllocationAlgorithm() {
        if (currentMap == null || currentPeople == null || currentShelters == null || currentOptionalShelters == null) {
            if (visualizer != null) {
                visualizer.updateStatusLabel("שגיאה: נתונים חסרים להקצאה.");
            }
            return;
        }
        if (visualizer != null) {
            visualizer.updateStatusLabel("מריץ אלגוריתם הקצאה...");
        }

        // קריאה לתשובה בהתאם לחתימת solve
        bestAssignment = allocationSolver.solve(
                currentShelters,
                currentPeople,
                currentMap,
                currentMaxStepsForAllocation,
                currentPeople,
                currentOptionalShelters
        );

        if (bestAssignment == null || bestAssignment.isEmpty()) {
            System.out.println("No optimal assignment found or an error occurred.");
            if (visualizer != null) {
                visualizer.refreshDisplay(currentPeople, "הקצאה הסתיימה: לא נמצא שיבוץ", 0, 0);
            }
        } else {
            int saved = allocationSolver.getMaxPeopleSavedSoFar();
            int totalDist = allocationSolver.getBestTotalManhattanDistance();
            System.out.println("Optimal assignment for " + saved + " people. Total distance: " + totalDist);
            if (visualizer != null) {
                visualizer.refreshDisplay(currentPeople, "הקצאה הסתיימה: " + saved + " אנשים שובצו.", 0, saved);
            }
        }
    }
  /*  public void runAllocationAlgorithm() {
        if (currentMap == null || currentPeople == null || currentShelters == null || currentOptionalShelters == null) {
            if (visualizer != null) {
                visualizer.updateStatusLabel("שגיאה: נתונים חסרים להקצאה.");
            }
            return;
        }
        if (visualizer != null) {
            visualizer.updateStatusLabel("מריץ אלגוריתם הקצאה...");
        }

        // קריאה לתשובה בהתאם לחתימת solve
        bestAssignment = allocationSolver.solve(
                currentShelters,
                currentPeople,
                currentMap,
                currentMaxStepsForAllocation,
                currentPeople,
                currentOptionalShelters
        );

        // ================== תוספות חדשות כאן ==================

        // 1. קבלת מספר הקריאות שבוצעו בפועל (כבר קיים אצלך)
        this.BacktrackCallCount = allocationSolver.getBacktrackCallCount();

        // 2. קבלת מספר הקריאות הישירות שנחסכו
        long immediateCallsAvoided = allocationSolver.getPotentialImmediateCallsAvoided();

        // 3. חישוב סך הקריאות הפוטנציאלי (הערכה)
        long estimatedPotentialCalls = this.BacktrackCallCount + immediateCallsAvoided;

        // 4. קריאה למתודה החדשה ב-Visualizer לעדכון הסטטיסטיקה
        if (visualizer != null) {
            visualizer.updateAlgorithmStats(this.BacktrackCallCount, estimatedPotentialCalls);
        }

        // ================== סוף התוספות ==================

        if (bestAssignment == null || bestAssignment.isEmpty()) {
            System.out.println("No optimal assignment found or an error occurred.");
            if (visualizer != null) {
                visualizer.refreshDisplay(currentPeople, "הקצאה הסתיימה: לא נמצא שיבוץ", 0, 0);
            }
        } else {
            int saved = allocationSolver.getMaxPeopleSavedSoFar();
            int totalDist = allocationSolver.getBestTotalManhattanDistance();
            System.out.println("Optimal assignment for " + saved + " people. Total distance: " + totalDist);
            if (visualizer != null) {
                // שים לב: אתה קורא גם ל-refreshDisplay כדי לעדכן את המפה, וגם ל-updateAlgorithmStats כדי לעדכן את ה-Labels. זה מצוין.
                visualizer.refreshDisplay(currentPeople, "הקצאה הסתיימה: " + saved + " אנשים שובצו.", 0, saved);
            }
        }
    }*/

    /**
     * מאתחל את מנוע הסימולציה לאחר הקצאה
     */
    public void initializeSimulationEngine() {
        if (bestAssignment == null) {
            if (visualizer != null) {
                visualizer.updateStatusLabel("שגיאה: אין הקצאה לאתחול סימולציה.");
            }
            return;
        }
        simulationEngine = new SimulationEngine(currentMap, currentPeople, currentShelters, DEFAULT_MAX_SIM_TIME);
        simulationEngine.initializeSimulation(bestAssignment);

        if (visualizer != null) {
            visualizer.refreshDisplay(simulationEngine.getPersonList(), "מנוע סימולציה אותחל", 0, 0);
        }
    }

   //סימולציה דינאמית
    public void startSimulation() {
        if (simulationEngine == null) return;

        // 1. הגדר Timeline ריק מראש, וסמן אותו כ־final כדי שנוכל לשייך אותו ב־lambda
        final Timeline timeline = new Timeline();

        // 2. צור KeyFrame עם ה־handler שבו תעדכן ותעצור את ה־timeline במידת הצורך
        KeyFrame frame = new KeyFrame(Duration.millis(100), event -> {
            if (!simulationEngine.isSimulationFinished()) {
                // כל צעד סימולציה
                simulationEngine.runSingleStep();

                int reached = (int) simulationEngine.getPersonList().stream()
                        .filter(p -> p.getStatus() == PersonStatus.REACHED_SHELTER)
                        .count();

                if (visualizer != null) {
                    visualizer.refreshDisplay(
                            simulationEngine.getPersonList(),
                            "סימולציה רצה...",
                            simulationEngine.getCurrentTimeStep(),
                            reached
                    );
                }

            } else {
                // כאן הסימולציה הסתיימה – עצור את ה־timeline
                timeline.stop();

                if (visualizer != null) {
                    visualizer.updateInfoTextArea(
                            "--- הסימולציה הסתיימה ---\n" +
                                    getSimulationSummaryText()
                    );
                }
            }
        });

        // 3. קבע את ה־KeyFrame ל־Timeline, הגדר לולאה אינסופית והפעל
        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }


    /**
     * מסכם את תוצאות הסימולציה לטקסט
     */
    private String getSimulationSummaryText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total steps: ").append(simulationEngine.getCurrentTimeStep()).append("\n");
        // אפשר להוסיף כאן סטטוסים ודוח מקלטים
        return sb.toString();
    }
}