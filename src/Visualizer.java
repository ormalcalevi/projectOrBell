
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List; // ודאי שאת מייבאת את המחלקות שלך
// import your.project.GridMap;
// import your.project.Person;
// import your.project.Shelter;
// import your.project.Cell;
// import your.project.CellType;
// import your.project.PersonStatus;


    public class Visualizer extends Application {

        private static final int CELL_SIZE = 10; // גודל תא בפיקסלים - אפשר לשנות
        private static final int WINDOW_DEFAULT_WIDTH = 800;
        private static final int WINDOW_DEFAULT_HEIGHT = 600;

        private Canvas mapCanvas;
        private GraphicsContext gc; // GraphicsContext לציור על ה-Canvas
        private Label statusLabel;
        private TextArea infoTextArea;

        // נתונים מהסימולציה - נצטרך דרך לאתחל אותם ולקבל עדכונים
        private GridMap currentMap;
        private List<Person> currentPeopleList;
        private List<Shelter> currentShelterList;
        // private Map<Person, Shelter> currentAssignment; // אם נרצה להציג שיבוצים

        // מתודה סטטית כדי שה-SimulationController יוכל להפעיל את ה-GUI
        // ה-SimulationController יצטרך להחזיק רפרנס למופע של Visualizer אם הוא לא זה שמפעיל את launch
        private static Visualizer instance;

        public Visualizer() {
            instance = this; // שמירת המופע הנוכחי
        }

        public static Visualizer getInstance() {
            return instance;
        }

        @Override
        public void start(Stage primaryStage) {
            primaryStage.setTitle("BELL - Symulator Ewakuacji");

            BorderPane rootPane = new BorderPane();

            // 1. משטח תצוגה ראשי (Canvas)
            // גודל ה-Canvas ייקבע בהתאם לגודל המפה שתיטען
            // כרגע נשתמש בגודל ברירת מחדל, ונשנה אותו כשנטען מפה
            mapCanvas = new Canvas(WINDOW_DEFAULT_WIDTH - 200, WINDOW_DEFAULT_HEIGHT - 100); // גודל התחלתי
            gc = mapCanvas.getGraphicsContext2D();
            rootPane.setCenter(mapCanvas);

            // 2. תצוגת סטטוס ומידע (מימין ל-Canvas)
            VBox rightPanel = new VBox(10); // 10 זה המרווח בין רכיבים
            rightPanel.setStyle("-fx-padding: 10;");

            statusLabel = new Label("Status: Oczekiwanie na załadowanie scenariusza...");
            infoTextArea = new TextArea();
            infoTextArea.setEditable(false);
            infoTextArea.setPrefRowCount(10);

            // (נוסיף כאן כפתורים בהמשך אם נרצה)

            rightPanel.getChildren().addAll(new Label("Informacje o symulacji:"), statusLabel, infoTextArea);
            rootPane.setRight(rightPanel);


            Scene scene = new Scene(rootPane, WINDOW_DEFAULT_WIDTH, WINDOW_DEFAULT_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();

            // דוגמה לאיך ה-SimulationController יוכל לעדכן את ה-Visualizer
            // זה צריך לקרות אחרי שה-Visualizer נוצר וה-Stage הוצג
            // אם ה-Visualizer הוא זה שמפעיל את ה-Controller, אז הקריאה לאתחול תהיה כאן
            // אם ה-Controller מפעיל את ה-Visualizer, הוא יצטרך לקרוא למתודות public של ה-Visualizer
        }

        // --- מתודות ציבוריות לעדכון ה-GUI מה-SimulationController ---

        /**
         * מאתחל את התצוגה עם נתוני התרחיש הראשוניים.
         * ייקרא על ידי SimulationController לאחר טעינת התרחיש.
         */
        public void initializeVisuals(GridMap map, List<Person> people, List<Shelter> shelters) {
            this.currentMap = map;
            this.currentPeopleList = people;
            this.currentShelterList = shelters;

            if (map != null) {
                // התאמת גודל ה-Canvas לגודל המפה
                mapCanvas.setWidth(map.getCols() * CELL_SIZE);
                mapCanvas.setHeight(map.getRows() * CELL_SIZE);
            }
            clearCanvas();
            drawGrid();
            drawObstacles();
            drawShelters();
            drawPeople(); // צייר אנשים במיקומם ההתחלתי
            updateStatusLabel("Scenariusz załadowany. Gotowy do uruchomienia alokacji.");
        }

        /**
         * מרענן את כל התצוגה על ה-Canvas.
         * ייקרא על ידי SimulationController בכל צעד סימולציה.
         */
        public void refreshDisplay(List<Person> updatedPeopleList, String message, int currentTime, int peopleSaved) {
            this.currentPeopleList = updatedPeopleList; // עדכון רשימת האנשים הפנימית

            Platform.runLater(() -> { // חשוב לעדכונים מחוץ ל-Thread של JavaFX
                clearCanvas();
                drawGrid();
                drawObstacles();
                drawShelters();
                // drawPaths(); // (אופציונלי) אם רוצים להציג מסלולים
                drawPeople();

                updateStatusLabel(message);
                updateInfoTextArea("Czas: " + currentTime + "\nUratowani: " + peopleSaved);
            });
        }

        public void updateStatusLabel(String text) {
            Platform.runLater(() -> statusLabel.setText("Status: " + text));
        }

        public void updateInfoTextArea(String text) {
            Platform.runLater(() -> infoTextArea.setText(text));
        }

        public void appendInfoTextArea(String text) {
            Platform.runLater(() -> infoTextArea.appendText("\n" + text));
        }


        // --- מתודות עזר פנימיות לציור ---

        private void clearCanvas() {
            gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        }

        private void drawGrid() {
            if (currentMap == null) return;
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(0.5);

            for (int i = 0; i <= currentMap.getRows(); i++) {
                gc.strokeLine(0, i * CELL_SIZE, currentMap.getCols() * CELL_SIZE, i * CELL_SIZE);
            }
            for (int j = 0; j <= currentMap.getCols(); j++) {
                gc.strokeLine(j * CELL_SIZE, 0, j * CELL_SIZE, currentMap.getRows() * CELL_SIZE);
            }
        }

        private void drawObstacles() {
            if (currentMap == null) return;
            gc.setFill(Color.DARKGRAY); // צבע למכשולים
            for (int r = 0; r < currentMap.getRows(); r++) {
                for (int c = 0; c < currentMap.getCols(); c++) {
                    // החליפי את התנאי הבא בבדיקה האמיתית של סוג התא מה-GridMap שלך
                    // if (currentMap.getCell(r, c).getType() == CellType.OBSTACLE) {
                    // gc.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    // }
                    // דוגמה: נניח שיש לך מתודה isObstacle(r,c) ב-GridMap
                    if (currentMap.getCell(r,c).getType() == CellType.OBSTACLE ) { // החליפי בבדיקה שלך
                        gc.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }

        private void drawShelters() {
            if (currentShelterList == null) return;
            gc.setFill(Color.GREEN); // צבע למקלטים
            for (Shelter shelter : currentShelterList) {
                // ודאי שיש לך גישה למיקום המקלט (Cell)
                // Cell shelterLocation = shelter.getLocation();
                // gc.fillRect(shelterLocation.getCol() * CELL_SIZE, shelterLocation.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                // אפשר להוסיף טקסט עם ה-ID של המקלט או הקיבולת
                // gc.setFill(Color.BLACK);
                // gc.fillText(shelter.getId(), shelterLocation.getCol() * CELL_SIZE + 2, shelterLocation.getRow() * CELL_SIZE + CELL_SIZE - 2);
                // gc.setFill(Color.GREEN); // החזרה לצבע המקלט
                Cell shelterLocation = shelter.getLocation();
                if (shelterLocation != null) {
                    gc.fillRect(shelterLocation.getCol() * CELL_SIZE, shelterLocation.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    // (אופציונלי) הוספת טקסט ID
                    gc.setFill(Color.BLACK);
                    gc.fillText(shelter.getId(), shelterLocation.getCol() * CELL_SIZE + 2, shelterLocation.getRow() * CELL_SIZE + CELL_SIZE - 2);
                    gc.setFill(Color.GREEN); // חזרה לצבע מילוי מקלט
                }
            }
        }

        private void drawPeople() {
            if (currentPeopleList == null) return;
            // צבעים שונים לסטטוסים שונים של אנשים
            // gc.setFill(Color.BLUE); // ברירת מחדל לאדם
            for (Person person : currentPeopleList) {
                // Cell personLocation = person.getCurrentLocation();
                // PersonStatus status = person.getStatus();
                // switch (status) {
                // case UNASSIGNED: gc.setFill(Color.LIGHTSLATEGRAY); break;
                // case ASSIGNED: gc.setFill(Color.ORANGE); break;
                // case MOVING: gc.setFill(Color.BLUE); break;
                // case REACHED_SHELTER: gc.setFill(Color.DARKGREEN); break; // או לא לצייר אותם אם הם "בתוך" המקלט
                // case STUCK: gc.setFill(Color.RED); break;
                // default: gc.setFill(Color.BLACK); break;
                // }
                // gc.fillOval(personLocation.getCol() * CELL_SIZE + 1, personLocation.getRow() * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2); // עיגול קטן יותר מהתא

                Cell personLocation = person.getCurrentLocation();
                if (personLocation != null) {
                    PersonStatus status = person.getStatus(); // ודאי שזו המתודה הנכונה לקבלת הסטטוס
                    switch (status) {
                        case UNASSIGNED: gc.setFill(Color.LIGHTSLATEGRAY); break;
                        case ASSIGNED: gc.setFill(Color.ORANGE); break; // אדם ששובץ אך עוד לא זז
                        case MOVING: gc.setFill(Color.BLUE); break;
                        case REACHED_SHELTER: gc.setFill(Color.DARKGREEN); break; // אולי לא לצייר, או לצייר בצבע אחר
                        case STUCK: gc.setFill(Color.RED); break;
                        default: gc.setFill(Color.BLACK); break;
                    }
                    // ציור אדם כעיגול. אפשר גם להשתמש בסמל או תמונה.
                    // +1 ו -2 זה כדי שהעיגול יהיה מעט קטן מהתא ולא יגע בקווים
                    if (status != PersonStatus.REACHED_SHELTER) { // לא נצייר אנשים שהגיעו (כדי לא להסתיר מקלטים)
                        gc.fillOval(personLocation.getCol() * CELL_SIZE + (CELL_SIZE*0.1),
                                personLocation.getRow() * CELL_SIZE + (CELL_SIZE*0.1),
                                CELL_SIZE * 0.8, CELL_SIZE * 0.8);
                    }
                }
            }
        }

        // (אופציונלי) מתודה לציור מסלולים
        private void drawPaths() {
            // אם לכל אדם משובץ יש רשימה של תאי מסלול, אפשר לעבור עליהם ולצייר קווים
            // gc.setStroke(Color.ORANGE);
            // gc.setLineWidth(1);
            // for (Person person : currentPeopleList) {
            // if (person.getAssignedShelter() != null && person.getPath() != null && !person.getPath().isEmpty()) {
            // List<Cell> path = person.getPath();
            // Cell previousCell = person.getInitialLocation(); // או המיקום הנוכחי אם רוצים מסלול שנותר
            // for (Cell currentCellOnPath : path) {
            // gc.strokeLine(previousCell.getCol() * CELL_SIZE + CELL_SIZE / 2.0,
            // previousCell.getRow() * CELL_SIZE + CELL_SIZE / 2.0,
            // currentCellOnPath.getCol() * CELL_SIZE + CELL_SIZE / 2.0,
            // currentCellOnPath.getRow() * CELL_SIZE + CELL_SIZE / 2.0);
            // previousCell = currentCellOnPath;
            // }
            // }
            // }
        }

        // --- שילוב עם SimulationController ---
        // ה-SimulationController יצטרך להפעיל את האפליקציה הזו (למשל, Visualizer.launch(Visualizer.class, args))
        // או שה-main של התוכנית יפעיל את ה-Visualizer, וה-Visualizer ייצור וינהל את ה-SimulationController.
        // לאחר מכן, ה-Controller יקרא למתודות כמו initializeVisuals ו-refreshDisplay.

        public static void main(String[] args) {
            // כדי להריץ את ה-Visualizer באופן עצמאי לבדיקות עיצוב ראשוניות:
            // (תצטרכי ליצור מופעים דמה של GridMap, Person, Shelter אם תריצי כך)
            launch(args);
        }
    }

