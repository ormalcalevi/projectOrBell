import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {


    public static void main(String[] args) {


        int rows = 10;
        int cols = 10;
        int shelters=3;
        int peoples=17;
        int obstacles=25;
        int totalCapacity=15;
        long seed = 12345L;
        int maxSteps=10;
        informationManagement currentScenario = new informationManagement(rows, cols, peoples,shelters,totalCapacity,obstacles,seed,maxSteps);

        AllocationSolver sol = new AllocationSolver();

        if(currentScenario.getShelterList() == null){
            System.out.println("nullllllllllll");
        }
        else{
            Map<Person , Shelter > bestAssignment =  sol.solve(currentScenario.getShelterList() , currentScenario.getMap() , currentScenario.getMaxSteps() , currentScenario.getPeopleList() , currentScenario.getOptionalShelters());

            System.out.println("\n--- Best Assignment Found ---");
            if (bestAssignment.isEmpty()) {
                System.out.println("No assignment possible or no people/shelters.");
            } else {
                for (Map.Entry<Person, Shelter> entry : bestAssignment.entrySet()) {
                    Person person = entry.getKey();
                    Shelter shelter = entry.getValue();
                    int distance = sol.manhattanDistance(person.getCurrentLocation(), shelter.getLocation()); // השתמשי בפונקציית המנהטן שלך
                    System.out.println("Person " + person.getId() + " assigned to Shelter " + shelter.getId() +
                            " (Location: " + shelter.getLocation() + ", Distance: " + distance + ")");
                }
            }
            System.out.println("Total people saved: " + sol.getMaxPeopleSavedSoFar());
            System.out.println("Best total Manhattan distance: " + sol.getBestTotalManhattanDistance());


            System.out.println("\n--- People NOT Assigned ---");
            List<Person> allPeopleFromScenario = currentScenario.getPeopleList(); // שלפי את רשימת כל האנשים המקורית
            int notAssignedCount = 0;
            for (Person person : allPeopleFromScenario) {
                if (!bestAssignment.containsKey(person)) { // בדקי אם האדם הזה *לא* נמצא במפת השיבוצים
                    notAssignedCount++;
                    System.out.print("Person " + person.getId() + " at " + person.getCurrentLocation() + " was NOT assigned. ");

                    // (אופציונלי) הדפסת מידע נוסף על למה הוא לא שובץ, אם תרצי
                    List<Shelter> optionalSheltersForThisPerson = currentScenario.getOptionalShelters().get(person);
                    if (optionalSheltersForThisPerson == null || optionalSheltersForThisPerson.isEmpty()) {
                        System.out.println("Reason: Had no optional shelters in range from the start.");
                    } else {
                        boolean foundAvailableShelterInOptionalList = false;
                        for (Shelter potentialShelter : optionalSheltersForThisPerson) {
                            // חשוב לבדוק את הקיבולת הנותרת של המקלט *במצב הסופי של bestAssignment*
                            int finalOccupancy = 0;
                            for(Shelter assignedS : bestAssignment.values()){
                                if(assignedS.getId().equals(potentialShelter.getId())){ // השוואה לפי ID
                                    finalOccupancy++;
                                }
                            }
                            if (potentialShelter.getTotalCapacity() - finalOccupancy > 0) {
                                foundAvailableShelterInOptionalList = true;
                                break;
                            }
                        }
                        if (!foundAvailableShelterInOptionalList) {
                            System.out.println("Reason: All optional shelters in range were full in the final assignment.");
                        } else {
                            System.out.println("Reason: Not selected by the assignment algorithm (other constraints or better overall solution without this person).");
                        }
                    }
                }
            }
            if (notAssignedCount == 0) {
                System.out.println("All people were assigned (if possible given total capacity).");
            } else {
                System.out.println("Total people not assigned: " + notAssignedCount);
            }

            grafiIhope(currentScenario.getMap(),currentScenario.getShelterList(),currentScenario.getPeopleList() , bestAssignment);
        }
System.out.println("-----------------------------------------------------------");
    for(Person p: currentScenario.getPeopleList()){
        System.out.println(p.getAssignedShelter());
    }
        System.out.println("-----------------------------------------------------------");

    }

    // שם הפונקציה והפרמטרים נראים בסדר, למעט סוג המפה של assignment.
// Map<Shelter, Person> assignment הוא הפוך ממה שאנחנו צריכים.
// אנחנו צריכים Map<Person, Shelter> כדי לדעת לאן כל אדם הולך.
    public static void grafiIhope(GridMap map, List<Shelter> shelterList, List<Person> personList, Map<Person, Shelter> assignment) { // שיניתי את סוג המפה
        // הגודל 10x10 הוא קבוע כאן, עדיף לקבל אותו מה-GridMap map
        int numRows = 10; // קבלת מספר השורות מהמפה
        int numCols = 10;   // קבלת מספר העמודות מהמפה

        for (int i = 0; i < numRows; i++) { // לולאה על השורות
            for (int j = 0; j < numCols; j++) { // לולאה על העמודות
                Cell currentCellFromMap = map.getCell(i, j); // קבל את התא הנוכחי מהמפה
                String cellOutput = "..."; // ברירת מחדל לתא ריק (או משהו דומה)
                boolean printedSomethingForThisCell = false; // דגל שיעזור לנו לדעת אם כבר הדפסנו משהו לתא הזה

                // 1. בדוק אם יש מקלט בתא הזה
                for (Shelter s : shelterList) {
                    if (s.getLocation() != null && s.getLocation().getRow() == i && s.getLocation().getCol() == j) {
                        cellOutput = String.format("%-5s", s.getId()); // הדפס ID של מקלט, מרופד ל-5 תווים
                        printedSomethingForThisCell = true;
                        break; // מצאנו מקלט בתא הזה, אין צורך להמשיך לבדוק מקלטים אחרים
                    }
                }

                // 2. אם לא הודפס מקלט, בדוק אם יש אדם בתא הזה (מיקום התחלתי)
                if (!printedSomethingForThisCell) {
                    for (Person p : personList) {
                        if (p.getCurrentLocation() != null && p.getCurrentLocation().getRow() == i && p.getCurrentLocation().getCol() == j) {
                            if (assignment.containsKey(p)) { // בדוק אם האדם הזה משובץ
                                Shelter assignedShelter = assignment.get(p);
                                cellOutput = String.format("%-5s", p.getId() + ">" + assignedShelter.getId().charAt(1)); // P1>1 (התו השני של ID המקלט)
                            } else {
                                cellOutput = String.format("%-5s", p.getId() + "(X)"); // אדם לא משובץ
                            }
                            printedSomethingForThisCell = true;
                            break; // מצאנו אדם בתא הזה
                        }
                    }
                }

                // 3. אם לא הודפס מקלט או אדם, בדוק אם זה מכשול (לפי סוג התא מהמפה)
                if (!printedSomethingForThisCell && currentCellFromMap.getType() == CellType.OBSTACLE) {
                    cellOutput = String.format("%-5s", "###");
                    printedSomethingForThisCell = true;
                }

                // 4. אם לא הודפס כלום עד עכשיו, זה תא ריק (או סוג אחר אם יש לך)
                //   (הערך "... " כבר נמצא ב-cellOutput כברירת מחדל אם printedSomethingForThisCell עדיין false)
                if (!printedSomethingForThisCell && currentCellFromMap.getType() == CellType.EMPTY) {
                    cellOutput = String.format("%-5s", "...");
                }


                System.out.print(cellOutput + " "); // הדפס את ייצוג התא עם רווח אחריו
            }
            System.out.println(); // ירידת שורה בסוף כל שורת מפה
        }
    }
}