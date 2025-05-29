import java.util.List;

public enum PersonStatus {
    IDLE,          // מצב התחלתי לפני הקצאה
    ASSIGNED,      // הוקצה למקלט (לאחר Backtracking), עדיין לא בתנועה
    MOVING,        // בתנועה לאורך המסלול
    REACHED_SHELTER, // הגיע בהצלחה למקלט
    UNASSIGNED,    // לא הוקצה למקלט (לאחר Backtracking)
    STUCK,     // לא יכול לזוז / נתקע / נגמר הזמן
}

