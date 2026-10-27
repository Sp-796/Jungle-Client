// Paquete de la UI: contiene la interfaz Observer.
package ui;

// Importa GameState para la actualizacion.
import client.model.GameState;

/**
 * Interfaz Observer del cliente.
 */
// Interfaz que define el metodo update (patron Observer).
public interface Observer {

    // Actualiza la vista con el nuevo estado.
    void update(GameState gameState);
}