
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
public class Player {
    private String name;
    private String classType;
    private int health;
    private int maxHealth;
    private int x, y;
    private int damage;
    private int maxMoves;
    private int range;
    private boolean hasSpecialUsed;
    private boolean hasSecondSpecialUsed;
    private boolean isInvisible;
    private Random random = new Random();

    public Player(String name, String classType, int x, int y) {
        this.name = name;
        this.classType = classType;
        this.x = x;
        this.y = y;
        this.hasSpecialUsed = false;
        this.hasSecondSpecialUsed = false;
        this.isInvisible = false;
        initializeStats();
    }

    private void initializeStats() {
        switch (classType) {
            case "Cazador de Zombies":
                health = maxHealth = 100;
                damage = 30;
                maxMoves = 3;
                range = 1;
                break;
            case "Médico de Campo":
                health = maxHealth = 80;
                damage = 10;
                maxMoves = 4;
                range = 3;
                break;
            case "Nigromante":
                health = maxHealth = 70;
                damage = 20;
                maxMoves = 3;
                range = 5;
                break;
            case "Caballero Fantasma":
                health = maxHealth = 120;
                damage = 35;
                maxMoves = 2;
                range = 1;
                break;
            case "Ladrón de Tumbas":
                health = maxHealth = 90;
                damage = 15;
                maxMoves = 5;
                range = 3;
                break;
            case "Bruja Oscura":
                health = maxHealth = 70;
                damage = 20;
                maxMoves = 3;
                range = 5;
                break;
            case "Zombie Amistoso":
                health = maxHealth = 100;
                damage = 10;
                maxMoves = 3;
                range = 1;
                break;
            case "Explorador":
                health = maxHealth = 90;
                damage = 15;
                maxMoves = 6;
                range = 3;
                break;
            case "Sacerdote":
                health = maxHealth = 80;
                damage = 10;
                maxMoves = 3;
                range = 5;
                break;
            case "Gólem de Piedra":
                health = maxHealth = 150;
                damage = 30;
                maxMoves = 2;
                range = 1;
                break;
            case "Artillero":
                health = maxHealth = 90;
                damage = 25;
                maxMoves = 3;
                range = 5;
                break;
            case "Alquimista":
                health = maxHealth = 80;
                damage = 15;
                maxMoves = 4;
                range = 3;
                break;
            case "Berserker":
                health = maxHealth = 130;
                damage = 35;
                maxMoves = 3;
                range = 1;
                break;
            case "Hechicero del Viento":
                health = maxHealth = 70;
                damage = 20;
                maxMoves = 4;
                range = 5;
                break;
            case "Cazador de Sombras":
                health = maxHealth = 90;
                damage = 20;
                maxMoves = 5;
                range = 3;
                break;
            case "Paladín":
                health = maxHealth = 110;
                damage = 25;
                maxMoves = 3;
                range = 1;
                break;
            case "Druida":
                health = maxHealth = 80;
                damage = 15;
                maxMoves = 4;
                range = 5;
                break;
            case "Ingeniero":
                health = maxHealth = 90;
                damage = 20;
                maxMoves = 3;
                range = 3;
                break;
            case "Monje":
                health = maxHealth = 100;
                damage = 20;
                maxMoves = 4;
                range = 1;
                break;
            case "Arcanista":
                health = maxHealth = 10000;
                damage = 5000;
                maxMoves = 30;
                range = 50;
                break;
            case "Esqueleto Aliado":
                health = maxHealth = 50;
                damage = 15;
                maxMoves = 2;
                range = 1;
                break;
            default:
                health = maxHealth = 100;
                damage = 20;
                maxMoves = 3;
                range = 1;
        }
    }

    public void attack(Enemy enemy) {
        GameLogic logic = GameLogic.getInstance();
        boolean isMagical = classType.equals("Nigromante") || classType.equals("Bruja Oscura") ||
                classType.equals("Sacerdote") || classType.equals("Hechicero del Viento") ||
                classType.equals("Druida") || classType.equals("Arcanista");
        if (enemy instanceof Fantasma && !isMagical) {
            logic.addGameMessage(name + " no puede atacar a un Fantasma con un ataque no mágico.");
            return;
        }
        if (enemy instanceof EsqueletoArmado && Math.abs(enemy.getX() - x) <= 1 && Math.abs(enemy.getY() - y) <= 1) {
            enemy.setHealth(enemy.getHealth() - damage * 2);
            logic.addGameMessage(name + " ataca a Esqueleto Armado (-" + (damage * 2) + " HP).");
        } else {
            switch (classType) {
                case "Cazador de Zombies":
                    for (Enemy e : new ArrayList<>(logic.getEnemies())) {
                        if (Math.abs(e.getX() - x) <= 2 && Math.abs(e.getY() - y) <= 2) {
                            e.setHealth(e.getHealth() - damage);
                            logic.addGameMessage(name + " ataca a enemigo (-" + damage + " HP).");
                        }
                    }
                    break;
                case "Caballero Fantasma":
                    enemy.setHealth(enemy.getHealth() - damage);
                    logic.addGameMessage(name + " ataca a enemigo (-" + damage + " HP).");
                    break;
                case "Bruja Oscura":
                    enemy.setHealth(enemy.getHealth() - (damage + 5));
                    logic.addGameMessage(name + " ataca a enemigo (-" + (damage + 5) + " HP).");
                    break;
                case "Gólem de Piedra":
                    for (Enemy e : new ArrayList<>(logic.getEnemies())) {
                        if (Math.abs(e.getX() - x) <= 1 && Math.abs(e.getY() - y) <= 1) {
                            e.setHealth(e.getHealth() - damage);
                            logic.addGameMessage(name + " ataca a enemigo (-" + damage + " HP).");
                        }
                    }
                    break;
                case "Sacerdote":
                    if (enemy.getHealth() > 0) {
                        enemy.setHealth(0);
                        logic.addGameMessage(name + " elimina instantáneamente a enemigo.");
                    }
                    break;
                default:
                    enemy.setHealth(enemy.getHealth() - damage);
                    logic.addGameMessage(name + " ataca a enemigo (-" + damage + " HP).");
            }
        }
    }

    public void useSpecial() {
        if (hasSpecialUsed) {
            GameLogic.getInstance().addGameMessage(name + " ya usó su habilidad especial.");
            return;
        }
        GameLogic logic = GameLogic.getInstance();
        try {
            switch (classType) {
                case "Cazador de Zombies":
                    boolean trapExists = false;
                    for (Trap t : logic.getTraps()) {
                        if (t.getX() == x && t.getY() == y && t.isActive()) {
                            trapExists = true;
                            break;
                        }
                    }
                    if (!trapExists) {
                        logic.getTraps().add(new Trap("Pitfall", x, y));
                        logic.addGameMessage(name + " coloca una trampa.");
                    } else {
                        logic.addGameMessage(name + " ya hay una trampa aquí.");
                    }
                    break;
                case "Médico de Campo":
                    for (Player p : logic.getPlayers()) {
                        if (p.getHealth() <= 0) {
                            p.setHealth(p.getMaxHealth() / 2);
                            logic.addGameMessage(name + " revive a " + p.getName() + " con mitad de salud.");
                            logic.getTurnOrder().add(p);
                            break;
                        }
                    }
                    break;
                case "Nigromante":
                    int skeletonsSummoned = 0;
                    int[][] offsets = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
                    for (int[] offset : offsets) {
                        int newX = x + offset[0];
                        int newY = y + offset[1];
                        if (newX >= 0 && newX < GameLogic.BOARD_SIZE && newY >= 0 && newY < GameLogic.BOARD_SIZE && !logic.isOccupied(newX, newY)) {
                            Player skeleton = new Player("Esqueleto Aliado " + (skeletonsSummoned + 1), "Esqueleto Aliado", newX, newY);
                            logic.getPlayers().add(skeleton);
                            logic.getTurnOrder().add(skeleton);
                            logic.addGameMessage(name + " invoca un Esqueleto Aliado en (" + newX + "," + newY + ").");
                            skeletonsSummoned++;
                            if (skeletonsSummoned >= 2) break;
                        }
                    }
                    if (skeletonsSummoned == 0) {
                        logic.addGameMessage(name + " no encuentra espacio para invocar esqueletos.");
                    } else {
                        logic.addGameMessage(name + " invoca " + skeletonsSummoned + " esqueletos aliados.");
                    }
                    break;
                case "Caballero Fantasma":
                    int newX, newY;
                    do {
                        newX = random.nextInt(GameLogic.BOARD_SIZE);
                        newY = random.nextInt(GameLogic.BOARD_SIZE);
                    } while (logic.isOccupied(newX, newY));
                    x = newX;
                    y = newY;
                    logic.addGameMessage(name + " se teletransporta.");
                    break;
                case "Ladrón de Tumbas":
                    logic.getObstacles().removeIf(o -> Math.abs(o.x - x) <= 1 && Math.abs(o.y - y) <= 1);
                    logic.addGameMessage(name + " destruye obstáculos cercanos.");
                    break;
                case "Bruja Oscura":
                    for (Enemy e : logic.getEnemies()) {
                        if (Math.abs(e.getX() - x) <= range && Math.abs(e.getY() - y) <= range) {
                            e.setDamage(e.getDamage() / 2);
                            logic.addGameMessage(name + " reduce el daño de un enemigo.");
                            break;
                        }
                    }
                    break;
                case "Zombie Amistoso":
                    if (!logic.getEnemies().isEmpty()) {
                        Enemy e = logic.getEnemies().remove(0);
                        logic.getTurnOrder().remove(e);
                        Player newPlayer = new Player("Zombie Convertido", "Zombie Amistoso", e.getX(), e.getY());
                        logic.getPlayers().add(newPlayer);
                        logic.getTurnOrder().add(newPlayer);
                        logic.addGameMessage(name + " convierte un enemigo en aliado.");
                    }
                    break;
                case "Explorador":
                    for (Trap t : logic.getTraps()) {
                        if (t.isActive() && !t.getType().equals("LockedDoor")) {
                            t.setActive(false);
                        }
                    }
                    logic.addGameMessage(name + " desactiva todas las trampas.");
                    break;
                case "Sacerdote":
                    health = Math.min(maxHealth, health + 20);
                    logic.addGameMessage(name + " se cura 20 HP.");
                    break;
                case "Gólem de Piedra":
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int ox = x + dx;
                            int oy = y + dy;
                            if (ox >= 0 && ox < GameLogic.BOARD_SIZE && oy >= 0 && oy < GameLogic.BOARD_SIZE && !logic.isOccupied(ox, oy)) {
                                logic.getObstacles().add(new Point(ox, oy));
                            }
                        }
                    }
                    logic.addGameMessage(name + " crea una barrera de obstáculos.");
                    break;
                case "Artillero":
                    for (Enemy e : new ArrayList<>(logic.getEnemies())) {
                        if (e.getY() == y && e.getX() > x) {
                            e.setHealth(e.getHealth() - 30);
                        }
                    }
                    logic.getEnemies().removeIf(e -> {
                        if (e.getHealth() <= 0) {
                            logic.getTurnOrder().remove(e);
                            return true;
                        }
                        return false;
                    });
                    logic.addGameMessage(name + " dispara, dañando enemigos en línea recta.");
                    break;
                case "Alquimista":
                    if (logic.getSharedInventory().size() < GameLogic.MAX_INVENTORY_SIZE) {
                        Item potion = random.nextBoolean() ?
                                logic.createItem("Poción de Vida", true, false, 30 + (logic.getCurrentLevel() * 5)) :
                                logic.createItem("Poción de Daño", true, false, 10 + (logic.getCurrentLevel() * 3));
                        logic.getSharedInventory().add(potion);
                        logic.addGameMessage(name + " crea una poción.");
                    }
                    break;
                case "Berserker":
                    damage *= 2;
                    maxHealth /= 2;
                    health = Math.min(health, maxHealth);
                    logic.addGameMessage(name + " duplica su daño, pero reduce su salud máxima.");
                    break;
                case "Hechicero del Viento":
                    for (Enemy e : new ArrayList<>(logic.getEnemies())) {
                        if (Math.abs(e.getX() - x) <= 2 && Math.abs(e.getY() - y) <= 2) {
                            int dx = Integer.compare(e.getX(), x);
                            int dy = Integer.compare(e.getY(), y);
                            int newEX = e.getX() + dx;
                            int newEY = e.getY() + dy;
                            if (newEX >= 0 && newEX < GameLogic.BOARD_SIZE && newEY >= 0 && newEY < GameLogic.BOARD_SIZE && !logic.isOccupied(newEX, newEY)) {
                                e.setX(newEX);
                                e.setY(newEY);
                            }
                        }
                    }
                    logic.addGameMessage(name + " empuja a los enemigos cercanos.");
                    break;
                case "Cazador de Sombras":
                    isInvisible = true;
                    logic.addGameMessage(name + " se vuelve invisible para el próximo turno.");
                    break;
                case "Paladín":
                    for (Player p : logic.getPlayers()) {
                        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 10));
                    }
                    logic.addGameMessage(name + " otorga un escudo a todos los jugadores.");
                    break;
                case "Druida":
                    int treeX, treeY;
                    do {
                        treeX = x + (random.nextBoolean() ? 1 : -1);
                        treeY = y + (random.nextBoolean() ? 1 : -1);
                    } while (treeX < 0 || treeX >= GameLogic.BOARD_SIZE || treeY < 0 || treeY >= GameLogic.BOARD_SIZE || logic.isOccupied(treeX, treeY));
                    Player newPlayer = new Player("Árbol Viviente", "Zombie Amistoso", treeX, treeY);
                    logic.getPlayers().add(newPlayer);
                    logic.getTurnOrder().add(newPlayer);
                    logic.addGameMessage(name + " invoca un Árbol Viviente.");
                    break;
                case "Ingeniero":
                    int trapX, trapY;
                    do {
                        trapX = x + (random.nextBoolean() ? 1 : -1);
                        trapY = y + (random.nextBoolean() ? 1 : -1);
                    } while (trapX < 0 || trapX >= GameLogic.BOARD_SIZE || trapY < 0 || trapY >= GameLogic.BOARD_SIZE || logic.isOccupied(trapX, trapY));
                    logic.getTraps().add(new Trap("Laser", trapX, trapY));
                    logic.addGameMessage(name + " coloca una trampa de láser.");
                    break;
                case "Monje":
                    health = Math.min(maxHealth, health + maxHealth / 2);
                    damage = 20;
                    logic.addGameMessage(name + " restaura 50% de su salud.");
                    break;
                case "Arcanista":
                    ArrayList<Enemy> enemiesToRemove = new ArrayList<>();
                    for (Enemy e : logic.getEnemies()) {
                        if (e != null) {
                            e.setHealth(e.getHealth() - 50);
                            if (e.getHealth() <= 0) {
                                enemiesToRemove.add(e);
                            }
                        }
                    }
                    logic.getEnemies().removeAll(enemiesToRemove);
                    logic.getTurnOrder().removeAll(enemiesToRemove);
                    if (logic.getEnemies().isEmpty()) {
                        Item key = logic.createItem("Llave", true, false, 0);
                        logic.getKeys().add(key);
                        logic.addGameMessage(name + " elimina a " + enemiesToRemove.size() + " enemigos y una llave aparece en el tablero.");
                    } else if (!enemiesToRemove.isEmpty()) {
                        logic.addGameMessage(name + " daña a todos los enemigos, elimina a " + enemiesToRemove.size() + ".");
                    } else {
                        logic.addGameMessage(name + " daña a todos los enemigos.");
                    }
                    break;
            }
            hasSpecialUsed = true;
            logic.notifyRepaint();
        } catch (Exception ex) {
            logic.showError("Error al usar la habilidad especial: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void useSecondSpecial() {
        if (hasSecondSpecialUsed) {
            GameLogic.getInstance().addGameMessage(name + " ya usó su habilidad secundaria.");
            return;
        }
        GameLogic logic = GameLogic.getInstance();
        try {
            switch (classType) {
                case "Médico de Campo":
                    boolean foundDead = false;
                    for (Player p : new ArrayList<>(logic.getDeadPlayers())) {
                        foundDead = true;
                        int[] offsets = {0, 1, -1, 2, -2, 3, -3};
                        int newX = -1, newY = -1;
                        boolean positionFound = false;
                        for (int dx : offsets) {
                            for (int dy : offsets) {
                                int tryX = x + dx;
                                int tryY = y + dy;
                                if (tryX >= 0 && tryX < GameLogic.BOARD_SIZE && tryY >= 0 && tryY < GameLogic.BOARD_SIZE && !logic.isOccupied(tryX, tryY)) {
                                    newX = tryX;
                                    newY = tryY;
                                    positionFound = true;
                                    break;
                                }
                            }
                            if (positionFound) break;
                        }
                        if (!positionFound) {
                            logic.addGameMessage("No hay espacio para revivir a " + p.getName() + ".");
                            break;
                        }
                        p.setHealth(p.getMaxHealth() / 2);
                        p.setX(newX);
                        p.setY(newY);
                        logic.getDeadPlayers().remove(p);
                        if (!logic.getPlayers().contains(p)) {
                            logic.getPlayers().add(p);
                        }
                        logic.addGameMessage(name + " revive a " + p.getName() + " con " + p.getHealth() + " HP en (" + newX + "," + newY + ").");
                        if (!logic.getTurnOrder().contains(p)) {
                            logic.getTurnOrder().add(p);
                        }
                        break;
                    }
                    if (!foundDead) {
                        logic.addGameMessage(name + " no encuentra jugadores muertos para revivir.");
                    }
                    break;
                case "Nigromante":
                    Enemy controlledZombie = null;
                    for (Enemy e : logic.getEnemies()) {
                        if ((e instanceof ZombieComun || e instanceof ZombieCorredor) &&
                                Math.abs(e.getX() - x) <= range && Math.abs(e.getY() - y) <= range) {
                            controlledZombie = e;
                            break;
                        }
                    }
                    if (controlledZombie != null) {
                        Enemy targetEnemy = null;
                        double minDistance = Double.MAX_VALUE;
                        for (Enemy e : logic.getEnemies()) {
                            if (e != controlledZombie) {
                                double distance = Math.sqrt(Math.pow(e.getX() - controlledZombie.getX(), 2) + Math.pow(e.getY() - controlledZombie.getY(), 2));
                                if (distance < minDistance && distance <= controlledZombie.getRange()) {
                                    minDistance = distance;
                                    targetEnemy = e;
                                }
                            }
                        }
                        if (targetEnemy != null) {
                            targetEnemy.setHealth(targetEnemy.getHealth() - controlledZombie.getDamage());
                            logic.addGameMessage(name + " controla un zombie para atacar a otro enemigo (-" + controlledZombie.getDamage() + " HP).");
                            if (targetEnemy.getHealth() <= 0) {
                                logic.getEnemies().remove(targetEnemy);
                                logic.getTurnOrder().remove(targetEnemy);
                                logic.addGameMessage("El enemigo atacado muere.");
                                if (logic.getEnemies().isEmpty()) {
                                    Item key = logic.createItem("Llave", true, false, 0);
                                    logic.getKeys().add(key);
                                    logic.addGameMessage("¡Último enemigo derrotado! Una llave aparece en el tablero.");
                                }
                            }
                        } else {
                            logic.addGameMessage(name + " no encuentra un enemigo válido para atacar con el zombie controlado.");
                        }
                    } else {
                        logic.addGameMessage(name + " no encuentra un zombie para controlar.");
                    }
                    break;
                default:
                    logic.addGameMessage(name + " no tiene habilidad secundaria.");
                    break;
            }
            hasSecondSpecialUsed = true;
            logic.notifyRepaint();
        } catch (Exception ex) {
            logic.showError("Error al usar la habilidad secundaria: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getClassType() {
        return classType;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getMaxMoves() {
        return maxMoves;
    }

    public int getRange() {
        return range;
    }

    public boolean hasSpecialUsed() {
        return hasSpecialUsed;
    }

    public void setHasSpecialUsed(boolean hasSpecialUsed) {
        this.hasSpecialUsed = hasSpecialUsed;
    }

    public boolean hasSecondSpecialUsed() {
        return hasSecondSpecialUsed;
    }

    public void setHasSecondSpecialUsed(boolean hasSecondSpecialUsed) {
        this.hasSecondSpecialUsed = hasSecondSpecialUsed;
    }

    public boolean isInvisible() {
        return isInvisible;
    }

    public void setInvisible(boolean invisible) {
        isInvisible = invisible;
    }
}