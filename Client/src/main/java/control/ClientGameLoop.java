// Paquete de control del cliente: contiene el bucle local de animacion.
package control;

// Importa Animal para animar.
import client.model.Animal;
// Importa GameState para el estado.
import client.model.GameState;
// Importa las constantes del cliente.
import constants.IConstants;
// Importa LinkedHashSet para conjuntos ordenados.
import java.util.LinkedHashSet;
// Importa Set para conjuntos.
import java.util.Set;
// Importa Supplier para obtener el estado.
import java.util.function.Supplier;
// Importa Timer para el bucle.
import javax.swing.Timer;

/**
 * Bucle local de animacion.
 */
// Clase que implementa el bucle de animacion del cliente.
public class ClientGameLoop {

    // Proveedor del estado.
    private final Supplier<GameState> stateSupplier;
    // Accion a ejecutar por frame.
    private final Runnable onFrame;
    // Frames por segundo.
    private final int fps = Math.max(1, 1000 / IConstants.refreshRate);

    // Timer de Swing.
    private Timer timer;
    // Indica si esta corriendo.
    private boolean running;
    // Momento del ultimo tick.
    private long lastNanos;

    // Constructor.
    public ClientGameLoop(Supplier<GameState> stateSupplier, Runnable onFrame) {
        // Asigna el proveedor.
        this.stateSupplier = stateSupplier;
        // Asigna la accion o una vacia.
        this.onFrame = onFrame == null ? () -> {
        } : onFrame;
    }

    // Inicia el bucle.
    public void start() {
        // Si ya esta corriendo, no hace nada.
        if (running) {
            return;
        }

        // Marca como corriendo.
        running = true;
        // Inicializa el ultimo tick.
        lastNanos = System.nanoTime();
        // Crea el timer.
        timer = new Timer(1000 / fps, event -> tick());
        // Inicia el timer.
        timer.start();
    }

    // Detiene el bucle.
    public void stop() {
        // Marca como no corriendo.
        running = false;
        // Detiene el timer si existe.
        if (timer != null) {
            timer.stop();
        }
    }

    // Indica si esta corriendo.
    public boolean isRunning() {
        return running;
    }

    // Ejecuta un tick.
    private void tick() {
        // Tiempo actual.
        long now = System.nanoTime();
        // Delta en ms.
        int deltaMs = (int) Math.min(100L, Math.max(1L, (now - lastNanos) / 1_000_000L));
        // Actualiza el ultimo tick.
        lastNanos = now;

        // Obtiene el estado.
        GameState state = stateSupplier.get();
        // Si existe, actualiza animaciones.
        if (state != null) {
            updateAnimations(deltaMs);
        }

        // Ejecuta la accion por frame.
        onFrame.run();
    }

    // Interpola todos los animales visibles.
    public void updateAnimations(int deltaTime) {
        // Obtiene el estado.
        GameState state = stateSupplier.get();
        // Si es nulo, no hace nada.
        if (state == null) {
            return;
        }

        // Conjunto de animales (sin duplicados).
        Set<Animal> animals = new LinkedHashSet<>();
        // Agrega los visibles.
        animals.addAll(state.getVisibleAnimals());
        // Agrega los propios.
        animals.addAll(state.getMyAnimals());

        // Anima cada animal.
        for (Animal animal : animals) {
            animal.updateLocalPosition(deltaTime);
        }
    }
}