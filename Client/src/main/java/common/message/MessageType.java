// Paquete comun de mensajes: contiene el catalogo de tipos de mensaje del protocolo.
package common.message;

/**
 * Catalogo de tipos de mensaje del protocolo.
 */
// Clase final con constantes String para los tipos de mensaje.
public final class MessageType {

    // Constructor privado.
    private MessageType() {
        // Clase de constantes.
    }

    // Tipos Cliente -> Servidor.
    public static final String CREATE_MATCH = "CREATE_MATCH";
    public static final String JOIN_MATCH = "JOIN_MATCH";
    public static final String SELECT_ANIMAL = "SELECT_ANIMAL";
    public static final String MOVE_ANIMALS = "MOVE_ANIMALS";
    public static final String ATTACK = "ATTACK";
    public static final String VIEW_ZONE = "VIEW_ZONE";

    // Tipos Servidor -> Cliente.
    public static final String MATCH_CREATED = "MATCH_CREATED";
    public static final String MATCH_JOINED = "MATCH_JOINED";
    public static final String ANIMAL_SELECTION = "ANIMAL_SELECTION";
    public static final String GAME_STARTED = "GAME_STARTED";
    public static final String STATE_UPDATE = "STATE_UPDATE";
    public static final String GAME_OVER = "GAME_OVER";
    public static final String ERROR = "ERROR";

    // Eventos puntuales.
    public static final String COFFEE_COLLECTED = "COFFEE_COLLECTED";
}