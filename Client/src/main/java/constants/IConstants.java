package constants;

/**
 * Constantes del cliente.
 *
 * Se conservan los nombres del enunciado (camelCase) para que el cliente y el
 * servidor sean faciles de comparar. Los valores que tambien usa el servidor
 * (velocidad, radios, tamano del mundo) deben coincidir con los suyos.
 */
public interface IConstants {

    // ------------------------------------------------------------------
    // Estadisticas de los participantes (spec del cliente)
    // ------------------------------------------------------------------

    int initialEnergyPlayer = 20;
    int initialEnergyBot = 10;
    int maxEnergy = 20;

    int initialDamagePlayer = 2;
    int initialDamageBot = 1;
    int maxDamageCapacityPlayer = 12;
    int maxDamageCapacityBot = 1;
    int attackCooldown = 1000;

    int maxTeamSizePlayer = 10;
    int maxTeamSizeBot = 4;
    int botPackCount = 5;
    int maxAmountCoffee = 10;
    int maxZones = 12;

    double animalSpeed = 50.0;
    double encounterRadius = 15.0;
    int refreshRate = 30;

    String defaultServerIP = "localhost";
    int defaultServerPort = 8080;

    int windowWidth = 1280;
    int windowHeight = 720;
    String assetsPath = "/assets/images/";

    // ------------------------------------------------------------------
    // Geometria del mundo (el servidor manda estos valores en GAME_STARTED)
    // ------------------------------------------------------------------

    int worldWidth = 1280;
    int worldHeight = 720;
    int zoneColumns = 4;
    int zoneRows = 3;

    // ------------------------------------------------------------------
    // Dibujo
    // ------------------------------------------------------------------

    /** Tamano en unidades del mundo con el que se dibuja un animal. */
    double animalSpriteSize = 40.0;

    /** Tamano en unidades del mundo de una taza de cafe. */
    double coffeeSpriteSize = 24.0;

    /** Distancia (unidades del mundo) para considerar que se hizo clic en un animal. */
    double clickRadius = 22.0;

    /** Margen del mundo que se deja libre al dibujar. */
    int mapPadding = 4;

    // ------------------------------------------------------------------
    // Red y protocolo
    // ------------------------------------------------------------------

    int connectionTimeoutMs = 5000;
    int maxMessageLength = 64 * 1024;
    int maxNicknameLength = 20;
    String defaultNickname = "Jugador";

    // ------------------------------------------------------------------
    // Transporte UDP
    // ------------------------------------------------------------------

    /** Milisegundos antes de reenviar un mensaje sin ACK. */
    int udpRetransmitMs = 150;

    /** Milisegundos entre PING de mantenimiento. */
    int udpKeepAliveMs = 2000;

    /** Milisegundos sin recibir nada tras los cuales se da la conexion por perdida. */
    int udpTimeoutMs = 15000;

    /** Maximo de mensajes pendientes de ACK antes de descartar los nuevos. */
    int udpWindowSize = 256;

    /** Maximo de mensajes adelantados que se guardan esperando al faltante. */
    int udpMaxBufferedMessages = 1024;

    /** Maximo de retransmisiones por pasada de mantenimiento. */
    int udpMaxRetransmitsPerTick = 32;

    /** Milisegundos entre intentos de HELLO durante el handshake. */
    int udpHandshakePollMs = 200;

    /** Tamano maximo de un datagrama. */
    int udpMaxDatagramSize = 60000;

    /** Buffer de recepcion del socket UDP. */
    int udpReceiveBufferSize = 1_048_576;

    /** Milisegundos entre pasadas de retransmision y deteccion de timeout. */
    int udpHousekeepingMs = 50;

    // ------------------------------------------------------------------
    // Colores de las banderas (los manda el servidor como texto)
    // ------------------------------------------------------------------

    String flagColorPlayerOne = "Guaria Morada";
    String flagColorPlayerTwo = "Yiguirro";

    // ------------------------------------------------------------------
    // Motivos de fin de partida (viajan en GAME_OVER.reason)
    // ------------------------------------------------------------------

    String finishReasonFlagTouched = "FLAG_TOUCHED";
    String finishReasonPackDefeated = "PACK_DEFEATED";
    String finishReasonDisconnect = "DISCONNECT";
    String finishReasonOpponentLeft = "OPPONENT_LEFT";
}
