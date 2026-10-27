// Paquete comun del modelo: contiene los datos minimos de un animal compartidos entre cliente y servidor.
package common.model;

/**
 * Datos de un animal que comparten el cliente y el servidor.
 *
 * La version del servidor agrega ruta, zona y combate; la del cliente agrega
 * interpolacion y datos visuales.
 */
// Clase abstracta que sirve de plantilla para los animales del cliente (herencia).
public abstract class BaseAnimal {

    // Identificador unico del animal.
    protected int id;
    // Especie del animal.
    protected String species;
    // Energia actual del animal.
    protected int currentEnergy;
    // Posicion horizontal del animal.
    protected double x;
    // Posicion vertical del animal.
    protected double y;

    // Constructor protegido usado por las clases hijas.
    protected BaseAnimal(int id, String species, int currentEnergy, double x, double y) {
        // Asigna el id.
        this.id = id;
        // Asigna la especie.
        this.species = species;
        // Asigna la energia.
        this.currentEnergy = currentEnergy;
        // Asigna x.
        this.x = x;
        // Asigna y.
        this.y = y;
    }

    // Getter del id.
    public int getId() {
        // Retorna id.
        return id;
    }

    // Getter de la especie.
    public String getSpecies() {
        // Retorna species.
        return species;
    }

    // Getter de la energia.
    public int getCurrentEnergy() {
        // Retorna currentEnergy.
        return currentEnergy;
    }

    // Setter de la energia.
    public void setCurrentEnergy(int currentEnergy) {
        // Asigna currentEnergy.
        this.currentEnergy = currentEnergy;
    }

    // Getter de x.
    public double getX() {
        // Retorna x.
        return x;
    }

    // Setter de x.
    public void setX(double x) {
        // Asigna x.
        this.x = x;
    }

    // Getter de y.
    public double getY() {
        // Retorna y.
        return y;
    }

    // Setter de y.
    public void setY(double y) {
        // Asigna y.
        this.y = y;
    }
}