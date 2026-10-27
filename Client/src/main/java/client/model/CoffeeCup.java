// Paquete del modelo del cliente: contiene las tazas de cafe visibles.
package client.model;

/**
 * Taza de cafe que el servidor permite ver en la zona actual.
 */
// Clase final que representa una taza de cafe visible.
public final class CoffeeCup {

    // Id de la taza.
    private final String cupId;
    // Coordenada x.
    private final double x;
    // Coordenada y.
    private final double y;

    // Constructor.
    public CoffeeCup(String cupId, double x, double y) {
        // Asigna el id.
        this.cupId = cupId;
        // Asigna x.
        this.x = x;
        // Asigna y.
        this.y = y;
    }

    // Getter del id.
    public String getCupId() {
        // Retorna cupId.
        return cupId;
    }

    // Getter de x.
    public double getX() {
        // Retorna x.
        return x;
    }

    // Getter de y.
    public double getY() {
        // Retorna y.
        return y;
    }
}