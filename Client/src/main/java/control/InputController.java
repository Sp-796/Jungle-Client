// Paquete de control del cliente: contiene el controlador de entrada del mouse.
package control;

// Importa Animal para la seleccion.
import client.model.Animal;
// Importa GameState para el estado.
import client.model.GameState;
// Importa Point2D para coordenadas.
import common.model.Point2D;
// Importa MapPanel para la conversion de coordenadas.
import ui.MapPanel;
// Importa MouseAdapter para eventos del mouse.
import java.awt.event.MouseAdapter;
// Importa MouseEvent para eventos del mouse.
import java.awt.event.MouseEvent;
// Importa ArrayList para listas.
import java.util.ArrayList;
// Importa Collection para colecciones.
import java.util.Collection;
// Importa Collections para listas inmutables.
import java.util.Collections;
// Importa LinkedHashSet para conjuntos ordenados.
import java.util.LinkedHashSet;
// Importa List para listas.
import java.util.List;
// Importa Set para conjuntos.
import java.util.Set;
// Importa Consumer para mensajes.
import java.util.function.Consumer;
// Importa SwingUtilities para verificar el boton.
import javax.swing.SwingUtilities;

/**
 * Convierte los clics del mapa en acciones del protocolo.
 */
// Clase que extiende MouseAdapter y maneja los clics del mapa.
public class InputController extends MouseAdapter {

    // Cliente de red para enviar intenciones.
    public final NetworkClient networkClient;
    // Panel del mapa para conversion de coordenadas.
    private final MapPanel mapPanel;
    // Conjunto de ids de animales seleccionados.
    private final Set<Integer> selectedAnimalIds = new LinkedHashSet<>();

    // Estado del juego.
    private GameState gameState;
    // Indica si ya se inicializo la seleccion.
    private boolean selectionInitialized;
    // Sink de mensajes para la UI.
    private Consumer<String> messageSink = message -> {
    };

    // Constructor.
    public InputController(NetworkClient networkClient, MapPanel mapPanel) {
        // Asigna el cliente de red.
        this.networkClient = networkClient;
        // Asigna el panel del mapa.
        this.mapPanel = mapPanel;
    }

    // Asigna el sink de mensajes.
    public void setMessageSink(Consumer<String> messageSink) {
        // Asigna el sink o uno vacio.
        this.messageSink = messageSink == null ? message -> {
        } : messageSink;
    }

    // Se llama en cada actualizacion del servidor.
    public void setGameState(GameState gameState) {
        // Asigna el estado.
        this.gameState = gameState;

        // Si no se inicializo, selecciona todos.
        if (!selectionInitialized) {
            selectAll();
            selectionInitialized = true;
        } else {
            // Filtra los seleccionados que siguen vivos.
            Set<Integer> alive = aliveOwnAnimalIds();
            selectedAnimalIds.removeIf(id -> !alive.contains(id));
            updateSelectionHighlight();
        }
    }

    // Prepara el controlador para una partida nueva.
    public void reset() {
        // Limpia la seleccion.
        selectedAnimalIds.clear();
        // Reinicia la inicializacion.
        selectionInitialized = false;
        // Limpia el estado.
        gameState = null;
        // Actualiza el resaltado.
        updateSelectionHighlight();
    }

    // Maneja el clic del mouse.
    @Override
    public void mouseClicked(MouseEvent event) {
        // Si es boton derecho, no hace nada.
        if (SwingUtilities.isRightMouseButton(event)) {
            return;
        }
        // Si no es boton izquierdo, no hace nada.
        if (event.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        // Procesa el clic.
        onMouseClick(event.getX(), event.getY());
    }

    // Procesa un clic en coordenadas de pantalla.
    public void onMouseClick(int screenX, int screenY) {
        // Convierte a coordenadas del mundo.
        Point2D worldPoint = mapPanel.toWorld(screenX, screenY);
        // Si esta fuera del mapa, no hace nada.
        if (worldPoint == null) {
            return;
        }

        // Animal en el punto del clic.
        Animal clicked = mapPanel.getAnimalAt(worldPoint.getX(), worldPoint.getY());

        // Si es un animal propio, alterna la seleccion.
        if (clicked != null && clicked.isMine()) {
            toggleSelection(clicked.getId());
            return;
        }

        // Si es un animal enemigo vivo, ordena atacar.
        if (clicked != null && clicked.isAlive()) {
            handleAttackIntent(new ArrayList<>(selectedAnimalIds), clicked.getId());
            return;
        }

        // Si es terreno, ordena mover.
        handleMoveIntent(new ArrayList<>(selectedAnimalIds), worldPoint.getX(), worldPoint.getY());
    }

    // Maneja la intencion de mover.
    private void handleMoveIntent(List<Integer> selectedAnimalIds, double targetX, double targetY) {
        // Si no hay seleccionados, avisa.
        if (selectedAnimalIds == null || selectedAnimalIds.isEmpty()) {
            messageSink.accept("Selecciona al menos un animal (clic sobre tu animal).");
            return;
        }

        // Envia el movimiento al servidor.
        networkClient.sendMoveAnimals(new ArrayList<>(selectedAnimalIds), targetX, targetY);
    }

    // Maneja la intencion de atacar.
    private void handleAttackIntent(List<Integer> selectedAnimalIds, int targetAnimalId) {
        // Si no hay seleccionados, avisa.
        if (selectedAnimalIds == null || selectedAnimalIds.isEmpty()) {
            messageSink.accept("Selecciona al menos un animal para atacar.");
            return;
        }

        // Envia el ataque al servidor.
        networkClient.sendAttack(new ArrayList<>(selectedAnimalIds), targetAnimalId);
    }

    // Solicita al servidor cambiar la zona vista.
    public void handleZoneViewIntent(String zoneId) {
        // Si es valido, envia la peticion.
        if (zoneId != null && !zoneId.isBlank()) {
            networkClient.sendViewZone(zoneId);
        }
    }

    // Selecciona todos los animales vivos propios.
    public void selectAll() {
        // Limpia la seleccion.
        selectedAnimalIds.clear();
        // Agrega todos los vivos.
        selectedAnimalIds.addAll(aliveOwnAnimalIds());
        // Actualiza el resaltado.
        updateSelectionHighlight();
    }

    // Limpia la seleccion.
    public void clearSelection() {
        // Limpia la seleccion.
        selectedAnimalIds.clear();
        // Actualiza el resaltado.
        updateSelectionHighlight();
    }

    // Retorna los ids seleccionados.
    public List<Integer> getSelectedAnimalIds() {
        // Retorna copia inmutable.
        return Collections.unmodifiableList(new ArrayList<>(selectedAnimalIds));
    }

    // Retorna los ids de animales propios vivos.
    private Set<Integer> aliveOwnAnimalIds() {
        // Conjunto de ids.
        Set<Integer> ids = new LinkedHashSet<>();
        // Si no hay estado, retorna vacio.
        if (gameState == null) {
            return ids;
        }
        // Recorre los animales propios.
        for (Animal animal : gameState.getMyAnimals()) {
            // Si esta vivo, agrega el id.
            if (animal.isAlive()) {
                ids.add(animal.getId());
            }
        }
        // Retorna los ids.
        return ids;
    }

    // Alterna la seleccion de un animal.
    private void toggleSelection(int animalId) {
        // Si no se pudo remover, lo agrega.
        if (!selectedAnimalIds.remove(animalId)) {
            selectedAnimalIds.add(animalId);
        }
        // Actualiza el resaltado.
        updateSelectionHighlight();
    }

    // Actualiza el resaltado en el panel.
    private void updateSelectionHighlight() {
        // Copia de los seleccionados.
        Collection<Integer> copy = new ArrayList<>(selectedAnimalIds);
        // Asigna al panel.
        mapPanel.setSelectedAnimalIds(copy);
    }
}