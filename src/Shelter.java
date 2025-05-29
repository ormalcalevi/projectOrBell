public class Shelter {
    private String id;
    private Cell location;
    private int totalCapacity;
    private int currentOccupancy;

    public Shelter(int totalCapacity, String id) {
        setCurrentOccupancy(0);
        setId(id);
        setLocation(null);
        setTotalCapacity(totalCapacity);
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(int currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cell getLocation() {
        return location;
    }

    public void setLocation(Cell location) {
        this.location = location;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        if (totalCapacity < 0) {
            this.totalCapacity = 0;
        } else {
            this.totalCapacity = totalCapacity;
        }
    }

    public int getRemainingCapacity(){
        return totalCapacity - currentOccupancy;
    }

    public boolean isFull(){
        return this.currentOccupancy >= totalCapacity;
    }

    public void resetOccupancy(){
        setCurrentOccupancy(0);
    }

    public void addOccupant() {
        if (this.currentOccupancy < this.totalCapacity) { // בדוק אם יש עוד מקום
            this.currentOccupancy++;
        } else {
            System.err.println("Shelter " + getId() + " is full. Cannot add more occupants. Current: " + this.currentOccupancy + ", Capacity: " + this.totalCapacity);
        }
    }

    public void decrementOccupancy() {
        if (this.currentOccupancy > 0) {
            this.currentOccupancy--;
        } else {
            System.err.println("Warning: Attempted to decrement occupancy below zero for shelter " + this.id);
        }
    }



}
