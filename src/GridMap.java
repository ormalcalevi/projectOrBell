import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GridMap {
    private Cell[][] Map;
    private int Rows;
    private int Cols;
    private Random randomGenerator;


    // Constructor - לפי גודל רצוי
    public GridMap(int numRows, int numCols, long randomSeed ) { // הוספנו seed
        this.Rows = numRows;
        this.Cols = numCols;
        this.Map= new Cell[numRows][numCols];
        this.randomGenerator = new Random(randomSeed);
        initializeEmptyMap();
    }

    private void initializeEmptyMap() {
        for (int r = 0; r < Rows; r++) {
            for (int c = 0; c < Cols; c++) {
                Map[r][c] = new Cell(r, c, CellType.EMPTY);
            }
        }
    }

    // (קונסטרקטור מטעינת קובץ/מערך יכול להישאר דומה, עם התאמה לשמות המשתנים)

    // Getters for dimensions
    public int getNumRows() { return Rows; }
    public int getNumCols() { return Cols; }

    // בדיקה אם קואורדינטה נמצאת בגבולות המפה
    public boolean isValid(int row, int col) {
        return row >= 0 && row < Rows && col >= 0 && col < Cols;
    }

    // קבלת התא במיקום מסוים
    public Cell getCell(int row, int col) {
        if (isValid(row, col)) {
            return Map[row][col];
        }
        return null;
    }

    // בדיקה אם ניתן ללכת על תא מסוים
    public boolean isWalkable(int row, int col) {
        Cell cell = getCell(row, col);
        return cell != null && cell.isWalkable();
    }

    // מתודה לאתחול מכשולים/מקלטים (צריך לוודא שלא דורסים)
    public boolean setCellType(int row, int col, CellType type) {
        if (isValid(row, col)) {
            if (getCell(row, col).getType() == CellType.EMPTY){
                Map[row][col].setType(type);
                return true;
            }
            return false;

        }
        return false;
    }


    // **** getWalkableNeighbors מעודכן לפי x=row, y=col ****
    public List<Cell> getWalkableNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int r = cell.getRow();
        int c = cell.getCol();

        // שינויים בשורה (dr) ובעמודה (dc) עבור 8 כיוונים
        int[] dr = {-1, 1, 0, 0, -1, -1, 1, 1}; // למעלה, למטה, ללא, ללא, למעלה-שמאלה, למעלה-ימינה, למטה-שמאלה, למטה-ימינה
        int[] dc = {0, 0, -1, 1, -1, 1, -1, 1}; // ללא, ללא, שמאלה, ימינה, שמאלה, ימינה, שמאלה, ימינה

        for (int i = 0; i < 8; i++) {
            int neighborRow = r + dr[i];
            int neighborCol = c + dc[i];

            // 1. האם השכן הפוטנציאלי הוא תא חוקי והליכתי?
            if (isWalkable(neighborRow, neighborCol)) {
                // 2. טיפול במקרה של תנועה באלכסון (הגישה המחמירה)
                boolean isDiagonal = (dr[i] != 0 && dc[i] != 0);
                if (isDiagonal) {
                    // בדוק את שני תאי ה"פינה": (r + dr[i], c) ו- (r, c + dc[i])
                    if (isWalkable(r + dr[i], c) && isWalkable(r, c + dc[i])) {
                        neighbors.add(getCell(neighborRow, neighborCol));
                    }
                } else {
                    // תנועה ישרה
                    neighbors.add(getCell(neighborRow, neighborCol));
                }
            }
        }
        return neighbors;
    }



    // פונקציה למצוא תא ריק רנדומלי (חשוב כדי למקם אובייקטים)
    public Cell findRandomEmptyCell() {
        int attempts = 0;
        int maxAttempts = Rows * Cols * 2; // למנוע לולאה אינסופית אם המפה מלאה

        while(attempts < maxAttempts) {
            int r = randomGenerator.nextInt(Rows);
            int c = randomGenerator.nextInt(Cols);
            if (getCell(r, c).getType() == CellType.EMPTY) {
                return getCell(r, c);
            }
            attempts++;
        }
        return null; // לא נמצא תא ריק
    }


    public void placeObstacles(int count) {
        System.out.println("ממקם " + count + " מכשולים...");
        for (int i = 0; i < count; i++) {
            Cell emptyCell = findRandomEmptyCell();
            if (emptyCell != null) {
                emptyCell.setType(CellType.OBSTACLE);
            } else {
                System.err.println("אזהרה: לא נמצא תא ריק עבור מכשול מספר " + (i + 1));
                break; // אין טעם להמשיך אם המפה מלאה
            }
        }
    }

}


