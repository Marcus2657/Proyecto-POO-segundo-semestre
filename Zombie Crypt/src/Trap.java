public class Trap {
    private String type;
    private int x, y;
    private boolean isActive;

    public Trap(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.isActive = true;
    }

    public String getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void checkConflictWithDoor(int doorX, int doorY) throws IllegalArgumentException {
        if (this.x == doorX && this.y == doorY) {
            throw new IllegalArgumentException("Trap cannot be placed on the same position as a door.");
        }
    }
}