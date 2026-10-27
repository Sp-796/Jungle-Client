// Paquete del modelo del cliente: contiene las zonas del mapa.
package client.model;

// Importa BaseZone del paquete comun (herencia).
import common.model.BaseZone;
// Importa Point2D del paquete comun.
import common.model.Point2D;
// Importa ArrayList para listas.
import java.util.ArrayList;
// Importa Collections para listas inmutables.
import java.util.Collections;
// Importa List para listas.
import java.util.List;

/**
 * Zona del mapa en el cliente.
 */
// Clase que extiende BaseZone y representa una zona del mapa en el cliente.
public class Zone extends BaseZone {

    // Nombre visible de la zona.
    private final String displayName;
    // Nombre de la imagen.
    private final String imageName;
    // Coordenada x.
    private final double x;
    // Coordenada y.
    private final double y;
    // Ancho.
    private final double width;
    // Alto.
    private final double height;

    // Caminos conectados.
    private final List<Path> connectedPaths = new ArrayList<>();
    // Animales presentes.
    private final List<Animal> presentAnimals = new ArrayList<>();
    // Tazas presentes.
    private final List<CoffeeCup> presentCoffeeCups = new ArrayList<>();

    // Indica si es visible.
    private boolean isVisible;

    // Datos de la bandera.
    private String flagOwnerId;
    private String flagColor;
    private double flagX;
    private double flagY;

    // Constructor simplificado.
    public Zone(String zoneId, boolean hasFlag) {
        this(zoneId, zoneId, null, 0, 0, 0, 0, hasFlag);
    }

    // Constructor completo.
    public Zone(String zoneId,
                String displayName,
                String imageName,
                double x,
                double y,
                double width,
                double height,
                boolean hasFlag) {
        // Llama al constructor de BaseZone.
        super(zoneId, hasFlag);
        // Asigna el nombre visible.
        this.displayName = displayName == null ? zoneId : displayName;
        // Asigna la imagen.
        this.imageName = imageName;
        // Asigna x.
        this.x = x;
        // Asigna y.
        this.y = y;
        // Asigna ancho.
        this.width = width;
        // Asigna alto.
        this.height = height;
    }

    // Retorna los caminos conectados.
    public List<Path> getConnectedPaths() {
        return Collections.unmodifiableList(connectedPaths);
    }

    // Agrega un camino evitando duplicados.
    public void addPath(Path path) {
        if (path == null) {
            return;
        }
        for (Path existing : connectedPaths) {
            if (existing.getKey().equals(path.getKey())) {
                return;
            }
        }
        connectedPaths.add(path);
    }

    // Retorna los animales presentes.
    public List<Animal> getPresentAnimals() {
        return Collections.unmodifiableList(presentAnimals);
    }

    // Limpia los animales presentes.
    public void clearPresentAnimals() {
        presentAnimals.clear();
    }

    // Agrega un animal presente.
    public void addPresentAnimal(Animal animal) {
        if (animal != null && !presentAnimals.contains(animal)) {
            presentAnimals.add(animal);
        }
    }

    // Retorna las tazas presentes.
    public List<CoffeeCup> getPresentCoffeeCups() {
        return Collections.unmodifiableList(presentCoffeeCups);
    }

    // Limpia las tazas presentes.
    public void clearCoffeeCups() {
        presentCoffeeCups.clear();
    }

    // Agrega una taza presente.
    public void addCoffeeCup(CoffeeCup cup) {
        if (cup != null && !presentCoffeeCups.contains(cup)) {
            presentCoffeeCups.add(cup);
        }
    }

    // Indica si es visible.
    public boolean isVisible() {
        return isVisible;
    }

    // Setter de visible.
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    // Indica si un punto esta dentro.
    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width
                && pointY >= y && pointY <= y + height;
    }

    // Retorna el centro.
    public Point2D getCenter() {
        return new Point2D(x + width / 2.0, y + height / 2.0);
    }

    // Asigna los datos de la bandera.
    public void setFlag(String ownerId, String color, double flagX, double flagY) {
        this.flagOwnerId = ownerId;
        this.flagColor = color;
        this.flagX = flagX;
        this.flagY = flagY;
        setHasFlag(ownerId != null);
    }

    // Limpia la bandera.
    public void clearFlag() {
        this.flagOwnerId = null;
        this.flagColor = null;
        this.flagX = 0;
        this.flagY = 0;
        setHasFlag(false);
    }

    // Getter del dueno de la bandera.
    public String getFlagOwnerId() {
        return flagOwnerId;
    }

    // Getter del color de la bandera.
    public String getFlagColor() {
        return flagColor;
    }

    // Getter de flagX.
    public double getFlagX() {
        return flagX;
    }

    // Getter de flagY.
    public double getFlagY() {
        return flagY;
    }

    // Getter del nombre visible.
    public String getDisplayName() {
        return displayName;
    }

    // Getter del nombre de imagen.
    public String getImageName() {
        return imageName;
    }

    // Getter de x.
    public double getX() {
        return x;
    }

    // Getter de y.
    public double getY() {
        return y;
    }

    // Getter de ancho.
    public double getWidth() {
        return width;
    }

    // Getter de alto.
    public double getHeight() {
        return height;
    }
}