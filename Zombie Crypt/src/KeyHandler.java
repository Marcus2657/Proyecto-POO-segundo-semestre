import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.*;

public class KeyHandler extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        GameLogic logic = GameLogic.getInstance();
        if (!logic.isGameRunning() || logic.getCurrentTurnIndex() >= logic.getTurnOrder().size()) {
            return;
        }

        Object currentEntity = logic.getTurnOrder().get(logic.getCurrentTurnIndex());
        if (!(currentEntity instanceof Player)) {
            return;
        }

        Player currentPlayer = (Player) currentEntity;
        if (currentPlayer.getClassType().equals("Esqueleto Aliado")) {
            return;
        }

        int keyCode = e.getKeyCode();
        // Initialize original coordinates as final to ensure they are effectively final
        final int newX = currentPlayer.getX();
        final int newY = currentPlayer.getY();
        // Initialize updated coordinates for movement
        final int updatedX;
        final int updatedY;

        // Assign updated coordinates based on key pressed without modifying newX or newY
        switch (keyCode) {
            case KeyEvent.VK_UP:
                updatedX = newX;
                updatedY = newY - 1;
                break;
            case KeyEvent.VK_DOWN:
                updatedX = newX;
                updatedY = newY + 1;
                break;
            case KeyEvent.VK_LEFT:
                updatedX = newX - 1;
                updatedY = newY;
                break;
            case KeyEvent.VK_RIGHT:
                updatedX = newX + 1;
                updatedY = newY;
                break;
            case KeyEvent.VK_SPACE:
                if (logic.canAttack(currentPlayer)) {
                    Enemy target = null;
                    int attackRange = currentPlayer.getClassType().equals("Cazador de Zombies") ? 2 : currentPlayer.getRange();
                    for (Enemy enemy : logic.getEnemies()) {
                        if (Math.abs(enemy.getX() - currentPlayer.getX()) <= attackRange &&
                            Math.abs(enemy.getY() - currentPlayer.getY()) <= attackRange) {
                            target = enemy;
                            break;
                        }
                    }
                    if (target != null) {
                        currentPlayer.attack(target);
                        logic.setHasAttacked(true);
                        if (target.getHealth() <= 0) {
                            logic.getEnemies().remove(target);
                            logic.getTurnOrder().remove(target);
                            logic.addGameMessage("Enemigo eliminado.");
                            if (logic.getEnemies().isEmpty()) {
                                Item key = logic.createItem("Llave", true, false, 0);
                                logic.getKeys().add(key);
                                logic.addGameMessage("¡Último enemigo derrotado! Una llave aparece en el tablero.");
                            }
                        }
                    }
                }
                // No movement occurs, so set updated coordinates to current position
                updatedX = newX;
                updatedY = newY;
                break;
            case KeyEvent.VK_V:
                currentPlayer.useSpecial();
                logic.setHasAttacked(true);
                updatedX = newX;
                updatedY = newY;
                break;
            case KeyEvent.VK_C:
                currentPlayer.useSecondSpecial();
                logic.setHasAttacked(true);
                updatedX = newX;
                updatedY = newY;
                break;
            case KeyEvent.VK_0:
            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
            case KeyEvent.VK_6:
            case KeyEvent.VK_7:
            case KeyEvent.VK_8:
            case KeyEvent.VK_9:
                int itemIndex = keyCode - KeyEvent.VK_0;
                logic.useSharedItem(itemIndex);
                logic.setHasAttacked(true);
                updatedX = newX;
                updatedY = newY;
                break;
            case KeyEvent.VK_ENTER:
                logic.endTurn();
                updatedX = newX;
                updatedY = newY;
                break;
            default:
                // No action taken, no movement
                updatedX = newX;
                updatedY = newY;
                return;
        }

        if (updatedX != currentPlayer.getX() || updatedY != currentPlayer.getY()) {
            if (logic.getMovesLeft() > 0 && updatedX >= 0 && updatedX < GameLogic.BOARD_SIZE && updatedY >= 0 && updatedY < GameLogic.BOARD_SIZE && !logic.isOccupied(updatedX, updatedY)) {
                for (Trap t : logic.getTraps()) {
                    if (t.getX() == updatedX && t.getY() == updatedY && t.isActive()) {
                        if (t.getType().equals("Pitfall")) {
                            currentPlayer.setHealth(currentPlayer.getHealth() - 20);
                            t.setActive(false);
                            logic.addGameMessage(currentPlayer.getName() + " cae en una trampa (-20 HP).");
                            if (currentPlayer.getHealth() <= 0) {
                                logic.addGameMessage(currentPlayer.getName() + " ha muerto.");
                            }
                        } else if (t.getType().equals("Laser")) {
                            currentPlayer.setHealth(currentPlayer.getHealth() - 10);
                            logic.addGameMessage(currentPlayer.getName() + " recibe daño de un láser (-10 HP).");
                            if (currentPlayer.getHealth() <= 0) {
                                logic.addGameMessage(currentPlayer.getName() + " ha muerto.");
                            }
                        } else if (t.getType().equals("LockedDoor")) {
                            ArrayList<Item> keys = logic.getKeys();
                            boolean hasKey = false;
                            for (Item key : keys) {
                                if (key.getX() == updatedX && key.getY() == updatedY) {
                                    hasKey = true;
                                    break;
                                }
                            }
                            if (!hasKey) {
                                for (Item item : logic.getSharedInventory()) {
                                    if (item.getName().equals("Llave")) {
                                        hasKey = true;
                                        logic.getSharedInventory().remove(item);
                                        break;
                                    }
                                }
                            }
                            if (hasKey) {
                                t.setActive(false);
                                keys.removeIf(k -> k.getX() == updatedX && k.getY() == updatedY);
                                logic.addGameMessage(currentPlayer.getName() + " usa una llave para abrir la puerta.");
                                logic.advanceToNextLevel(null);
                                return;
                            } else {
                                logic.addGameMessage(currentPlayer.getName() + " necesita una llave para abrir la puerta.");
                                return;
                            }
                        }
                    }
                }

                currentPlayer.setX(updatedX);
                currentPlayer.setY(updatedY);
                logic.setMovesLeft(logic.getMovesLeft() - 1);
                logic.addGameMessage(currentPlayer.getName() + " se mueve a (" + updatedX + "," + updatedY + ").");

                ArrayList<Item> itemsToRemove = new ArrayList<>();
                for (Item item : logic.getItems()) {
                    if (item.getX() == updatedX && item.getY() == updatedY) {
                        if (logic.getSharedInventory().size() < GameLogic.MAX_INVENTORY_SIZE) {
                            logic.getSharedInventory().add(item);
                            itemsToRemove.add(item);
                            logic.addGameMessage(currentPlayer.getName() + " recoge " + item.getName() + ".");
                        } else {
                            logic.addGameMessage("Inventario lleno. No se puede recoger " + item.getName() + ".");
                        }
                    }
                }
                logic.getItems().removeAll(itemsToRemove);

                ArrayList<Item> keysToRemove = new ArrayList<>();
                for (Item key : logic.getKeys()) {
                    if (key.getX() == updatedX && key.getY() == updatedY) {
                        if (logic.getSharedInventory().size() < GameLogic.MAX_INVENTORY_SIZE) {
                            logic.getSharedInventory().add(key);
                            keysToRemove.add(key);
                            logic.addGameMessage(currentPlayer.getName() + " recoge una Llave.");
                        } else {
                            logic.addGameMessage("Inventario lleno. No se puede recoger la Llave.");
                        }
                    }
                }
                logic.getKeys().removeAll(keysToRemove);
            }
        }

        // Example lambda using effectively final variables newX and newY
        SwingUtilities.invokeLater(() -> {
            logic.addGameMessage(currentPlayer.getName() + " started at (" + newX + "," + newY + ")");
        });

        logic.notifyRepaint();
    }
}