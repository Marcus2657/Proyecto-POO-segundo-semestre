import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import javax.swing.*;

public class GameLogic {
    private static GameLogic instance;
    private ArrayList<Player> players;
    private ArrayList<Player> deadPlayers;
    private ArrayList<Enemy> enemies;
    private ArrayList<Item> items;
    private ArrayList<Item> sharedInventory;
    private ArrayList<Item> keys;
    private ArrayList<Point> obstacles;
    private ArrayList<Trap> traps;
    private ArrayList<Riddle> riddles;
    private ArrayList<GameMessage> gameMessages;
    private ArrayList<Object> turnOrder;
    private int currentTurnIndex;
    private int currentRound;
    private boolean isGameRunning;
    public static final int TILE_SIZE = 30;
    public static final int BOARD_SIZE = 20;
    public static final int MAX_INVENTORY_SIZE = 10;
    private Random random;
    private int movesLeft;
    private boolean hasAttacked;
    private int numPlayers;
    private int currentLevel;
    private final int MAX_LEVELS = 20;
    private GamePanel gamePanel;
    private Timer messageTimer;
    private Image weaponSprite;

    private GameLogic() {
        players = new ArrayList<>();
        deadPlayers = new ArrayList<>();
        enemies = new ArrayList<>();
        items = new ArrayList<>();
        sharedInventory = new ArrayList<>();
        keys = new ArrayList<>();
        obstacles = new ArrayList<>();
        traps = new ArrayList<>();
        riddles = new ArrayList<>();
        gameMessages = new ArrayList<>();
        turnOrder = new ArrayList<>();
        random = new Random();
        currentTurnIndex = 0;
        currentRound = 1;
        isGameRunning = false;
        currentLevel = 1;
        initRiddles();
        messageTimer = new Timer(1000, e -> {
            gameMessages.removeIf(GameMessage::isExpired);
            notifyRepaint();
        });
        messageTimer.start();
    }

    public static GameLogic getInstance() {
        if (instance == null) {
            instance = new GameLogic();
        }
        return instance;
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void setWeaponSprite(Image weaponSprite) {
        this.weaponSprite = weaponSprite;
    }

    public Image getWeaponSprite() {
        return weaponSprite;
    }

    public void notifyRepaint() {
        if (gamePanel != null && isGameRunning) { // Evitar repintar si el juego no está activo
            SwingUtilities.invokeLater(() -> gamePanel.repaint()); // Usar SwingUtilities para evitar bloqueos
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    private void initRiddles() {
        riddles.add(new Riddle(
                "Siempre está delante de ti, pero no puedes verlo. ¿Qué es?",
                "El futuro",
                new String[]{"El futuro", "El pasado", "El cielo", "Un espejo"}
        ));
        riddles.add(new Riddle(
                "Cuanto más quitas, más grande se vuelve. ¿Qué es?",
                "Un agujero",
                new String[]{"Un árbol", "Un agujero", "Una montaña", "Un río"}
        ));
        riddles.add(new Riddle(
                "¿Qué tiene manos pero no puede aplaudir?",
                "Un reloj",
                new String[]{"Un árbol", "Un reloj", "Un río", "Un hombre dormido"}
        ));
        riddles.add(new Riddle(
                "Me puedes romper sin tocarme. ¿Qué soy?",
                "Una promesa",
                new String[]{"Un vidrio", "Un huevo", "Una promesa", "Un corazón"}
        ));
    }

    public void initPlayersAndStartGame(JFrame frame, Map<String, Image> obstacleSprites, Image weaponSprite) {
        this.weaponSprite = weaponSprite;
        String input = JOptionPane.showInputDialog(frame,
                "Ingrese la cantidad de jugadores (1-5):", "Cantidad de Jugadores",
                JOptionPane.PLAIN_MESSAGE);
        try {
            numPlayers = Integer.parseInt(input);
            if (numPlayers < 1 || numPlayers > 5) {
                showError("Por favor, ingrese un número entre 1 y 5.");
                return;
            }
            players.clear();
            sharedInventory.clear();
            ArrayList<String> availableClasses = new ArrayList<>();
            String[] allClasses = {
                    "Cazador de Zombies", "Médico de Campo", "Nigromante", "Caballero Fantasma",
                    "Ladrón de Tumbas", "Bruja Oscura", "Zombie Amistoso", "Explorador",
                    "Sacerdote", "Gólem de Piedra", "Artillero", "Alquimista", "Berserker",
                    "Hechicero del Viento", "Cazador de Sombras", "Paladín", "Druida",
                    "Ingeniero", "Monje", "Arcanista"
            };
            for (String className : allClasses) {
                availableClasses.add(className);
            }

            for (int i = 0; i < numPlayers; i++) {
                String[] currentOptions = availableClasses.toArray(new String[0]);
                String selectedClass = (String) JOptionPane.showInputDialog(frame,
                        "Jugador " + (i + 1) + ": Elige tu personaje:", "Selección de Personaje",
                        JOptionPane.PLAIN_MESSAGE, null, currentOptions, currentOptions[0]);
                if (selectedClass != null) {
                    players.add(new Player("Jugador " + (i + 1), selectedClass, 0, 0));
                    availableClasses.remove(selectedClass);
                } else {
                    showError("Debes seleccionar un personaje.");
                    players.clear();
                    return;
                }
            }
            startGame(frame, obstacleSprites, weaponSprite);
        } catch (NumberFormatException ex) {
            if (input != null) {
                showError("Por favor, ingrese un número válido.");
            }
        }
    }

    private void startGame(JFrame frame, Map<String, Image> obstacleSprites, Image weaponSprite) {
        currentTurnIndex = 0;
        currentRound = 1;
        isGameRunning = true;
        movesLeft = 0;
        hasAttacked = false;
        currentLevel = 1;
        gameMessages.clear();
        initGame();
        frame.getContentPane().removeAll();
        if (gamePanel != null && gamePanel.getKeyListeners().length > 0) {
            gamePanel.removeKeyListener(gamePanel.getKeyListeners()[0]);
        }
        gamePanel = new GamePanel(obstacleSprites, weaponSprite);
        frame.add(gamePanel);
        frame.revalidate();
        frame.repaint();
        gamePanel.requestFocusInWindow();
    }

    public void initGame() {
        enemies.clear();
        items.clear();
        sharedInventory.clear();
        traps.clear();
        keys.clear();
        obstacles.clear();
        gameMessages.clear();
        turnOrder.clear();
        deadPlayers.clear();

        spawnObstaclesForLevel(currentLevel);

        int[][] startPositions = {{0,0}, {0,1}, {0,2}, {0,3}, {0,4}};
        for (int i = 0; i < numPlayers; i++) {
            if (i < players.size()) {
                Player p = players.get(i);
                int x = startPositions[i][0];
                int y = startPositions[i][1];
                // Ensure player spawn position is not occupied by obstacles
                if (isTileOccupied(x, y)) {
                    // Find a new position if the default one is occupied
                    do {
                        x = random.nextInt(5); // Restrict to top-left quadrant for players
                        y = random.nextInt(5);
                    } while (isTileOccupied(x, y));
                }
                p.setX(x);
                p.setY(y);
                p.setHealth(p.getMaxHealth());
                p.setHasSpecialUsed(false);
                p.setHasSecondSpecialUsed(false);
            }
        }

        spawnEnemiesForLevel(currentLevel);
        spawnItemsForLevel(currentLevel);
        spawnTrapsForLevel(currentLevel);

        updateTurnOrder();

        if (!turnOrder.isEmpty() && turnOrder.get(currentTurnIndex) instanceof Player) {
            Player currentPlayer = (Player) turnOrder.get(currentTurnIndex);
            movesLeft = currentPlayer.getMaxMoves();
            hasAttacked = false;
        }
        addGameMessage("Ronda " + currentRound + " comienza.");
    }

    private void updateTurnOrder() {
        turnOrder.clear();
        for (Player p : players) {
            if (p.getHealth() > 0) {
                turnOrder.add(p);
            }
        }
        for (Enemy e : enemies) {
            if (e.getHealth() > 0) {
                turnOrder.add(e);
            }
        }
    }

    public void advanceToNextLevel(JFrame frame) {
        try {
            if (currentLevel == 5 || currentLevel == 10 || currentLevel == 15 || currentLevel == 20) {
                int riddleIndex = (currentLevel / 5) - 1;
                Riddle riddle = riddles.get(riddleIndex);

                String selectedAnswer = (String) JOptionPane.showInputDialog(
                        frame,
                        riddle.getQuestion(),
                        "Acertijo para avanzar",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        riddle.getOptions(),
                        riddle.getOptions()[0]
                );

                if (selectedAnswer != null && selectedAnswer.equals(riddle.getCorrectAnswer())) {
                    JOptionPane.showMessageDialog(frame, "¡Correcto! Avanzas al siguiente " +
                            (currentLevel == 20 ? "nivel final completado" : "mundo") + ".");
                    proceedToNextLevel(frame);
                } else {
                    JOptionPane.showMessageDialog(frame, "Respuesta incorrecta. El mundo se reinicia.");
                    initGame();
                }
            } else {
                proceedToNextLevel(frame);
            }
        } catch (Exception ex) {
            showError("Error al avanzar de nivel: " + ex.getMessage());
            System.out.println("Error al avanzar de nivel: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void proceedToNextLevel(JFrame frame) {
        try {
            if (currentLevel < MAX_LEVELS) {
                currentLevel++;
                initGame();
                JOptionPane.showMessageDialog(frame, "¡Has avanzado al nivel " + currentLevel + " - " + getCurrentWorld() + "!");
            } else {
                JOptionPane.showMessageDialog(frame, "¡Has ganado! Completaste todos los niveles en " + getCurrentWorld() + ".");
                returnToMenu(frame);
            }
        } catch (Exception ex) {
            showError("Error al proceder al siguiente nivel: " + ex.getMessage());
            System.out.println("Error al proceder al siguiente nivel: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public boolean isOccupied(int x, int y) {
        // Used for movement checks, considers players, enemies, and obstacles
        for (Player p : players) {
            if (p.getX() == x && p.getY() == y && p.getHealth() > 0) return true;
        }
        for (Enemy e : enemies) {
            if (e.getX() == x && e.getY() == y) return true;
        }
        for (Point o : obstacles) {
            if (o.x == x && o.y == y) return true;
        }
        return false;
    }

    private boolean isTileOccupied(int x, int y) {
        // Comprehensive check for all entities to prevent spawn overlap
        for (Player p : players) {
            if (p.getX() == x && p.getY() == y && p.getHealth() > 0) return true;
        }
        for (Enemy e : enemies) {
            if (e.getX() == x && e.getY() == y) return true;
        }
        for (Point o : obstacles) {
            if (o.x == x && o.y == y) return true;
        }
        for (Item item : items) {
            if (item.getX() == x && item.getY() == y) return true;
        }
        for (Item key : keys) {
            if (key.getX() == x && key.getY() == y) return true;
        }
        for (Trap t : traps) {
            if (t.getX() == x && t.getY() == y) return true;
        }
        return false;
    }

    public void interactWithTrap(Player player, JFrame frame) {
        try {
            for (Trap t : traps) {
                if (t.getX() == player.getX() && t.getY() == player.getY() && t.isActive()) {
                    if (t.getType().equals("LockedDoor")) {
                        for (Item item : sharedInventory) {
                            if (item.getName().equals("Llave")) {
                                t.setActive(false);
                                sharedInventory.remove(item);
                                addGameMessage(player.getName() + " abre la puerta con una llave.");
                                advanceToNextLevel(frame);
                                return;
                            }
                        }
                        addGameMessage("Necesitas una llave para abrir la puerta.");
                    } else if (t.getType().equals("Pitfall")) {
                        player.setHealth(0);
                        t.setActive(false);
                        addGameMessage(player.getName() + " muere al caer en un hoyo.");
                    } else if (t.getType().equals("Laser")) {
                        player.setHealth(player.getHealth() - 90);
                        t.setActive(false);
                        addGameMessage(player.getName() + " pierde 90 HP por láser.");
                        if (player.getHealth() <= 0) {
                            addGameMessage(player.getName() + " ha muerto.");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            showError("Error al interactuar con la trampa: " + ex.getMessage());
            System.out.println("Error al interactuar con la trampa: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public boolean canAttack(Player player) {
        if (hasAttacked) return false;
        for (Enemy e : enemies) {
            int attackRange = player.getClassType().equals("Cazador de Zombies") ? 2 : player.getRange();
            if (Math.abs(player.getX() - e.getX()) <= attackRange && Math.abs(player.getY() - e.getY()) <= attackRange) {
                return true;
            }
        }
        return false;
    }

    private void enemyTurn(Enemy enemy) {
        try {
            if (!players.isEmpty()) {
                Player closestPlayer = null;
                double minDistance = Double.MAX_VALUE;
                for (Player p : players) {
                    if (p.getHealth() > 0 && !p.isInvisible()) {
                        double distance = Math.sqrt(Math.pow(p.getX() - enemy.getX(), 2) + Math.pow(p.getY() - enemy.getY(), 2));
                        if (distance < minDistance) {
                            minDistance = distance;
                            closestPlayer = p;
                        }
                    }
                }
                if (closestPlayer != null) {
                    enemy.moveTowardsPlayer(closestPlayer);
                    for (Trap t : traps) {
                        if (t.getX() == enemy.getX() && t.getY() == enemy.getY() && t.isActive()) {
                            if (t.getType().equals("Pitfall")) {
                                enemy.setHealth(0);
                                t.setActive(false);
                                addGameMessage("Enemigo muere al caer en un hoyo.");
                            } else if (t.getType().equals("Laser")) {
                                enemy.setHealth(enemy.getHealth() - 50);
                                t.setActive(false);
                                addGameMessage("Enemigo pierde 50 HP por láser.");
                            }
                        }
                    }
                    if (enemy.getHealth() > 0) {
                        enemy.attack(closestPlayer);
                    }
                }
            }
        } catch (Exception ex) {
            showError("Error en el turno del enemigo: " + ex.getMessage());
            System.out.println("Error en el turno del enemigo: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void skeletonAllyTurn(Player skeleton) {
        try {
            if (skeleton.getClassType().equals("Esqueleto Aliado") && skeleton.getHealth() > 0) {
                Enemy targetEnemy = null;
                int minDistance = Integer.MAX_VALUE;
                for (Enemy e : enemies) {
                    int distance = Math.abs(e.getX() - skeleton.getX()) + Math.abs(e.getY() - skeleton.getY());
                    if (distance < minDistance) {
                        minDistance = distance;
                        targetEnemy = e;
                    }
                }

                if (targetEnemy == null) {
                    addGameMessage(skeleton.getName() + " no encuentra enemigos.");
                    return;
                }

                int movesAvailable = skeleton.getMaxMoves();
                while (movesAvailable > 0 && minDistance > skeleton.getRange()) {
                    int dx = Integer.compare(targetEnemy.getX(), skeleton.getX());
                    int dy = Integer.compare(targetEnemy.getY(), skeleton.getY());
                    int newX = skeleton.getX() + dx;
                    int newY = skeleton.getY() + dy;

                    if (Math.abs(targetEnemy.getX() - skeleton.getX()) > Math.abs(targetEnemy.getY() - skeleton.getY())) {
                        if (!isOccupied(newX, skeleton.getY()) && newX >= 0 && newX < BOARD_SIZE) {
                            skeleton.setX(newX);
                            addGameMessage(skeleton.getName() + " se mueve a (" + skeleton.getX() + "," + skeleton.getY() + ").");
                        } else if (!isOccupied(skeleton.getX(), newY) && newY >= 0 && newY < BOARD_SIZE) {
                            skeleton.setY(newY);
                            addGameMessage(skeleton.getName() + " se mueve a (" + skeleton.getX() + "," + skeleton.getY() + ").");
                        } else {
                            break;
                        }
                    } else {
                        if (!isOccupied(skeleton.getX(), newY) && newY >= 0 && newY < BOARD_SIZE) {
                            skeleton.setY(newY);
                            addGameMessage(skeleton.getName() + " se mueve a (" + skeleton.getX() + "," + skeleton.getY() + ").");
                        } else if (!isOccupied(newX, skeleton.getY()) && newX >= 0 && newX < BOARD_SIZE) {
                            skeleton.setX(newX);
                            addGameMessage(skeleton.getName() + " se mueve a (" + skeleton.getX() + "," + skeleton.getY() + ").");
                        } else {
                            break;
                        }
                    }

                    for (Trap t : traps) {
                        if (t.getX() == skeleton.getX() && t.getY() == skeleton.getY() && t.isActive()) {
                            if (t.getType().equals("Pitfall")) {
                                skeleton.setHealth(0);
                                t.setActive(false);
                                addGameMessage(skeleton.getName() + " muere al caer en un hoyo.");
                                return;
                            } else if (t.getType().equals("Laser")) {
                                skeleton.setHealth(skeleton.getHealth() - 50);
                                t.setActive(false);
                                addGameMessage(skeleton.getName() + " pierde 50 HP por láser.");
                                if (skeleton.getHealth() <= 0) {
                                    addGameMessage(skeleton.getName() + " ha muerto.");
                                    return;
                                }
                            }
                        }
                    }

                    movesAvailable--;
                    minDistance = Math.abs(targetEnemy.getX() - skeleton.getX()) + Math.abs(targetEnemy.getY() - skeleton.getY());
                }

                if (minDistance <= skeleton.getRange()) {
                    skeleton.attack(targetEnemy);
                    if (targetEnemy.getHealth() <= 0) {
                        enemies.remove(targetEnemy);
                        turnOrder.remove(targetEnemy);
                        addGameMessage(skeleton.getName() + " elimina un enemigo.");
                        if (enemies.isEmpty()) {
                            Item key = createItem("Llave", true, false, 0);
                            keys.add(key);
                            addGameMessage("¡Último enemigo derrotado! Una llave aparece en el tablero.");
                        }
                    } else {
                        addGameMessage(skeleton.getName() + " ataca un enemigo (-" + skeleton.getDamage() + " HP).");
                    }
                } else {
                    addGameMessage(skeleton.getName() + " no puede atacar; enemigo fuera de rango.");
                }
            }
        } catch (Exception ex) {
            showError("Error en el turno del esqueleto aliado: " + ex.getMessage());
            System.out.println("Error en el turno del esqueleto aliado: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void endTurn() {
        try {
            ArrayList<Player> newlyDeadPlayers = new ArrayList<>();
            for (Player p : players) {
                if (p.getHealth() > 0) {
                    interactWithTrap(p, null);
                }
                if (p.getHealth() <= 0 && !deadPlayers.contains(p)) {
                    newlyDeadPlayers.add(p);
                    addGameMessage(p.getName() + " ha muerto.");
                }
            }
            deadPlayers.addAll(newlyDeadPlayers);
            turnOrder.removeAll(newlyDeadPlayers);

            if (players.stream().allMatch(p -> p.getHealth() <= 0)) {
                handleGameOver();
                return;
            }

            ArrayList<Enemy> deadEnemies = new ArrayList<>();
            for (Enemy e : enemies) {
                if (e.getHealth() <= 0) {
                    deadEnemies.add(e);
                    addGameMessage("Un enemigo ha muerto.");
                }
            }
            enemies.removeAll(deadEnemies);
            turnOrder.removeAll(deadEnemies);

            currentTurnIndex++;
            if (currentTurnIndex >= turnOrder.size()) {
                currentTurnIndex = 0;
                currentRound++;
                updateTurnOrder();
                addGameMessage("Ronda " + currentRound + " comienza.");

                for (Player p : players) {
                    if (p.isInvisible()) {
                        p.setInvisible(false);
                        addGameMessage(p.getName() + " ya no está invisible.");
                    }
                }
            }

            if (turnOrder.isEmpty()) {
                handleGameOver();
                return;
            }

            Object nextEntity = turnOrder.get(currentTurnIndex);
            if (nextEntity instanceof Player) {
                Player currentPlayer = (Player) nextEntity;
                if (currentPlayer.getClassType().equals("Esqueleto Aliado")) {
                    skeletonAllyTurn(currentPlayer);
                    endTurn();
                } else {
                    movesLeft = currentPlayer.getMaxMoves();
                    hasAttacked = false;
                }
            } else if (nextEntity instanceof Enemy) {
                Enemy enemy = (Enemy) nextEntity;
                enemyTurn(enemy);
                endTurn();
            }

            notifyRepaint();
        } catch (Exception ex) {
            showError("Error al finalizar el turno: " + ex.getMessage());
            System.out.println("Error al finalizar el turno: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleGameOver() {
        try {
            isGameRunning = false;
            int option = JOptionPane.showConfirmDialog(null,
                    "¡Game Over! Todos los jugadores han muerto en el nivel " + currentLevel + " (" + getCurrentWorld() + "). ¿Quieres reintentar?",
                    "Fin del Juego", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(() -> initPlayersAndStartGame(null, null, weaponSprite));
            } else {
                SwingUtilities.invokeLater(() -> returnToMenu(null));
            }
        } catch (Exception ex) {
            showError("Error al manejar el fin del juego: " + ex.getMessage());
            System.out.println("Error al manejar el fin del juego: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public Item createItem(String name, boolean isConsumable, boolean isWeapon, int effectValue) {
        try {
            int x, y;
            do {
                x = random.nextInt(BOARD_SIZE);
                y = random.nextInt(BOARD_SIZE);
            } while (isTileOccupied(x, y));
            return new Item(name, x, y, isConsumable, isWeapon, effectValue);
        } catch (Exception ex) {
            showError("Error al crear item: " + ex.getMessage());
            System.out.println("Error al crear item: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public void useSharedItem(int index) {
        try {
            if (index >= 0 && index < sharedInventory.size()) {
                Item item = sharedInventory.get(index);
                if (item.isConsumable()) {
                    Object currentEntity = turnOrder.get(currentTurnIndex);
                    if (currentEntity instanceof Player) {
                        Player currentPlayer = (Player) currentEntity;
                        if (item.getName().equals("Poción de Vida")) {
                            currentPlayer.setHealth(Math.min(currentPlayer.getMaxHealth(), currentPlayer.getHealth() + item.getEffectValue()));
                            addGameMessage(currentPlayer.getName() + " usa Poción de Vida (+" + item.getEffectValue() + " HP).");
                            sharedInventory.remove(index);
                        } else if (item.getName().equals("Poción de Daño")) {
                            currentPlayer.setDamage(currentPlayer.getDamage() + item.getEffectValue());
                            addGameMessage(currentPlayer.getName() + " usa Poción de Daño (+" + item.getEffectValue() + " daño).");
                            sharedInventory.remove(index);
                        } else if (item.getName().equals("Llave")) {
                            addGameMessage("Debes estar en la puerta para usar la llave.");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            showError("Error al usar item compartido: " + ex.getMessage());
            System.out.println("Error al usar item compartido: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void spawnObstaclesForLevel(int level) {
        try {
            obstacles.clear();

            if (level >= 11 && level <= 15) {
                for (int x = 2; x < BOARD_SIZE - 2; x += 4) {
                    for (int y = 0; y < BOARD_SIZE; y++) {
                        if (y % 4 != 0) {
                            if (!isTileOccupied(x, y)) {
                                obstacles.add(new Point(x, y));
                            }
                        }
                    }
                }
                for (int y = 2; y < BOARD_SIZE - 2; y += 4) {
                    for (int x = 0; x < BOARD_SIZE; x++) {
                        if (x % 4 != 0) {
                            if (!isTileOccupied(x, y)) {
                                obstacles.add(new Point(x, y));
                            }
                        }
                    }
                }
                obstacles.removeIf(o -> (o.x == 0 && o.y == 0));
                obstacles.removeIf(o -> (o.x == BOARD_SIZE - 1 && o.y == BOARD_SIZE - 1));

                int extraObstacles = 5 + (level / 2);
                for (int i = 0; i < extraObstacles; i++) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE);
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y) || (x == 0 && y == 0) || (x == BOARD_SIZE - 1 && y == BOARD_SIZE - 1));
                    obstacles.add(new Point(x, y));
                }
            } else {
                int numObstacles = 5 + (level / 2);
                for (int i = 0; i < numObstacles; i++) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE);
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y));
                    obstacles.add(new Point(x, y));
                }
            }
        } catch (Exception ex) {
            showError("Error al generar obstáculos: " + ex.getMessage());
            System.out.println("Error al generar obstáculos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void spawnEnemiesForLevel(int level) {
        try {
            enemies.clear();
            int numEnemies = Math.min(10, 3 + level); // Limitar a un máximo de 10 enemigos
            int baseHealth = 50 + (level * 10);
            int baseDamage = 10 + (level * 2);

            if (level <= 5) {
                for (int i = 0; i < numEnemies; i++) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE - 5) + 5;
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y));
                    enemies.add(random.nextBoolean() ?
                            new Fantasma(x, y, baseHealth - 10, baseDamage - 5) :
                            new ZombieCorredor(x, y, baseHealth - 20, baseDamage + 5));
                }
            } else if (level <= 10) {
                for (int i = 0; i < numEnemies; i++) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE - 5) + 5;
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y));
                    enemies.add(random.nextBoolean() ?
                            new ZombieComun(x, y, baseHealth, baseDamage) :
                            new ZombieCorredor(x, y, baseHealth - 20, baseDamage + 5));
                }
                if (level == 10) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE - 5) + 5;
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y));
                    enemies.add(new NecromanteEnemigo(x, y, 200, 30));
                }
            } else if (level <= 15) {
                for (int i = 0; i < numEnemies; i++) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE - 5) + 5;
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y));
                    enemies.add(random.nextBoolean() ?
                            new EsqueletoArmado(x, y, baseHealth, baseDamage) :
                            new ZombieCorredor(x, y, baseHealth - 20, baseDamage + 5));
                }
                if (level == 15) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE - 5) + 5;
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y));
                    enemies.add(new NecromanteEnemigo(x, y, 300, 35));
                }
            } else {
                for (int i = 0; i < numEnemies; i++) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE - 5) + 5;
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y));
                    enemies.add(random.nextBoolean() ?
                            new Fantasma(x, y, baseHealth - 10, baseDamage - 5) :
                            new NecromanteEnemigo(x, y, baseHealth + 20, baseDamage + 10));
                }
                if (level == 20) {
                    int x, y;
                    do {
                        x = random.nextInt(BOARD_SIZE - 5) + 5;
                        y = random.nextInt(BOARD_SIZE);
                    } while (isTileOccupied(x, y));
                    enemies.add(new NecromanteEnemigo(x, y, 500, 50));
                }
            }
        } catch (Exception ex) {
            showError("Error al generar enemigos: " + ex.getMessage());
            System.out.println("Error al generar enemigos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void spawnItemsForLevel(int level) {
        try {
            items.clear();
            int numItems = 3 + (level / 2);

            for (int i = 0; i < 2 + (level / 3); i++) {
                items.add(createItem("Poción de Vida", true, false, 30 + (level * 5)));
            }
            items.add(createItem("Poción de Daño", true, false, 10 + (level * 3)));
            items.add(createItem("Arma", false, true, 10));
        } catch (Exception ex) {
            showError("Error al generar ítems: " + ex.getMessage());
            System.out.println("Error al generar ítems: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void spawnTrapsForLevel(int level) {
        try {
            traps.clear();
            keys.clear();
            int numTraps = Math.min(5, 2 + (level / 3)); // Limitar a un máximo de 5 trampas

            int doorX, doorY;
            do {
                doorX = random.nextInt(BOARD_SIZE - 5) + 5;
                doorY = random.nextInt(BOARD_SIZE);
            } while (isTileOccupied(doorX, doorY));
            traps.add(new Trap("LockedDoor", doorX, doorY));

            for (int i = 0; i < numTraps - 1; i++) {
                int x, y;
                String trapType;
                do {
                    x = random.nextInt(BOARD_SIZE - 5) + 5;
                    y = random.nextInt(BOARD_SIZE);
                } while (isTileOccupied(x, y));

                if (level <= 5) {
                    trapType = (random.nextInt(2) == 0) ? "Laser" : "Pitfall";
                } else {
                    trapType = "Pitfall";
                }
                traps.add(new Trap(trapType, x, y));
            }
        } catch (Exception ex) {
            showError("Error al generar trampas: " + ex.getMessage());
            System.out.println("Error al generar trampas: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public String getCurrentWorld() {
        if (currentLevel >= 1 && currentLevel <= 5) return "Bosques Oscuros";
        else if (currentLevel <= 10) return "Cementerios";
        else if (currentLevel <= 15) return "Catacumbas";
        else return "Castillos Embrujados";
    }

    public Color getWorldBackgroundColor() {
        if (currentLevel >= 1 && currentLevel <= 5) return new Color(0, 100, 0);
        else if (currentLevel <= 10) return new Color(139, 69, 19);
        else if (currentLevel <= 15) return new Color(105, 105, 105);
        else return new Color(75, 0, 130);
    }

    public Color getWorldObstacleColor() {
        if (currentLevel >= 1 && currentLevel <= 5) return new Color(139, 69, 19);
        else if (currentLevel <= 10) return new Color(169, 169, 169);
        else if (currentLevel <= 15) return new Color(139, 137, 137);
        else return new Color(128, 0, 128);
    }

    private void returnToMenu(JFrame frame) {
        try {
            isGameRunning = false;
            if (frame != null) {
                frame.getContentPane().removeAll();
                if (gamePanel != null && gamePanel.getKeyListeners().length > 0) {
                    gamePanel.removeKeyListener(gamePanel.getKeyListeners()[0]);
                }
                JPanel menuPanel = new JPanel();
                menuPanel.setLayout(new GridLayout(2, 1, 10, 10));

                JButton startButton = new JButton("Iniciar Juego");
                JButton exitButton = new JButton("Salir");

                startButton.addActionListener(e -> initPlayersAndStartGame(frame, null, weaponSprite));
                exitButton.addActionListener(e -> System.exit(0));

                menuPanel.add(startButton);
                menuPanel.add(exitButton);
                frame.add(menuPanel);
                frame.revalidate();
                frame.repaint();
            }
        } catch (Exception ex) {
            showError("Error al volver al menú: " + ex.getMessage());
            System.out.println("Error al volver al menú: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void addGameMessage(String message) {
        gameMessages.add(new GameMessage(message));
        if (gameMessages.size() > 3) {
            gameMessages.remove(0);
        }
        notifyRepaint();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Player> getDeadPlayers() {
        return deadPlayers;
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public ArrayList<Item> getSharedInventory() {
        return sharedInventory;
    }

    public ArrayList<Item> getKeys() {
        return keys;
    }

    public ArrayList<Point> getObstacles() {
        return obstacles;
    }

    public ArrayList<Trap> getTraps() {
        return traps;
    }

    public ArrayList<Riddle> getRiddles() {
        return riddles;
    }

    public ArrayList<GameMessage> getGameMessages() {
        return gameMessages;
    }

    public ArrayList<Object> getTurnOrder() {
        return turnOrder;
    }

    public int getCurrentTurnIndex() {
        return currentTurnIndex;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public int getMovesLeft() {
        return movesLeft;
    }

    public void setMovesLeft(int movesLeft) {
        this.movesLeft = movesLeft;
    }

    public boolean hasAttacked() {
        return hasAttacked;
    }

    public void setHasAttacked(boolean hasAttacked) {
        this.hasAttacked = hasAttacked;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
}