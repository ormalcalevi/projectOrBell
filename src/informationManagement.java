
import java.util.*;

public class informationManagement {


    private GridMap map;
    private List<Person> peopleList;
    private List<Shelter> shelterList;
    private int rows, cols, numPeople, numShelters, totalCapacity, numObstacles,maxSteps;
    private long randomSeed;
    private Map<Person , List<Shelter>> optionalShelters;//for each person in the map


    public informationManagement(int rows, int cols, int numPeople, int numShelters, int totalCapacity, int numObstacles, long randomSeed,int maxSteps) {
        setCols(cols);
        setRows(rows);
        setNumObstacles(numObstacles);
        setNumPeople(numPeople);
        setNumShelters(numShelters);
        setTotalCapacity(totalCapacity);
        setRandomSeed(randomSeed);
        setMaxSteps(maxSteps);
        this.optionalShelters = new HashMap<>();

        generateScenario();
        SearchForOptionalShelters(this.map,this.peopleList,this.shelterList,this.maxSteps);
    }
    private void generateScenario(){
        this.map = new GridMap(this.rows, this.cols, this.randomSeed );
        this.map.placeObstacles(this.numObstacles);

        this.shelterList = createShelters(this.numShelters, this.totalCapacity, this.randomSeed);
        placeSheltersOnMap(this.map, this.shelterList);

        this.peopleList = createPeople(this.numPeople);
        placePeopleOnMap(this.map, this.peopleList);
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public GridMap getMap() {
        return map;
    }

    public void setMap(GridMap map) {
        this.map = map;
    }

    public int getNumObstacles() {
        return numObstacles;
    }

    public void setNumObstacles(int numObstacles) {
        this.numObstacles = numObstacles;
    }

    public int getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public int getNumShelters() {
        return numShelters;
    }

    public void setNumShelters(int numShelters) {
        this.numShelters = numShelters;
    }

    public List<Person> getPeopleList() {
        return peopleList;
    }

    public void setPeopleList(List<Person> peopleList) {
        this.peopleList = peopleList;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public List<Shelter> getShelterList() {
        return shelterList;
    }

    public void setShelterList(List<Shelter> shelterList) {
        this.shelterList = shelterList;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public Map<Person, List<Shelter>> getOptionalShelters() {
        return optionalShelters;
    }

    public void setOptionalShelters(Map<Person, List<Shelter>> optionalShelters) {
        optionalShelters = optionalShelters;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public static List<Shelter> createShelters(int numShelter, int totalCapacity, long random){
        List<Shelter>shelterList=new ArrayList<>();
        if (numShelter <= 0) return shelterList;
        Random capacityRandom = new Random(random); // שימוש ב-seed שהתקבל

        int[] capacities = new int[numShelter];//רשימה עבור כל המלקטים וכמה קיבולת יש בכל אחת

        for (int i = 0; i < totalCapacity; i++) {
            int randomIndex = capacityRandom.nextInt(numShelter);
            capacities[randomIndex]++;
        }

        for (int i = 0; i < numShelter; i++) {
            String shelterId = "S" + (i + 1); // יצירת ID
            shelterList.add(new Shelter(capacities[i], shelterId));
        }
        return shelterList;

    }

    public static List<Person> createPeople(int numPeople){
        List<Person>peopleList=new ArrayList<>();
        if (numPeople <= 0) return peopleList;

        for (int i = 0; i < numPeople; i++) {
            String personId = "P" + (i + 1); // יצירת ID
            peopleList.add(new Person(personId));
        }
        return peopleList;
    }

    public static void placeSheltersOnMap(GridMap map ,List<Shelter> sheltersList){
        if (map == null || sheltersList == null || sheltersList.isEmpty()) return;

        for (Shelter shelter : sheltersList) {
            Cell emptyCell = map.findRandomEmptyCell();
            if (emptyCell != null) {
                boolean success = map.setCellType(emptyCell.getRow(), emptyCell.getCol(), CellType.SHELTER);//שינוי למקלט
                if (success) {

                    shelter.setLocation(emptyCell);

                    //System.out.println("מיקום: " + shelter.getId() + " ב-" + emptyCell); // הדפסת בדיקה
                }
                else {
                    //בדיקת ביטחון
                    System.err.println("שגיאה: התא שנמצא עבור " + shelter.getId() + " אינו ריק!");
                }
            } else {
                // emptyCell==null->There are no more empty cells on the map.
                return;
            }
        }
    }

    public void placePeopleOnMap(GridMap map, List<Person>peopleList){
        if (this.map == null || peopleList == null || peopleList.isEmpty()) return;

        for (Person person : peopleList) {
            Cell emptyCell = map.findRandomEmptyCell();
            if (emptyCell != null) {

                if (map.getCell(emptyCell.getRow(), emptyCell.getCol()).getType() == CellType.EMPTY) {
                    person.setCurrentLocation(emptyCell);
                }

            } else {
                // emptyCell==null->There are no more empty cells on the map.
                return;
            }
        }
    }

    public void SearchForOptionalShelters(GridMap map ,List<Person>peopleList, List<Shelter>shelterList, int maxSteps){
        if (map == null || peopleList == null ||shelterList == null || shelterList.isEmpty() || peopleList.isEmpty()) {
           System.out.println("problem");
            return;
        }

        for (Person person:peopleList){
            List<Shelter> personSpecificAvailableShelters = new ArrayList<>();
            Cell personLoc=person.getCurrentLocation();
            if (personLoc == null) {
                System.err.println("Person " + person.getId() + " has no location.");
                this.optionalShelters.put(person,personSpecificAvailableShelters);
            }
            else {
                int personRow = personLoc.getRow();
                int personCol = personLoc.getCol();

                for (Shelter shelter : shelterList) {
                    Cell shelterLoc = shelter.getLocation();
                    if (shelterLoc != null) {
                        int rOffset = shelterLoc.getRow() - personRow;
                        int cOffset = shelterLoc.getCol() - personCol;

                        int manhattanDistance = Math.abs(rOffset) + Math.abs(cOffset);
                        if (manhattanDistance <= maxSteps) {
                                // found
                           personSpecificAvailableShelters.add(shelter);
                        }

                    }
                    else{
                        System.out.println("shelter loc in null");
                    }

                }
            }
            personSpecificAvailableShelters.sort(Comparator.comparingInt(shelter ->
                    manhattanDistance(personLoc, shelter.getLocation())
            ));

        this.optionalShelters.put(person, personSpecificAvailableShelters);


        }


    }

    private int manhattanDistance(Cell c1, Cell c2) {
        if (c1 == null || c2 == null) return Integer.MAX_VALUE;
        return Math.abs(c1.getRow() - c2.getRow()) + Math.abs(c1.getCol() - c2.getCol());
    }
}
