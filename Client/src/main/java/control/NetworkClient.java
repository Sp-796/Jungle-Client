// Paquete de control del cliente: contiene el cliente de red UDP.
package control;

// Importa GameState para el estado.
import client.model.GameState;
// Importa MatchResult para el resumen.
import client.model.MatchResult;
// Importa Message para el protocolo.
import common.message.Message;
// Importa MessageType para los tipos.
import common.message.MessageType;
// Importa las constantes del cliente.
import constants.IConstants;
// Importa UdpClientTransport para el transporte.
import control.udp.UdpClientTransport;
// Importa IOException para errores de IO.
import java.io.IOException;
// Importa ArrayList para listas.
import java.util.ArrayList;
// Importa LinkedHashMap para mapas.
import java.util.LinkedHashMap;
// Importa List para listas.
import java.util.List;
// Importa Map para mapas.
import java.util.Map;
// Importa CopyOnWriteArrayList para listas thread-safe.
import java.util.concurrent.CopyOnWriteArrayList;
// Importa AtomicBoolean para flags atomicos.
import java.util.concurrent.atomic.AtomicBoolean;
// Importa Consumer para funciones.
import java.util.function.Consumer;
// Importa SwingUtilities para el EDT.
import javax.swing.SwingUtilities;
// Importa Observer para las vistas.
import ui.Observer;

/**
 * Conexion UDP con el servidor.
 */
// Clase que gestiona la conexion del cliente con el servidor.
public class NetworkClient {

    // Adaptador JSON.
    private final JsonAdapter jsonAdapter = new JsonAdapter();
    // Lista de observadores.
    private final List<Observer> observers = new CopyOnWriteArrayList<>();
    // Lista de oyentes de eventos.
    private final List<ClientEventListener> listeners = new CopyOnWriteArrayList<>();
    // Flag de desconexion notificada.
    private final AtomicBoolean disconnectNotified = new AtomicBoolean(false);

    // Transporte UDP.
    private UdpClientTransport transport;
    // Hilo de escucha.
    private Thread listenerThread;
    // Indica si esta conectado.
    private volatile boolean connected;

    // Registra un observador.
    public void addObserver(Observer observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    // Remueve un observador.
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    // Registra un oyente de eventos.
    public void addEventListener(ClientEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    // Indica si esta conectado.
    public boolean isConnected() {
        return connected;
    }

    // Abre la conexion.
    public void connectToServer(String ip, int port) {
        // Si ya esta conectado, notifica.
        if (connected) {
            fireOnEdt(ClientEventListener::onConnected);
            return;
        }

        // Hilo conector.
        Thread connector = new Thread(() -> {
            try {
                // Conecta el transporte.
                UdpClientTransport newTransport = UdpClientTransport.connect(
                        ip, port, IConstants.connectionTimeoutMs);

                // Asigna el transporte.
                this.transport = newTransport;
                // Marca como conectado.
                this.connected = true;
                // Reinicia el flag.
                this.disconnectNotified.set(false);

                // Notifica la conexion.
                fireOnEdt(ClientEventListener::onConnected);
                // Inicia la escucha.
                listenForUpdate();
            } catch (IOException | RuntimeException error) {
                // Marca como no conectado.
                connected = false;
                // Notifica la desconexion.
                fireOnEdt(listener -> listener.onDisconnected(
                        "No se pudo conectar con " + ip + ":" + port + " (" + error.getMessage() + ")"));
            }
        }, "client-connector");

        // Hilo demonio.
        connector.setDaemon(true);
        // Inicia el hilo.
        connector.start();
    }

    // Inicia el hilo de escucha.
    private void listenForUpdate() {
        // Crea el hilo.
        listenerThread = new Thread(this::readLoop, "client-listener");
        // Hilo demonio.
        listenerThread.setDaemon(true);
        // Inicia el hilo.
        listenerThread.start();
    }

    // Bucle de lectura.
    private void readLoop() {
        // Transporte actual.
        UdpClientTransport current = transport;
        if (current == null) {
            return;
        }

        try {
            String line;
            // Lee mientras haya mensajes.
            while ((line = current.receive()) != null) {
                // Si esta en blanco, salta.
                if (line.isBlank()) {
                    continue;
                }
                // Si es muy grande, envia error.
                if (line.length() > IConstants.maxMessageLength) {
                    fireError("El servidor envio un mensaje demasiado grande");
                    continue;
                }

                // Deserializa el mensaje.
                final Message message;
                try {
                    message = Message.fromJson(line);
                } catch (RuntimeException invalid) {
                    fireError("Mensaje invalido del servidor: " + invalid.getMessage());
                    continue;
                }

                // Enruta en el EDT.
                SwingUtilities.invokeLater(() -> route(message));
            }
        } finally {
            // Si estaba conectado, notifica la desconexion.
            if (connected) {
                notifyDisconnected("Se perdio la conexion con el servidor");
            }
        }
    }

    // Cierra la conexion.
    public void disconnect() {
        // Marca como no conectado.
        connected = false;

        // Cierra el transporte.
        UdpClientTransport current = transport;
        transport = null;
        if (current != null) {
            current.close();
        }

        // Reinicia el flag.
        disconnectNotified.set(false);
    }

    // Notifica la desconexion.
    private void notifyDisconnected(String reason) {
        connected = false;
        closeQuietly();

        if (disconnectNotified.compareAndSet(false, true)) {
            fireOnEdt(listener -> listener.onDisconnected(reason));
        }
    }

    // Cierra silenciosamente.
    private void closeQuietly() {
        UdpClientTransport current = transport;
        transport = null;
        if (current != null) {
            current.close();
        }
    }

    // Envia una accion al servidor.
    public void sendAction(String actionType, Object payload) {
        UdpClientTransport current = transport;

        // Si no esta conectado, envia error.
        if (!connected || current == null) {
            fireError("No hay conexion con el servidor");
            return;
        }

        try {
            // Adapta la accion a JSON.
            String json = jsonAdapter.adaptActionToJson(actionType, payload);
            // Envia el mensaje.
            if (!current.send(json)) {
                fireError("No se pudo enviar la accion: el canal UDP esta saturado");
            }
        } catch (RuntimeException error) {
            fireError("No se pudo enviar la accion: " + error.getMessage());
        }
    }

    // Envia CREATE_MATCH.
    public void sendCreateMatch(String nickname) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nickname", nickname);
        sendAction(MessageType.CREATE_MATCH, payload);
    }

    // Envia JOIN_MATCH.
    public void sendJoinMatch(String nickname, String code) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nickname", nickname);
        payload.put("code", code);
        sendAction(MessageType.JOIN_MATCH, payload);
    }

    // Envia SELECT_ANIMAL.
    public void sendSelectSpecies(String species) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("species", species);
        sendAction(MessageType.SELECT_ANIMAL, payload);
    }

    // Envia MOVE_ANIMALS.
    public void sendMoveAnimals(List<Integer> animalIds, double targetX, double targetY) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("animalIds", new ArrayList<>(animalIds));
        payload.put("targetX", targetX);
        payload.put("targetY", targetY);
        sendAction(MessageType.MOVE_ANIMALS, payload);
    }

    // Envia ATTACK.
    public void sendAttack(List<Integer> animalIds, int targetAnimalId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("animalIds", new ArrayList<>(animalIds));
        payload.put("targetAnimalId", targetAnimalId);
        sendAction(MessageType.ATTACK, payload);
    }

    // Envia VIEW_ZONE.
    public void sendViewZone(String zoneId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("zoneId", zoneId);
        sendAction(MessageType.VIEW_ZONE, payload);
    }

    // Enruta un mensaje del servidor.
    private void route(Message message) {
        try {
            // Enruta segun el tipo.
            switch (message.getType()) {
                case MessageType.MATCH_CREATED -> routeMatchCreated(message);
                case MessageType.MATCH_JOINED -> routeMatchJoined(message);
                case MessageType.ANIMAL_SELECTION -> routeAnimalSelection(message);
                case MessageType.GAME_STARTED -> routeGameStarted(message);
                case MessageType.STATE_UPDATE -> routeStateUpdate(message);
                case MessageType.GAME_OVER -> routeGameOver(message);
                case MessageType.ERROR -> fireError(message.getString("message"));
                case MessageType.COFFEE_COLLECTED -> fireEvent(listener ->
                        listener.onCoffeeCollected(
                                message.getString("cupId"),
                                message.getInt("damageCapacity", 0)));
                default -> {
                    // Un tipo nuevo del servidor no debe romper el cliente.
                }
            }
        } catch (RuntimeException error) {
            fireError("No se pudo procesar el mensaje " + message.getType()
                    + ": " + error.getMessage());
        }
    }

    // Enruta MATCH_CREATED.
    private void routeMatchCreated(Message message) {
        fireEvent(listener -> listener.onMatchCreated(
                message.getString("matchCode"),
                message.getString("playerId"),
                message.getString("playerName")
        ));
    }

    // Enruta MATCH_JOINED.
    private void routeMatchJoined(Message message) {
        List<String> species = readStrings(message, "availableSpecies");
        fireEvent(listener -> listener.onMatchJoined(
                message.getString("matchCode"),
                message.getString("playerId"),
                message.getString("playerName"),
                message.getInt("playerNumber", 0),
                message.getString("opponentName"),
                species
        ));
    }

    // Enruta ANIMAL_SELECTION.
    private void routeAnimalSelection(Message message) {
        List<String> species = readStrings(message, "availableSpecies");
        fireEvent(listener -> listener.onAnimalSelection(
                message.getString("yourSpecies"),
                message.getString("opponentSpecies"),
                species
        ));
    }

    // Enruta GAME_STARTED.
    private void routeGameStarted(Message message) {
        GameState state = jsonAdapter.adaptGameStarted(message.getPayload());
        fireEvent(listener -> listener.onGameStarted(state));
    }

    // Enruta STATE_UPDATE.
    private void routeStateUpdate(Message message) {
        GameState state = jsonAdapter.adaptStateUpdate(message.getPayload());

        // Registra los observadores.
        for (Observer observer : observers) {
            state.addObserver(observer);
        }
        // Notifica a los observadores.
        state.notifyObservers();
    }

    // Enruta GAME_OVER.
    private void routeGameOver(Message message) {
        MatchResult result = jsonAdapter.adaptGameOver(message.getPayload());
        fireEvent(listener -> listener.onGameOver(result));
    }

    // Lee una lista de strings.
    private static List<String> readStrings(Message message, String key) {
        List<String> values = new ArrayList<>();
        for (var element : message.getArray(key)) {
            if (element != null && !element.isJsonNull()) {
                values.add(element.getAsString());
            }
        }
        return values;
    }

    // Envia un error.
    private void fireError(String message) {
        fireEvent(listener -> listener.onError(
                message == null || message.isBlank() ? "Error desconocido" : message));
    }

    // Ejecuta una accion sobre los oyentes.
    private void fireEvent(Consumer<ClientEventListener> action) {
        SwingUtilities.invokeLater(() -> {
            for (ClientEventListener listener : listeners) {
                action.accept(listener);
            }
        });
    }

    // Ejecuta en el EDT.
    private void fireOnEdt(Consumer<ClientEventListener> action) {
        fireEvent(action);
    }
}