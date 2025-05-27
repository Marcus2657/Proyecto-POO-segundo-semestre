import java.awt.*;
import java.util.Map;
import javax.sound.sampled.*;
import javax.swing.*;

public class GamePanel extends JPanel {
    private Image doorImage;
    private Image keyImage;
    private Image healthPotionImage;
    private Image damagePotionImage;
    private Image weaponSprite; // Added to store weapon sprite
    private Clip menuAudioClip;
    private Map<String, Image> obstacleSprites;

    public GamePanel(Map<String, Image> obstacleSprites, Image weaponSprite) {
        this.obstacleSprites = obstacleSprites;
        this.weaponSprite = weaponSprite; // Initialize weapon sprite
        setFocusable(true);
        addKeyListener(new KeyHandler());
        setPreferredSize(new Dimension(GameLogic.TILE_SIZE * GameLogic.BOARD_SIZE + 400, GameLogic.TILE_SIZE * GameLogic.BOARD_SIZE + 100));
        GameLogic.getInstance().setGamePanel(this);
        // Load door image
        try {
            doorImage = new ImageIcon(getClass().getClassLoader().getResource("Recursos/imagenes/janierpuerta.png")).getImage();
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de la puerta: " + e.getMessage());
        }
        // Load key image
        try {
            keyImage = new ImageIcon(getClass().getClassLoader().getResource("Recursos/imagenes/janierllave.png")).getImage();
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de la llave: " + e.getMessage());
        }
        // Load health potion image
        try {
            healthPotionImage = new ImageIcon(getClass().getClassLoader().getResource("Recursos/imagenes/janierpocvida.png")).getImage();
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de la poción de vida: " + e.getMessage());
        }
        // Load damage potion image
        try {
            damagePotionImage = new ImageIcon(getClass().getClassLoader().getResource("Recursos/imagenes/janierposdano.png")).getImage();
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de la poción de daño: " + e.getMessage());
        }
        // Load and play menu audio
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("Recursos/audio/menu.wav"));
            menuAudioClip = AudioSystem.getClip();
            menuAudioClip.open(audioStream);
            menuAudioClip.start();
            menuAudioClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el audio del menú: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (menuAudioClip != null && menuAudioClip.isRunning()) {
            menuAudioClip.stop();
            menuAudioClip.close();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGame(g);
    }

    private void drawGame(Graphics g) {
        GameLogic logic = GameLogic.getInstance();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(logic.getWorldBackgroundColor());
        g.fillRect(0, 0, GameLogic.TILE_SIZE * GameLogic.BOARD_SIZE, GameLogic.TILE_SIZE * GameLogic.BOARD_SIZE);
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < GameLogic.BOARD_SIZE; i++) {
            for (int j = 0; j < GameLogic.BOARD_SIZE; j++) {
                g.drawRect(i * GameLogic.TILE_SIZE, j * GameLogic.TILE_SIZE, GameLogic.TILE_SIZE, GameLogic.TILE_SIZE);
            }
        }

        // Dibujar obstáculos con sprites según el mundo
        String worldKey = getWorldKey(logic.getCurrentWorld());
        Image obstacleSprite = obstacleSprites.get(worldKey);
        for (Point o : logic.getObstacles()) {
            if (obstacleSprite != null) {
                g.drawImage(obstacleSprite, o.x * GameLogic.TILE_SIZE, o.y * GameLogic.TILE_SIZE, 
                            GameLogic.TILE_SIZE, GameLogic.TILE_SIZE, this);
            } else {
                g.setColor(logic.getWorldObstacleColor());
                g.fillRect(o.x * GameLogic.TILE_SIZE, o.y * GameLogic.TILE_SIZE, GameLogic.TILE_SIZE, GameLogic.TILE_SIZE);
            }
        }

        for (Item item : logic.getItems()) {
            if (item.getName().equals("Poción de Vida")) {
                if (healthPotionImage != null) {
                    g.drawImage(healthPotionImage, 
                                item.getX() * GameLogic.TILE_SIZE, 
                                item.getY() * GameLogic.TILE_SIZE, 
                                GameLogic.TILE_SIZE, 
                                GameLogic.TILE_SIZE, 
                                this);
                } else {
                    g.setColor(Color.YELLOW);
                    g.fillOval(item.getX() * GameLogic.TILE_SIZE + 10, item.getY() * GameLogic.TILE_SIZE + 10, 10, 10);
                }
            } else if (item.getName().equals("Poción de Daño")) {
                if (damagePotionImage != null) {
                    g.drawImage(damagePotionImage, 
                                item.getX() * GameLogic.TILE_SIZE, 
                                item.getY() * GameLogic.TILE_SIZE, 
                                GameLogic.TILE_SIZE, 
                                GameLogic.TILE_SIZE, 
                                this);
                } else {
                    g.setColor(Color.RED);
                    g.fillOval(item.getX() * GameLogic.TILE_SIZE + 10, item.getY() * GameLogic.TILE_SIZE + 10, 10, 10);
                }
            } else if (item.isWeapon()) {
                if (weaponSprite != null) {
                    g.drawImage(weaponSprite, 
                                item.getX() * GameLogic.TILE_SIZE, 
                                item.getY() * GameLogic.TILE_SIZE, 
                                GameLogic.TILE_SIZE, 
                                GameLogic.TILE_SIZE, 
                                this);
                } else {
                    g.setColor(Color.RED);
                    g.fillRect(item.getX() * GameLogic.TILE_SIZE + 10, item.getY() * GameLogic.TILE_SIZE + 10, 10, 10);
                }
            } else {
                g.setColor(Color.YELLOW);
                g.fillOval(item.getX() * GameLogic.TILE_SIZE + 10, item.getY() * GameLogic.TILE_SIZE + 10, 10, 10);
            }
        }

        for (Item key : logic.getKeys()) {
            if (keyImage != null) {
                g.drawImage(keyImage, key.getX() * GameLogic.TILE_SIZE, key.getY() * GameLogic.TILE_SIZE, 
                            GameLogic.TILE_SIZE, GameLogic.TILE_SIZE, this);
            } else {
                g.setColor(Color.CYAN);
                g.fillOval(key.getX() * GameLogic.TILE_SIZE + 10, key.getY() * GameLogic.TILE_SIZE + 10, 10, 10);
            }
        }

        for (Trap t : logic.getTraps()) {
            if (t.isActive()) {
                if (t.getType().equals("Pitfall")) {
                    g.setColor(Color.BLACK);
                    g.fillOval(t.getX() * GameLogic.TILE_SIZE + 5, t.getY() * GameLogic.TILE_SIZE + 5, 20, 20);
                } else if (t.getType().equals("Laser")) {
                    g.setColor(Color.RED);
                    g.fillRect(t.getX() * GameLogic.TILE_SIZE + 5, t.getY() * GameLogic.TILE_SIZE + 5, 20, 20);
                } else if (t.getType().equals("LockedDoor")) {
                    if (doorImage != null) {
                        g.drawImage(doorImage, t.getX() * GameLogic.TILE_SIZE, t.getY() * GameLogic.TILE_SIZE, 
                                    GameLogic.TILE_SIZE, GameLogic.TILE_SIZE, this);
                    } else {
                        g.setColor(Color.BLUE);
                        g.fillRect(t.getX() * GameLogic.TILE_SIZE, t.getY() * GameLogic.TILE_SIZE, 
                                   GameLogic.TILE_SIZE, GameLogic.TILE_SIZE);
                    }
                }
            }
        }

        for (Player p : logic.getPlayers()) {
            if (p.getHealth() > 0) {
                switch (p.getClassType()) {
                    case "Cazador de Zombies":
                        g.setColor(Color.BLUE);
                        break;
                    case "Médico de Campo":
                        g.setColor(Color.GREEN);
                        break;
                    case "Nigromante":
                        g.setColor(Color.MAGENTA);
                        break;
                    case "Caballero Fantasma":
                        g.setColor(Color.WHITE);
                        break;
                    case "Ladrón de Tumbas":
                        g.setColor(Color.YELLOW);
                        break;
                    case "Bruja Oscura":
                        g.setColor(Color.PINK);
                        break;
                    case "Zombie Amistoso":
                        g.setColor(Color.LIGHT_GRAY);
                        break;
                    case "Explorador":
                        g.setColor(Color.ORANGE);
                        break;
                    case "Sacerdote":
                        g.setColor(Color.CYAN);
                        break;
                    case "Gólem de Piedra":
                        g.setColor(Color.GRAY);
                        break;
                    case "Artillero":
                        g.setColor(new Color(139, 69, 19));
                        break;
                    case "Alquimista":
                        g.setColor(new Color(255, 215, 0));
                        break;
                    case "Berserker":
                        g.setColor(new Color(178, 34, 34));
                        break;
                    case "Hechicero del Viento":
                        g.setColor(new Color(135, 206, 235));
                        break;
                    case "Cazador de Sombras":
                        g.setColor(new Color(25, 25, 112));
                        break;
                    case "Paladín":
                        g.setColor(new Color(255, 245, 238));
                        break;
                    case "Druida":
                        g.setColor(new Color(34, 139, 34));
                        break;
                    case "Ingeniero":
                        g.setColor(new Color(205, 133, 63));
                        break;
                    case "Monje":
                        g.setColor(new Color(255, 140, 0));
                        break;
                    case "Arcanista":
                        g.setColor(new Color(75, 0, 130));
                        break;
                    case "Esqueleto Aliado":
                        g.setColor(Color.DARK_GRAY);
                        break;
                    default:
                        g.setColor(Color.BLUE);
                }
                if (p.isInvisible()) {
                    g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), 100));
                }
                g.fillOval(p.getX() * GameLogic.TILE_SIZE + 5, p.getY() * GameLogic.TILE_SIZE + 5, 20, 20);
                g.setColor(Color.BLACK);
                g.drawString(p.getName().substring(0, Math.min(p.getName().length(), 3)), 
                             p.getX() * GameLogic.TILE_SIZE + 5, p.getY() * GameLogic.TILE_SIZE + 15);
            }
        }

        for (Enemy e : logic.getEnemies()) {
            if (e.getHealth() > 0) {
                if (e instanceof ZombieComun) {
                    g.setColor(Color.GREEN);
                } else if (e instanceof ZombieCorredor) {
                    g.setColor(Color.YELLOW);
                } else if (e instanceof EsqueletoArmado) {
                    g.setColor(Color.GRAY);
                } else if (e instanceof Fantasma) {
                    g.setColor(Color.WHITE);
                } else if (e instanceof NecromanteEnemigo) {
                    g.setColor(Color.MAGENTA);
                }
                g.fillRect(e.getX() * GameLogic.TILE_SIZE + 5, e.getY() * GameLogic.TILE_SIZE + 5, 20, 20);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                String healthText = String.valueOf(e.getHealth());
                int textWidth = g.getFontMetrics().stringWidth(healthText);
                g.setColor(Color.WHITE);
                g.fillRect(e.getX() * GameLogic.TILE_SIZE + 15 - textWidth / 2 - 2, 
                           e.getY() * GameLogic.TILE_SIZE - 12, 
                           textWidth + 4, 14);
                g.setColor(Color.BLACK);
                g.drawString(healthText, 
                             e.getX() * GameLogic.TILE_SIZE + 15 - textWidth / 2, 
                             e.getY() * GameLogic.TILE_SIZE);
            }
        }

        int infoX = GameLogic.TILE_SIZE * GameLogic.BOARD_SIZE + 50;
        int infoY = 50;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Mundo: " + logic.getCurrentWorld(), infoX, infoY);
        g.drawString("Nivel: " + logic.getCurrentLevel(), infoX, infoY + 20);
        g.drawString("Ronda: " + logic.getCurrentRound(), infoX, infoY + 40);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        infoY += 80;
        g.drawString("Jugadores:", infoX, infoY);
        infoY += 20;
        for (Player p : logic.getPlayers()) {
            g.drawString(p.getName() + " (" + p.getClassType() + "): " + p.getHealth() + "/" + p.getMaxHealth() + " HP", infoX, infoY);
            infoY += 20;
        }

        infoY += 20;
        g.drawString("Inventario:", infoX, infoY);
        infoY += 20;
        for (int i = 0; i < logic.getSharedInventory().size(); i++) {
            Item item = logic.getSharedInventory().get(i);
            g.drawString((i) + ": " + item.getName() + (item.isConsumable() ? " (+" + item.getEffectValue() + ")" : ""), infoX, infoY);
            infoY += 20;
        }

        infoY += 20;
        g.drawString("Mensajes:", infoX, infoY);
        infoY += 20;
        for (GameMessage msg : logic.getGameMessages()) {
            g.drawString(msg.getText(), infoX, infoY);
            infoY += 20;
        }

        if (logic.isGameRunning() && logic.getCurrentTurnIndex() < logic.getTurnOrder().size()) {
            Object currentEntity = logic.getTurnOrder().get(logic.getCurrentTurnIndex());
            if (currentEntity instanceof Player) {
                Player currentPlayer = (Player) currentEntity;
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString("Turno de: " + currentPlayer.getName() + " (" + currentPlayer.getClassType() + ")", infoX, infoY + 20);
                g.drawString("Movimientos restantes: " + logic.getMovesLeft(), infoX, infoY + 40);
                g.drawString("Ataque disponible: " + (!logic.hasAttacked() ? "Sí" : "No"), infoX, infoY + 60);
            }
        }
    }

    private String getWorldKey(String world) {
        switch (world) {
            case "Bosques Oscuros":
                return "world1";
            case "Cementerios":
                return "world2";
            case "Catacumbas":
                return "world3";
            case "Castillos Embrujados":
                return "world4";
            default:
                return "world1";
        }
    }
}