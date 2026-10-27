// Paquete del cliente: contiene el punto de entrada.
package client;

// Importa NetworkClient para la red.
import control.NetworkClient;
// Importa SwingUtilities para el EDT.
import javax.swing.SwingUtilities;
// Importa UIManager para el look and feel.
import javax.swing.UIManager;
// Importa MainWindow para la ventana.
import ui.MainWindow;

/**
 * Punto de entrada del cliente.
 */
// Clase final de arranque del cliente.
public final class ClientMain {

    // Constructor privado.
    private ClientMain() {
        // Clase de arranque.
    }

    // Metodo principal.
    public static void main(String[] args) {
        // Construye la UI en el EDT.
        SwingUtilities.invokeLater(() -> {
            // Instala el look and feel.
            installLookAndFeel();

            // Crea el cliente de red.
            NetworkClient networkClient = new NetworkClient();
            // Crea la ventana principal.
            MainWindow mainWindow = new MainWindow(networkClient);
            // Muestra la ventana.
            mainWindow.showWindow();
        });
    }

    // Instala el look and feel del sistema.
    private static void installLookAndFeel() {
        try {
            // Usa el look and feel del sistema.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si no esta disponible, queda el de Java.
        }
    }
}