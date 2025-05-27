
public class Fantasma extends Enemy {
    public Fantasma(int x, int y, int health, int damage) {
        super(x, y, health, damage, 5);
    }

    @Override
    public void moveTowardsPlayer(Player player) {
        int dx = Integer.compare(player.getX(), x);
        int dy = Integer.compare(player.getY(), y);
        int newX = x + dx;
        int newY = y + dy;
        GameLogic logic = GameLogic.getInstance();
        for (Trap t : logic.getTraps()) {
            if (t.getX() == newX && t.getY() == newY && t.isActive() && t.getType().equals("Pitfall")) {
                health = 0;
                logic.addGameMessage("Fantasma cae en una trampa y muere.");
                return;
            }
        }
        if (newX >= 0 && newX < GameLogic.BOARD_SIZE) {
            x = newX;
        }
        if (newY >= 0 && newY < GameLogic.BOARD_SIZE) {
            y = newY;
        }
    }

    @Override
    public void attack(Player player) {
        if (Math.abs(player.getX() - x) <= range && Math.abs(player.getY() - y) <= range) {
            player.setHealth(player.getHealth() - damage);
            GameLogic.getInstance().addGameMessage("Fantasma ataca a " + player.getName() + " (-" + damage + " HP).");
        }
    }
}