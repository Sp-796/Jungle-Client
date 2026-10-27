package ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class SpriteLoaderTest {

    @Test
    void cargaTodasLasImagenesDelJuego() {
        SpriteLoader loader = new SpriteLoader();

        List<String> zones = List.of(
                "paramo", "bosquenuboso", "zonamontana", "bosquerodeado",
                "bosquehumedo", "bosquecatarata", "bosquecaverna", "senderorocoso",
                "manglar", "bosqueseco", "bosqueprofundo", "bosquedespejado"
        );
        List<String> animals = List.of(
                "perezoso", "mono", "tucan", "ranaroja", "jaguar", "danta", "boaconstructora"
        );

        for (String zone : zones) {
            assertNotNull(loader.zone(zone + ".svg"), "Falta la imagen de la zona " + zone);
        }
        for (String animal : animals) {
            assertNotNull(loader.animal(animal), "Falta la imagen del animal " + animal);
        }
        assertNotNull(loader.coffee(), "Falta la imagen del cafe");
        assertNull(loader.load("no-existe"));
        assertNull(loader.load(null));
    }
}
