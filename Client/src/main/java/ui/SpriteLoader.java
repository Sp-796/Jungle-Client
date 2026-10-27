// Paquete de la UI: contiene el cargador de sprites.
package ui;

// Importa las constantes del cliente.
import constants.IConstants;
// Importa Image para las imagenes.
import java.awt.Image;
// Importa IOException para errores de IO.
import java.io.IOException;
// Importa InputStream para leer recursos.
import java.io.InputStream;
// Importa HashMap para el cache.
import java.util.HashMap;
// Importa HashSet para los faltantes.
import java.util.HashSet;
// Importa Map para el cache.
import java.util.Map;
// Importa Set para los faltantes.
import java.util.Set;
// Importa ImageIO para leer imagenes.
import javax.imageio.ImageIO;

/**
 * Carga y cachea las imagenes del juego.
 */
// Clase final que carga y cachea las imagenes.
public final class SpriteLoader {

    // Cache de imagenes.
    private final Map<String, Image> cache = new HashMap<>();
    // Conjunto de imagenes faltantes.
    private final Set<String> missing = new HashSet<>();

    // Carga un recurso por nombre.
    public Image load(String assetName) {
        // Si es nulo o esta en blanco, retorna null.
        if (assetName == null || assetName.isBlank()) {
            return null;
        }

        // Nombre base sin extension.
        String baseName = assetName;
        int dot = baseName.lastIndexOf('.');
        if (dot > 0) {
            baseName = baseName.substring(0, dot);
        }

        // Si esta en cache, retorna.
        if (cache.containsKey(baseName)) {
            return cache.get(baseName);
        }
        // Si esta en faltantes, retorna null.
        if (missing.contains(baseName)) {
            return null;
        }

        // Ruta del recurso.
        String path = IConstants.assetsPath + baseName + ".png";

        // Intenta cargar el recurso.
        try (InputStream input = SpriteLoader.class.getResourceAsStream(path)) {
            if (input == null) {
                missing.add(baseName);
                return null;
            }

            Image image = ImageIO.read(input);
            if (image == null) {
                missing.add(baseName);
                return null;
            }

            cache.put(baseName, image);
            return image;
        } catch (IOException error) {
            missing.add(baseName);
            return null;
        }
    }

    // Carga una zona.
    public Image zone(String imageName) {
        return load(imageName);
    }

    // Carga un animal.
    public Image animal(String species) {
        return load(species);
    }

    // Carga el cafe.
    public Image coffee() {
        return load("cafe");
    }
}