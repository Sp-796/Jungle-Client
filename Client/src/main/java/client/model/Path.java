// Paquete del modelo del cliente: contiene los caminos visuales.
package client.model;

// Importa Point2D del paquete comun.
import common.model.Point2D;

/**
 * Camino visual entre dos zonas.
 */
// Clase final que representa un camino visual.
public final class Path {

    // Id de la zona de origen.
    private final String fromZoneId;
    // Id de la zona de destino.
    private final String toZoneId;
    // Coordenada x1.
    private final double x1;
    // Coordenada y1.
    private final double y1;
    // Coordenada x2.
    private final double x2;
    // Coordenada y2.
    private final double y2;

    // Constructor.
    public Path(String fromZoneId, String toZoneId, Point2D from, Point2D to) {
        // Asigna el id de origen.
        this.fromZoneId = fromZoneId;
        // Asigna el id de destino.
        this.toZoneId = toZoneId;
        // Asigna x1.
        this.x1 = from.getX();
        // Asigna y1.
        this.y1 = from.getY();
        // Asigna x2.
        this.x2 = to.getX();
        // Asigna y2.
        this.y2 = to.getY();
    }

    // Getter de fromZoneId.
    public String getFromZoneId() {
        return fromZoneId;
    }

    // Getter de toZoneId.
    public String getToZoneId() {
        return toZoneId;
    }

    // Getter de x1.
    public double getX1() {
        return x1;
    }

    // Getter de y1.
    public double getY1() {
        return y1;
    }

    // Getter de x2.
    public double getX2() {
        return x2;
    }

    // Getter de y2.
    public double getY2() {
        return y2;
    }

    // Clave estable para dibujar una sola vez cada arista.
    public String getKey() {
        return fromZoneId.compareTo(toZoneId) < 0
                ? fromZoneId + "|" + toZoneId
                : toZoneId + "|" + fromZoneId;
    }
}