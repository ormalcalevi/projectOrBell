import java.util.*;

public class AllocationSolver {
    private int maxPeopleSavedSoFar;
    private Map<Person, Shelter> bestAssignmentSoFar;
    private int bestTotalManhattanDistance;
    private GridMap map;
    private List<Shelter> sheltersList;
    private List<Person> peopleList;
    private int maxSteps;
    private Map<Person, List<Shelter>> optionalSheltersForEachPerson;
    private long backtrackCallCount;
    private long pruningActivationCount ;
    private long potentialImmediateCallsAvoided;


    public Map<Person, Shelter> solve(List<Shelter> sheltersList,List<Person>peopleList, GridMap map, int maxSteps, List<Person> initialPeopleList, Map<Person, List<Shelter>> optionalShelters) {
        setSheltersList(sheltersList);
        setPeopleList(peopleList);
        setMap(map);
        setMaxSteps(maxSteps);
        this.bestAssignmentSoFar = new HashMap<>();
        setMaxPeopleSavedSoFar(0);
        setBacktrackCallCount(0);
        setPruningActivationCount(0);
        setPotentialImmediateCallsAvoided(0);
        setBestTotalManhattanDistance(Integer.MAX_VALUE);
        setOptionalSheltersForEachPerson(optionalShelters);

        resetShelters(this.sheltersList);

        List<Person> unassignedPeople = new ArrayList<>(initialPeopleList);

        backtrackRecursiveHelper(unassignedPeople, new HashMap<>());
        routeUnassignedToSafePoints(this.peopleList ,this.bestAssignmentSoFar,this.optionalSheltersForEachPerson );

        return this.bestAssignmentSoFar;
    }

    public long getPotentialImmediateCallsAvoided() {
        return potentialImmediateCallsAvoided;
    }

    public void setPotentialImmediateCallsAvoided(long potentialImmediateCallsAvoided) {
        this.potentialImmediateCallsAvoided = potentialImmediateCallsAvoided;
    }

    public long getPruningActivationCount() {
        return pruningActivationCount;
    }

    public void setPruningActivationCount(long pruningActivationCount) {
        this.pruningActivationCount = pruningActivationCount;
    }

    public long getBacktrackCallCount() {
        return backtrackCallCount;
    }

    public void setBacktrackCallCount(long backtrackCallCount) {
        this.backtrackCallCount = backtrackCallCount;
    }

    public void setSheltersList(List<Shelter> sheltersList) {
        this.sheltersList = sheltersList;
    }

    public void setMap(GridMap map) {
        this.map = map;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public void setBestAssignmentSoFar(Map<Person, Shelter> bestAssignmentSoFar) {
        this.bestAssignmentSoFar = bestAssignmentSoFar;
    }

    public int getMaxPeopleSavedSoFar() {
        return maxPeopleSavedSoFar;
    }

    public void setMaxPeopleSavedSoFar(int maxPeopleSavedSoFar) {
        this.maxPeopleSavedSoFar = maxPeopleSavedSoFar;
    }

    public int getBestTotalManhattanDistance() {
        return bestTotalManhattanDistance;
    }

    public void setBestTotalManhattanDistance(int bestTotalManhattanDistance) {
        this.bestTotalManhattanDistance = bestTotalManhattanDistance;
    }

    public void setOptionalSheltersForEachPerson(Map<Person, List<Shelter>> optionalShelters) {
        if (optionalShelters == null || optionalShelters.isEmpty()) {
            System.out.println("optionalShelters IN NUL");
        } else {
            this.optionalSheltersForEachPerson = optionalShelters;

        }
    }

    public void setPeopleList(List<Person> peopleList) {
        this.peopleList = peopleList;
    }

    private void resetShelters(List<Shelter> sheltersList) {
        for (Shelter shelter : sheltersList) {
            shelter.resetOccupancy();
        }
    }

    public void backtrackRecursiveHelper(List<Person> unassignedPeople, Map<Person, Shelter> currentAssignment) {
        backtrackCallCount++;

        // stage 1 : pruning
        boolean mustPrune = shouldPrune(unassignedPeople, currentAssignment);

        if (mustPrune) {
            if (!unassignedPeople.isEmpty()) {
                Person tempChosenPerson = selectNextPersonToAssign(new ArrayList<>(unassignedPeople));
                if (tempChosenPerson != null) {
                    List<Shelter> tempShelters = getValidAndSortedSheltersForPerson(tempChosenPerson);
                    this.potentialImmediateCallsAvoided += (tempShelters.size() + 1);
                }
            }
        } else {      // (mustPrune == false)
            // stage 2 : (Base Case)
            if (unassignedPeople.isEmpty()) {
                evaluateAndStoreSolution(currentAssignment);
            } else {
                // stage 3 : select the next person
                Person chosenPerson = selectNextPersonToAssign(unassignedPeople);

                //Step 4: Assignment for the selected person
                List<Person> nextUnassignedPeople = new ArrayList<>(unassignedPeople);
                nextUnassignedPeople.remove(chosenPerson);
                List<Shelter> sortedPossibleShelters = getValidAndSortedSheltersForPerson(chosenPerson);

                for (Shelter shelter : sortedPossibleShelters) {
                    currentAssignment.put(chosenPerson, shelter);
                    shelter.addOccupant();
                    backtrackRecursiveHelper(nextUnassignedPeople, currentAssignment); // קריאה רקורסיבית
                    currentAssignment.remove(chosenPerson);
                    shelter.decrementOccupancy();
                }

                //stage 5 :It represents the choice to leave 'personToAssign' unassigned and
                // find the best possible assignment for the 'nextUnassignedPeople' under that condition.
                backtrackRecursiveHelper(nextUnassignedPeople, currentAssignment);
            }
        }
    }


    private boolean shouldPrune(List<Person> unassignedPeople, Map<Person, Shelter> currentAssignment) {
        if (calculatePotentialMax(unassignedPeople, this.sheltersList, currentAssignment) <= this.maxPeopleSavedSoFar) {
            this.pruningActivationCount++;
            return true;
        }
        return false; // true אם צריך לגזום
    }

    private int calculatePotentialMax(List<Person> unassignedPeople, List<Shelter> allShelters, Map<Person, Shelter> currentAssignment) {
        int saveSoFarInCurrentPath = currentAssignment.size();

        // total remaining capacity in the path

        int totalRemainingCapacityInCurrentPath = 0;
        for (Shelter shelter : allShelters) {
            if (shelter.getRemainingCapacity() > 0) {
                totalRemainingCapacityInCurrentPath += shelter.getRemainingCapacity();
            }
        }
        // potential additional saves

        int potentialAdditionalSaves = Math.min(unassignedPeople.size(), totalRemainingCapacityInCurrentPath);
        return saveSoFarInCurrentPath + potentialAdditionalSaves;


    }

    private void evaluateAndStoreSolution(Map<Person, Shelter> currentAssignment) {
        //The current assignment allocates more people than the peak
        if (currentAssignment.size() > this.maxPeopleSavedSoFar) {
            setMaxPeopleSavedSoFar(currentAssignment.size());
            setBestAssignmentSoFar(new HashMap<>(currentAssignment));
            setBestTotalManhattanDistance(calculateTotalManhattanDistanceForAssignment(currentAssignment));
        }

        //The current assignment saves the same number of people from the peak: checking who is better using the total distance
        else if (currentAssignment.size() == this.maxPeopleSavedSoFar && currentAssignment.size() > 0) {
            int currentDistance = calculateTotalManhattanDistanceForAssignment(currentAssignment);
            if (currentDistance < this.bestTotalManhattanDistance) {
                setBestAssignmentSoFar(new HashMap<>(currentAssignment));
                setBestTotalManhattanDistance(currentDistance);
            }
        }

    }

    private int calculateTotalManhattanDistanceForAssignment(Map<Person, Shelter> assignment) {
        int totalDistance = 0;
        //Calculate the Manhattan distance between the person's current location and the location of the shelter, and sum all these distances.
        for (Map.Entry<Person, Shelter> entry : assignment.entrySet()) {
            Person person = entry.getKey();
            Shelter shelter = entry.getValue();

            Cell personLocation = person.getCurrentLocation();
            Cell shelterLocation = shelter.getLocation();

            if (personLocation != null && shelterLocation != null) {
                totalDistance += manhattanDistance(personLocation, shelterLocation);
            } else {
                System.out.println("person or shalter in assignment has null : " + person.getId() + " || " + shelter.getId());
                totalDistance += Integer.MAX_VALUE / assignment.size();//הוספת קננס גדול בשביל להראות את הבעייתיות וכך השיבוץ יזוהה כשיבוץ פחות טוב
            }
        }
        return totalDistance;
    }

    public int manhattanDistance(Cell c1, Cell c2) {
        return Math.abs(c1.getRow() - c2.getRow()) + Math.abs(c1.getCol() - c2.getCol());
    }

    private Person selectNextPersonToAssign(List<Person> unassignedPeople) {

        int minOptionsFound = Integer.MAX_VALUE;
        List<Person> candidatesWithMinOptions = new ArrayList<>();

        //check if there is remaining capacity in the shelters
        for (Person person : unassignedPeople) {
            int validOptionsCount = 0;
            List<Shelter> personSpecificPotentialShelters = this.optionalSheltersForEachPerson.get(person);

            if (personSpecificPotentialShelters != null && !personSpecificPotentialShelters.isEmpty()) {
                for (Shelter shelter : personSpecificPotentialShelters) {
                    if (shelter.getRemainingCapacity() > 0) {
                        validOptionsCount++;
                    }
                }
            }


            if (validOptionsCount < minOptionsFound) {
                minOptionsFound = validOptionsCount;
                candidatesWithMinOptions.clear();
                candidatesWithMinOptions.add(person);
            } else if (validOptionsCount == minOptionsFound) {
                candidatesWithMinOptions.add(person);
            }
        }

        //If there is only one person on the list - pull
        //
        //Return the first person on the list
        if (candidatesWithMinOptions.size() == 1) {//לכן אין שובר שיוויון כי יש רק מתמודד אחד
            return candidatesWithMinOptions.get(0);
        }

        //אם ברשימה יש כמה אנשים צריך להחליט איזה אדם יוחזר מהפונקציה:
        // ניתן ליצור פונקציה שתשתמש ברמת סיכון עבור כל אדם וכך להחליט לפי רמת הסיכון הגבוהה ביותר
        // כרגע נעשה לפי הוצאת האדם הראשון ברשימה
        else {
            return candidatesWithMinOptions.get(0);
        }

    }

    private List<Shelter> getValidAndSortedSheltersForPerson(Person person) {

        List<Shelter> optionalSheltersForThisPerson = this.optionalSheltersForEachPerson.get(person);
        List<Shelter> validSheltersWithCapacity = new ArrayList<>();

        if (optionalSheltersForThisPerson != null && !optionalSheltersForThisPerson.isEmpty()) {
            for (Shelter shelter : optionalSheltersForThisPerson) {
                if (shelter.getRemainingCapacity() > 0) {
                    validSheltersWithCapacity.add(shelter);
                }
            }
        }

        if (!validSheltersWithCapacity.isEmpty()) {
            validSheltersWithCapacity.sort((s1, s2) -> {
                //Check which distance is greater
                int dist1 = manhattanDistance(person.getCurrentLocation(), s1.getLocation());
                int dist2 = manhattanDistance(person.getCurrentLocation(), s2.getLocation());
                if (dist1 != dist2) {
                    return Integer.compare(dist1, dist2);
                }

                ////If there are equals: check which remaining capacity is greater
                int remainingCapacity1 = s1.getRemainingCapacity();
                int remainingCapacity2 = s2.getRemainingCapacity();
                if (remainingCapacity1 != remainingCapacity2) {
                    return Integer.compare(remainingCapacity2, remainingCapacity1);//Desc sort
                }
                return 0;
            });
        }
        return validSheltersWithCapacity;
    }


    private void routeUnassignedToSafePoints(List<Person> allPeople, Map<Person, Shelter> bestAssignment,
                                             Map<Person, List<Shelter>> optionalSheltersMap) {

        for (Person person : allPeople) {
            // בדוק אם האדם לא שובץ למקלט רשמי
            if ( !bestAssignment.containsKey(person) || bestAssignment.get(person) == null) {
                person.setAssignedShelter(null); // אין לו שיוך רשמי

                List<Shelter> personOptionalShelters = optionalSheltersMap.get(person);
                Cell alternativeGoal = null;

                if (personOptionalShelters != null && !personOptionalShelters.isEmpty()) {
                    Shelter shelterNear = personOptionalShelters.get(0);

                    Cell shelterLoc = shelterNear.getLocation();
                    alternativeGoal=searchForFreeCell(shelterLoc);


                }

                if (alternativeGoal != null) {
                    person.setAlternativeGoalCell(alternativeGoal);
                } else {
                    person.setAlternativeGoalCell(null);
                }
            }
        }
    }

    private Cell searchForFreeCell(Cell shelterLoc ){
//ניתן להוסיףף בדיקה אם המשתנה הזה הוא לא NULL
        Cell alternativeGoal = null;
        boolean alternativeGoalFound = false;



        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int i = 0; i < directions.length && !alternativeGoalFound; i++) {
            int targetRow = shelterLoc.getRow() + directions[i][0];
            int targetCol = shelterLoc.getCol() + directions[i][1];

            if (map.isValid(targetRow, targetCol)) {
                Cell potentialCell = map.getCell(targetRow, targetCol);
                if (potentialCell.isWalkable() && potentialCell.getType() != CellType.SHELTER) {
                    alternativeGoal = potentialCell;
                    alternativeGoalFound = true;
                }
            }
        }
        return alternativeGoal;
    }
}