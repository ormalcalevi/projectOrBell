import java.util.Objects;

public class Cell {
    private int row;
    private int col;
    private CellType type;



    public Cell(int row, int col, CellType type) {
        setCol(col);
        setRow(row);
        setType(type);
    }

    // Getters
    public int getRow() { return row; }
    public int getCol() { return col; }
    public CellType getType() { return type; }


    public void setType(CellType type) { this.type = type; }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public boolean isWalkable() {
        return type != CellType.OBSTACLE;
    }

    // בתוך המחלקה Cell

// ... (השדות, הבנאי, והמתודות הקיימות שלך) ...

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // אם זה אותו אובייקט בדיוק בזיכרון
        if (o == null || getClass() != o.getClass()) return false; // אם האובייקט המושווה הוא null או מטיפוס אחר
        Cell cell = (Cell) o; // המרה בטוחה לטיפוס Cell
        // שני תאים שווים אם יש להם אותה שורה ואותה עמודה
        return row == cell.row && col == cell.col;
    }

    @Override
    public int hashCode() {
        // חישוב hashCode שמבוסס על השדות שמשמשים ב-equals()
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ") - " + type;
    }
}