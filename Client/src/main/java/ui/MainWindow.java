// Paquete de la UI: contiene la ventana principal.
package ui;

// Importa GameState para el estado.
import client.model.GameState;
// Importa MatchResult para el resumen final.
import client.model.MatchResult;
// Importa ClientEventListener para eventos.
import control.ClientEventListener;
// Importa ClientGameLoop para la animacion.
import control.ClientGameLoop;
// Importa InputController para la entrada.
import control.InputController;
// Importa NetworkClient para la red.
import control.NetworkClient;
// Importa las constantes del cliente.
import constants.IConstants;
// Importa BorderLayout para el layout.
import java.awt.BorderLayout;
// Importa CardLayout para las pantallas.
import java.awt.CardLayout;
// Importa Dimension para tamanos.
import java.awt.Dimension;
// Importa WindowAdapter para eventos de ventana.
import java.awt.event.WindowAdapter;
// Importa WindowEvent para eventos de ventana.
import java.awt.event.WindowEvent;
// Importa List para listas.
import java.util.List;
// Importa JFrame para la ventana.
import javax.swing.JFrame;
// Importa JOptionPane para dialogos.
import javax.swing.JOptionPane;
// Importa JPanel para paneles.
import javax.swing.JPanel;
// Importa SwingUtilities para el EDT.
import javax.swing.SwingUtilities;

/**
 * Ventana principal y coordinador de la interfaz.
 */
// Clase que implementa Observer y ClientEventListener.
@SuppressWarnings({"serial", "this-escape"})
public class MainWindow implements Observer, ClientEventListener {

    // Frame principal.
    private final JFrame frame = new JFrame("Jungle Attack");
    // Layout de tarjetas.
    private final CardLayout cardLayout = new CardLayout();
    // Panel de tarjetas.
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Panel del lobby.
    private final LobbyPanel lobbyPanel = new LobbyPanel();

    // Panel del mapa.
    public final MapPanel mapPanel = new MapPanel();
    // Panel del HUD.
    public final HUDPanel hudPanel = new HUDPanel();

    // Cliente de red.
    private final NetworkClient networkClient;
    // Controlador de entrada.
    private final InputController inputController;
    // Bucle de animacion.
    private final ClientGameLoop gameLoop;

    // Estado actual.
    private GameState currentState;
    // Id del jugador.
    private String playerId = "";
    // Codigo de la partida.
    private String matchCode = "";
    // Indica si esta en juego.
    private boolean inGame;
    // Indica si ya se manejo el fin.
    private boolean gameOverHandled;
    // Accion pendiente de conexion.
    private Runnable pendingConnectAction;

    // Constructor.
    public MainWindow(NetworkClient networkClient) {
        this.networkClient = networkClient;
        this.inputController = new InputController(networkClient, mapPanel);
        this.gameLoop = new ClientGameLoop(() -> currentState, mapPanel::repaint);

        networkClient.addObserver(this);
        networkClient.addObserver(mapPanel);
        networkClient.addObserver(hudPanel);
        networkClient.addEventListener(this);

        initializeWindow();
        wireActions();
    }

    // Inicializa la ventana.
    private void initializeWindow() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(IConstants.windowWidth, IConstants.windowHeight);
        frame.setMinimumSize(new Dimension(960, 620));
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                networkClient.disconnect();
                frame.dispose();
                System.exit(0);
            }
        });

        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(mapPanel.getBackground());
        gamePanel.add(hudPanel, BorderLayout.NORTH);
        gamePanel.add(mapPanel, BorderLayout.CENTER);

        cardPanel.add(lobbyPanel, "lobby");
        cardPanel.add(gamePanel, "game");
        frame.setContentPane(cardPanel);
        cardLayout.show(cardPanel, "lobby");
    }

    // Conecta las acciones.
    private void wireActions() {
        lobbyPanel.setOnCreate(request ->
                startConnectionAttempt(request.host(), request.port(),
                        () -> networkClient.sendCreateMatch(request.nickname())));

        lobbyPanel.setOnJoin(request ->
                startConnectionAttempt(request.host(), request.port(),
                        () -> networkClient.sendJoinMatch(request.nickname(), request.code())));

        lobbyPanel.setOnSpecies(species -> networkClient.sendSelectSpecies(species));

        hudPanel.setZoneSelectionListener(inputController::handleZoneViewIntent);
        hudPanel.setSelectAllAction(inputController::selectAll);
        mapPanel.addMouseListener(inputController);
        inputController.setMessageSink(hudPanel::setError);
    }

    // Muestra la ventana.
    public void showWindow() {
        frame.setVisible(true);
        SwingUtilities.invokeLater(lobbyPanel::requestFocusInWindow);
    }

    // Actualizacion del estado (Observer).
    @Override
    public void update(GameState gameState) {
        if (gameState == null) {
            return;
        }

        currentState = gameState;

        if (!inGame) {
            inGame = true;
            cardLayout.show(cardPanel, "game");
        }

        inputController.setGameState(gameState);
        frame.setTitle("Jungle Attack - " + safe(gameState.getPlayerName())
                + " vs " + safe(gameState.getOpponentName()));
    }

    // Evento de conexion.
    @Override
    public void onConnected() {
        lobbyPanel.setStatus("Conectado. Enviando solicitud...", false);

        Runnable action = pendingConnectAction;
        pendingConnectAction = null;
        if (action != null) {
            action.run();
        }
    }

    // Evento de desconexion.
    @Override
    public void onDisconnected(String reason) {
        gameLoop.stop();
        currentState = null;

        if (gameOverHandled) {
            return;
        }

        inGame = false;
        inputController.reset();
        mapPanel.update((GameState) null);
        hudPanel.update((GameState) null);
        cardLayout.show(cardPanel, "lobby");
        frame.setTitle("Jungle Attack");
        lobbyPanel.resetForNewMatch();
        lobbyPanel.setStatus(reason == null || reason.isBlank()
                ? "Se perdio la conexion con el servidor."
                : reason, true);
    }

    // Partida creada.
    @Override
    public void onMatchCreated(String code, String newPlayerId, String newPlayerName) {
        matchCode = safe(code);
        playerId = safe(newPlayerId);
        inGame = false;

        lobbyPanel.setBusy(false);
        lobbyPanel.showWaiting(matchCode);
        lobbyPanel.setStatus("Partida " + matchCode + " creada. Esperando al rival...", false);
    }

    // Partida unida.
    @Override
    public void onMatchJoined(String code,
                              String newPlayerId,
                              String newPlayerName,
                              int playerNumber,
                              String opponentName,
                              List<String> availableSpecies) {
        matchCode = safe(code);
        playerId = safe(newPlayerId);
        inGame = false;

        lobbyPanel.setBusy(false);
        lobbyPanel.showSelection(matchCode, availableSpecies, "", "");
        lobbyPanel.setStatus("Rival: " + safe(opponentName)
                + " (jugador " + playerNumber + "). Elige tu especie.", false);
    }

    // Seleccion de animales.
    @Override
    public void onAnimalSelection(String yourSpecies,
                                  String opponentSpecies,
                                  List<String> availableSpecies) {
        lobbyPanel.setBusy(false);
        lobbyPanel.showSelection(matchCode, availableSpecies, yourSpecies, opponentSpecies);

        if (yourSpecies != null && !yourSpecies.isBlank()) {
            lobbyPanel.setStatus("Esperando a que el rival elija su especie...", false);
        } else {
            lobbyPanel.setStatus("Elige una especie disponible.", false);
        }
    }

    // Partida iniciada.
    @Override
    public void onGameStarted(GameState gameState) {
        if (gameState == null) {
            return;
        }

        matchCode = safe(gameState.getMatchCode());
        gameOverHandled = false;
        inGame = true;
        currentState = gameState;

        inputController.reset();
        inputController.setGameState(gameState);
        mapPanel.update(gameState);
        hudPanel.update(gameState);

        cardLayout.show(cardPanel, "game");
        frame.setTitle("Jungle Attack - " + safe(gameState.getPlayerName())
                + " vs " + safe(gameState.getOpponentName()));
        hudPanel.setMessage("Partida iniciada: clic en el mapa para mover, clic en un enemigo para atacar.");

        gameLoop.start();
    }

    // Partida terminada.
    @Override
    public void onGameOver(MatchResult result) {
        gameLoop.stop();
        gameOverHandled = true;

        if (result == null) {
            returnToLobby();
            return;
        }

        boolean winner = result.isWinner(playerId);
        String title = winner ? "Victoria" : "Derrota";
        String message = (winner ? "Ganaste la partida." : "Perdiste la partida.")
                + "\n\n" + result.describe();

        Object[] options = {"Volver al lobby", "Salir"};
        int choice = JOptionPane.showOptionDialog(
                frame,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 1) {
            networkClient.disconnect();
            frame.dispose();
            System.exit(0);
        }

        returnToLobby();
    }

    // Error.
    @Override
    public void onError(String message) {
        if (gameOverHandled) {
            return;
        }

        if (inGame) {
            hudPanel.setError(message);
        } else {
            lobbyPanel.allowSpeciesRetry();
            lobbyPanel.setStatus(message, true);
        }
    }

    // Cafe recogido.
    @Override
    public void onCoffeeCollected(String cupId, int damageCapacity) {
        if (inGame) {
            hudPanel.setMessage("Cafe recogido. Dano actual del pack: " + damageCapacity);
        }
    }

    // Inicia la conexion.
    private void startConnectionAttempt(String host, int port, Runnable action) {
        if (networkClient.isConnected()) {
            lobbyPanel.setStatus("Enviando solicitud...", false);
            action.run();
            return;
        }

        pendingConnectAction = action;
        lobbyPanel.setStatus("Conectando con " + host + ":" + port + "...", false);
        networkClient.connectToServer(host, port);
    }

    // Vuelve al lobby.
    private void returnToLobby() {
        gameLoop.stop();
        networkClient.disconnect();

        currentState = null;
        gameOverHandled = false;
        inGame = false;
        playerId = "";
        matchCode = "";

        inputController.reset();
        mapPanel.update((GameState) null);
        hudPanel.update((GameState) null);
        lobbyPanel.resetForNewMatch();

        cardLayout.show(cardPanel, "lobby");
        frame.setTitle("Jungle Attack");
    }

    // Valor seguro.
    private static String safe(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}