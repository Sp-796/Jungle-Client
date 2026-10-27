// Paquete de control del cliente: contiene la interfaz de eventos.
package control;

// Importa GameState para el evento de inicio.
import client.model.GameState;
// Importa MatchResult para el evento de fin.
import client.model.MatchResult;
// Importa List para listas.
import java.util.List;

/**
 * Eventos del cliente que no son una simple actualizacion de estado.
 */
// Interfaz con metodos default para eventos del cliente (patron Observer).
public interface ClientEventListener {

    // Evento de conexion.
    default void onConnected() {
    }

    // Evento de desconexion.
    default void onDisconnected(String reason) {
    }

    // Partida creada.
    default void onMatchCreated(String matchCode, String playerId, String playerName) {
    }

    // Partida unida.
    default void onMatchJoined(String matchCode,
                               String playerId,
                               String playerName,
                               int playerNumber,
                               String opponentName,
                               List<String> availableSpecies) {
    }

    // Seleccion de animales.
    default void onAnimalSelection(String yourSpecies,
                                   String opponentSpecies,
                                   List<String> availableSpecies) {
    }

    // Partida iniciada.
    default void onGameStarted(GameState gameState) {
    }

    // Partida terminada.
    default void onGameOver(MatchResult result) {
    }

    // Error.
    default void onError(String message) {
    }

    // Cafe recogido.
    default void onCoffeeCollected(String cupId, int damageCapacity) {
    }
}