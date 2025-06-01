import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;


import java.util.List;
import java.util.stream.Collectors;

public class Visualizer extends Application {

    private static final int CELL_SIZE = 12;  // גודל תא בפיקסלים

    private Canvas canvas;
    private GraphicsContext gc;
    private Button restartButton;
    private Label timerLabel;
    private Label savedLabel;
    private Label peopleCountLabel;
    private Label obstaclesCountLabel;
    private Label sheltersCountLabel;
    private ListView<String> shelterListView;
    private Label statusLabel;
    private TextArea infoTextArea;

    private GridMap currentMap;
    private List<Shelter> currentShelters;

    private SimulationController ctrl;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. צור Canvas
        primaryStage.setMaximized(true);
        canvas = new Canvas(600, 600);
        gc     = canvas.getGraphicsContext2D();

        // 2. צור את כל ה־Controls מראש!
        restartButton        = new Button("הרץ שוב");
        timerLabel           = new Label("זמן: 0");
        savedLabel           = new Label("ניצולים: 0");
        peopleCountLabel     = new Label("אנשים: 0");
        obstaclesCountLabel  = new Label("מכשולים: 0");
        sheltersCountLabel   = new Label("מקלטים: 0");
        shelterListView      = new ListView<>();
        shelterListView.setPrefHeight(120);
        statusLabel          = new Label("ממתין...");
        infoTextArea         = new TextArea();
        infoTextArea.setPrefRowCount(4);

        // 3. עכשיו אפשר לבנות את ה־VBox בלי בעיה
        VBox side = new VBox(8,
                restartButton,
                timerLabel,
                savedLabel,
                peopleCountLabel,
                obstaclesCountLabel,
                sheltersCountLabel,
                new Label("מקלטים (ID:עומס/קיבולת):"),
                shelterListView,
                statusLabel,
                infoTextArea
        );
        side.setPadding(new Insets(10));
        side.setBackground(new Background(
                new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)
        ));

        // 4. עטוף את Canvas ב־ScrollPane (כפי שדיברנו)
        ScrollPane canvasScroll = new ScrollPane(canvas);
        canvasScroll.setPannable(true);
        canvasScroll.setPrefViewportWidth(600);
        canvasScroll.setPrefViewportHeight(600);

        // 5. סדר הכל ב־BorderPane
        BorderPane root = new BorderPane();
        root.setCenter(canvasScroll);
        root.setRight(side);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("BELL Simulation");
        primaryStage.show();

        // 6. הפעל סימולציה
        ctrl = new SimulationController(this);
        setupActions();
        ctrl.loadAndSetupScenario();
        ctrl.runAllocationAlgorithm();
        ctrl.initializeSimulationEngine();
        ctrl.startSimulation();
    }

    private void setupActions() {
        restartButton.setOnAction(e -> {
            infoTextArea.clear();
            timerLabel.setText("זמן: 0");
            savedLabel.setText("ניצולים: 0");
            ctrl.loadAndSetupScenario();
            ctrl.runAllocationAlgorithm();
            ctrl.initializeSimulationEngine();
            ctrl.startSimulation();
        });
    }

    public void initializeVisuals(GridMap map, List<Person> people, List<Shelter> shelters) {
        this.currentMap = map;
        this.currentShelters = shelters;
        canvas.setWidth(map.getCols() * CELL_SIZE);
        canvas.setHeight(map.getRows() * CELL_SIZE);

        // Static counts
        peopleCountLabel.setText("אנשים: " + people.size());
        int obstacleCount = 0;
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                if (map.getCell(r, c).getType() == CellType.OBSTACLE) obstacleCount++;
            }
        }
        obstaclesCountLabel.setText("מכשולים: " + obstacleCount);
        sheltersCountLabel.setText("מקלטים: " + shelters.size());

        // Shelters list
        Platform.runLater(() -> shelterListView.getItems().setAll(
                shelters.stream()
                        .map(s -> s.getId() + ": " + s.getCurrentOccupancy() + "/" + s.getTotalCapacity())
                        .collect(Collectors.toList())
        ));

        drawEverything(people);
    }

    public void refreshDisplay(List<Person> people, String statusText, int timeStep, int reachedCount) {
        Platform.runLater(() -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            drawMap();
            drawPeople(people);

            // Dynamic updates
            timerLabel.setText("זמן: " + timeStep);
            savedLabel.setText("ניצולים: " + reachedCount);
            peopleCountLabel.setText("אנשים: " + people.size());
            statusLabel.setText(statusText);

            shelterListView.getItems().setAll(
                    currentShelters.stream()
                            .map(s -> s.getId() + ": " + s.getCurrentOccupancy() + "/" + s.getTotalCapacity())
                            .collect(Collectors.toList())
            );
        });
    }

    public void updateInfoTextArea(String text) {
        Platform.runLater(() -> infoTextArea.appendText(text + " "));
    }

    public void updateStatusLabel(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    private void drawEverything(List<Person> people) {
        drawMap();          // כאן עדיין מציירים רק ריבועי רקע ומכשולים
        drawPeople(people); // עכשיו האנשים
        drawShelters();     // ואז המקלטים מעל כולם
    }

    // הוסף מתודה חדשה:
    private void drawShelters() {
        for (Shelter s : currentShelters) {
            int r = s.getLocation().getRow(), c = s.getLocation().getCol();
            gc.setFill(Color.GOLD);
            gc.fillRect(c*CELL_SIZE, r*CELL_SIZE, CELL_SIZE, CELL_SIZE);
            // אפשר גם לצייר גבול או אייקון
        }
    }


    private void drawMap() {
        int rows = currentMap.getRows(), cols = currentMap.getCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = currentMap.getCell(r, c);
                gc.setFill(cell.getType() == CellType.OBSTACLE ? Color.DARKGRAY
                        : cell.getType() == CellType.SHELTER  ? Color.GOLD
                        : Color.WHITE);
                gc.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        for (int r = 0; r <= rows; r++)
            gc.strokeLine(0, r * CELL_SIZE, cols * CELL_SIZE, r * CELL_SIZE);
        for (int c = 0; c <= cols; c++)
            gc.strokeLine(c * CELL_SIZE, 0, c * CELL_SIZE, rows * CELL_SIZE);
    }

    private void drawPeople(List<Person> people) {
        double size = CELL_SIZE * 0.8;
        for (Person p : people) {
            int r = p.getCurrentLocation().getRow(),
                    c = p.getCurrentLocation().getCol();

            Color fill;
            switch (p.getStatus()) {
                case REACHED_SHELTER: fill = Color.LIMEGREEN;     break;
                case MOVING:         fill = Color.CORNFLOWERBLUE; break;
                case STUCK:          fill = Color.RED;           break;
                case UNASSIGNED:     fill = Color.PURPLE;        break;  // סגול
                default:             fill = Color.GRAY;          break;
            }
            gc.setFill(fill);

            double x = c * CELL_SIZE + (CELL_SIZE - size) / 2;
            double y = r * CELL_SIZE + (CELL_SIZE - size) / 2;
            gc.fillOval(x, y, size, size);
        }
    }

}
