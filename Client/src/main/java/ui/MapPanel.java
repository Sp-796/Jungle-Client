// Paquete de la UI: contiene el panel del mapa.
package ui;

// Importa Animal para dibujar.
import client.model.Animal;
// Importa CoffeeCup para dibujar.
import client.model.CoffeeCup;
// Importa GameState para el estado.
import client.model.GameState;
// Importa Zone para las zonas.
import client.model.Zone;
// Importa las constantes del cliente.
import constants.IConstants;
// Importa Color para colores.
import java.awt.Color;
// Importa Dimension para tamanos.
import java.awt.Dimension;
// Importa Font para fuentes.
import java.awt.Font;
// Importa Graphics para dibujar.
import java.awt.Graphics;
// Importa Graphics2D para dibujar.
import java.awt.Graphics2D;
// Importa RenderingHints para calidad.
import java.awt.RenderingHints;
// Importa AffineTransform para transformaciones.
import java.awt.geom.AffineTransform;
// Importa BufferedImage para imagenes.
import java.awt.image.BufferedImage;
// Importa HashSet para conjuntos.
import java.util.HashSet;
// Importa Set para conjuntos.
import java.util.Set;
// Importa Observer para recibir actualizaciones.
import ui.Observer;

/**
 * Panel que dibuja el mapa, las zonas, los caminos, los animales y las tazas.
 */
// Clase que extiende JPanel e implementa Observer.
@SuppressWarnings({"serial", "this-escape", "cast"})
public class MapPanel extends javax.swing.JPanel implements Observer {

    // Cargador de sprites.
    private final SpriteLoader sprites = new SpriteLoader();
    // Conjunto de ids de animales seleccionados.
    private final Set<Integer> selectedAnimalIds = new HashSet<>();
    // Estado actual del juego.
    private GameState currentGameState;


    // Constructor.
    public MapPanel() {
        setBackground(new Color(12, 34, 20));
        setPreferredSize(new Dimension(IConstants.windowWidth, IConstants.windowHeight));
        setDoubleBuffered(true);
    }

    // Actualiza el estado (tambien implementa Observer).
    @Override
    public void update(GameState gameState) {
        this.currentGameState = gameState;
        repaint();
    }

    // Establece los ids seleccionados.
    public void setSelectedAnimalIds(java.util.Collection<Integer> ids) {
        selectedAnimalIds.clear();
        if (ids != null) {
            selectedAnimalIds.addAll(ids);
        }
        repaint();
    }

    // Convierte coordenadas de pantalla a coordenadas del mundo.
    public common.model.Point2D toWorld(int screenX, int screenY) {
        if (currentGameState == null) {
            return null;
        }
        // Retorna el punto.
        return new common.model.Point2D(screenX, screenY);
    }

    // Retorna el animal en una posicion del mundo.
    public client.model.Animal getAnimalAt(double worldX, double worldY) {
        if (currentGameState == null) {
            return null;
        }
        // Recorre los animales visibles.
        for (client.model.Animal animal : currentGameState.getVisibleAnimals()) {
            double dx = worldX - animal.getX();
            double dy = worldY - animal.getY();
            if (Math.hypot(dx, dy) <= IConstants.clickRadius) {
                return animal;
            }
        }
        // Recorre los animales propios.
        for (client.model.Animal animal : currentGameState.getMyAnimals()) {
            double dx = worldX - animal.getX();
            double dy = worldY - animal.getY();
            if (Math.hypot(dx, dy) <= IConstants.clickRadius) {
                return animal;
            }
        }
        return null;
    }

    // Dibuja el panel.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentGameState == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibuja las zonas.
        for (Zone zone : currentGameState.getMapZones().values()) {
            java.awt.Image image = sprites.zone(zone.getImageName());
            if (image != null) {
                g2d.drawImage(image, (int) zone.getX(), (int) zone.getY(),
                        (int) zone.getWidth(), (int) zone.getHeight(), null);
            } else {
                g2d.setColor(new Color(30, 60, 40));
                g2d.fillRect((int) zone.getX(), (int) zone.getY(),
                        (int) zone.getWidth(), (int) zone.getHeight());
            }
        }

        // Dibuja los caminos.
        g2d.setColor(new Color(120, 90, 50));
        for (client.model.Path path : currentGameState.getAllPaths()) {
            g2d.drawLine((int) path.getX1(), (int) path.getY1(),
                    (int) path.getX2(), (int) path.getY2());
        }

        // Dibuja los animales.
        for (client.model.Animal animal : currentGameState.getVisibleAnimals()) {
            drawAnimal(g2d, animal);
        }
        for (client.model.Animal animal : currentGameState.getMyAnimals()) {
            drawAnimal(g2d, animal);
        }

        // Dibuja las tazas.
        for (client.model.CoffeeCup cup : currentGameState.getVisibleCoffeeCups()) {
            java.awt.Image image = sprites.coffee();
            if (image != null) {
                int size = (int) IConstants.coffeeSpriteSize;
                g2d.drawImage(image, (int) (cup.getX() - size / 2.0),
                        (int) (cup.getY() - size / 2.0), size, size, null);
            }
        }

        // Dibuja la bandera de la zona vista.
        Zone viewed = currentGameState.getViewedZone();
        if (viewed != null && viewed.hasFlag()) {
            g2d.setColor(Color.YELLOW);
            g2d.fillOval((int) (viewed.getFlagX() - 8), (int) (viewed.getFlagY() - 8), 16, 16);
        }
    }

    // Dibuja un animal.
    private void drawAnimal(Graphics2D g2d, client.model.Animal animal) {
        if (animal == null) {
            return;
        }
        java.awt.Image image = sprites.animal(animal.getSpecies());
        int size = (int) IConstants.animalSpriteSize;
        int x = (int) (animal.getX() - size / 2.0);
        int y = (int) (animal.getY() - size / 2.0);

        if (image != null) {
            g2d.drawImage(image, x, y, size, size, null);
        } else {
            g2d.setColor(animal.isMine() ? Color.CYAN : Color.RED);
            g2d.fillOval(x, y, size, size);
        }

        // Barra de energia.
        int barWidth = size;
        int barHeight = Math.max(3, Math.round(4));
        int barX = x;
        int barY = y - 6;
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        g2d.setColor(animal.isAlive() ? Color.GREEN : Color.RED);
        int filled = (int) (barWidth * (animal.getCurrentEnergy() / (double) IConstants.maxEnergy));
        g2d.fillRect(barX, barY, filled, barHeight);

        // Resaltado de seleccion.
        if (selectedAnimalIds.contains(animal.getId())) {
            g2d.setColor(Color.YELLOW);
            g2d.drawOval(x - 2, y - 2, size + 4, size + 4);
        }
    }


}