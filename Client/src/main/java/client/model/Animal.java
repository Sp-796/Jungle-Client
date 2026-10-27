// Paquete del modelo del cliente: contiene los animales del cliente.
package client.model;

// Importa BaseAnimal del paquete comun (herencia).
import common.model.BaseAnimal;
// Importa las constantes del cliente.
import constants.IConstants;

/**
 * Animal del cliente.
 *
 * Hereda id, especie, energia y posicion de {@link BaseAnimal}. Agrega el
 * destino local para interpolar el movimiento entre actualizaciones del
 * servidor y los datos que solo necesita la interfaz (zona, dueno, vida).
 */
// Clase que extiende BaseAnimal y agrega logica del cliente.
public class Animal extends BaseAnimal {

    // Coordenada x del destino.
    private double targetX;
    // Coordenada y del destino.
    private double targetY;
    // Zona actual del animal.
    private Zone currentZone;
    // Indica si esta vivo.
    private boolean isAlive;

    // Id de la zona en la que el servidor ubica al animal.
    private String zoneId;

    // "ME", "BOT" u "OPPONENT" segun el ultimo STATE_UPDATE.
    private String owner = "OPPONENT";

    // Indica si se esta moviendo.
    private boolean moving;

    // Constructor.
    public Animal(int id, String species, int currentEnergy, double x, double y) {
        // Llama al constructor de BaseAnimal.
        super(id, species, currentEnergy, x, y);
        // Asigna el destino inicial.
        this.targetX = x;
        this.targetY = y;
        // Determina si esta vivo.
        this.isAlive = currentEnergy > 0;
    }

    // Mueve la posicion local hacia el destino.
    public void updateLocalPosition(int deltaTime) {
        // Si no esta vivo o el delta no es positivo, no se mueve.
        if (!isAlive || deltaTime <= 0) {
            return;
        }

        // Paso maximo en este frame.
        double maxStep = IConstants.animalSpeed * (deltaTime / 1000.0);
        // Diferencia en x.
        double deltaX = targetX - getX();
        // Diferencia en y.
        double deltaY = targetY - getY();
        // Distancia al destino.
        double distance = Math.hypot(deltaX, deltaY);

        // Si ya llego, se detiene.
        if (distance <= maxStep || distance == 0.0) {
            setX(targetX);
            setY(targetY);
            moving = false;
            return;
        }

        // Avanza parcialmente.
        setX(getX() + (deltaX / distance) * maxStep);
        setY(getY() + (deltaY / distance) * maxStep);
    }

    // Aplica una instantanea del servidor.
    public void applyServerState(String species,
                                 int energy,
                                 double serverX,
                                 double serverY,
                                 boolean alive,
                                 String zoneId,
                                 String owner,
                                 double targetX,
                                 double targetY,
                                 boolean moving) {
        // Actualiza la especie si es valida.
        if (species != null && !species.isBlank()) {
            this.species = species;
        }

        // Actualiza la energia.
        setCurrentEnergy(energy);
        // Actualiza si esta vivo.
        this.isAlive = alive;
        // Actualiza la zona.
        this.zoneId = zoneId;
        // Actualiza el dueno.
        this.owner = owner == null || owner.isBlank() ? "OPPONENT" : owner;
        // Actualiza el destino.
        this.targetX = targetX;
        this.targetY = targetY;
        // Actualiza si se mueve.
        this.moving = moving;

        // Error entre la posicion local y la del servidor.
        double error = Math.hypot(serverX - getX(), serverY - getY());
        // Si no se mueve o el error es grande, corrige la posicion.
        if (!moving || error > 30.0) {
            setX(serverX);
            setY(serverY);
        }
    }

    // Indica si el animal es del jugador local.
    public boolean isMine() {
        return "ME".equals(owner);
    }

    // Indica si el animal es un bot.
    public boolean isBot() {
        return "BOT".equals(owner);
    }

    // Getter de targetX.
    public double getTargetX() {
        return targetX;
    }

    // Getter de targetY.
    public double getTargetY() {
        return targetY;
    }

    // Setter del destino.
    public void setTarget(double targetX, double targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    // Setter de targetX.
    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    // Setter de targetY.
    public void setTargetY(double targetY) {
        this.targetY = targetY;
    }

    // Getter de la zona actual.
    public Zone getCurrentZone() {
        return currentZone;
    }

    // Setter de la zona actual.
    public void setCurrentZone(Zone currentZone) {
        this.currentZone = currentZone;
    }

    // Indica si esta vivo.
    public boolean isAlive() {
        return isAlive;
    }

    // Setter de vivo.
    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    // Getter de zoneId.
    public String getZoneId() {
        return zoneId;
    }

    // Setter de zoneId.
    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    // Getter del dueno.
    public String getOwner() {
        return owner;
    }

    // Setter del dueno.
    public void setOwner(String owner) {
        this.owner = owner == null || owner.isBlank() ? "OPPONENT" : owner;
    }

    // Indica si se mueve.
    public boolean isMoving() {
        return moving;
    }

    // Setter de moving.
    public void setMoving(boolean moving) {
        this.moving = moving;
    }
}