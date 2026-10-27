package client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MatchResultTest {

    @Test
    void identificaAlGanadorPorId() {
        MatchResult result = new MatchResult("ABC123", "FLAG_TOUCHED", 95, "p1", "Ana",
                List.of(new MatchResult.PlayerResult("p1", "Ana", "jaguar", 4, 1, 2, 3, 5)));

        assertTrue(result.isWinner("p1"));
        assertFalse(result.isWinner("p2"));
        assertFalse(result.isWinner(null));
    }

    @Test
    void traduceLosMotivosDelServidor() {
        assertEquals("Se toco la bandera del rival", MatchResult.translateReason("FLAG_TOUCHED"));
        assertEquals("Una manada se quedo sin animales", MatchResult.translateReason("PACK_DEFEATED"));
        assertEquals("Un jugador se desconecto", MatchResult.translateReason("DISCONNECT"));
        assertEquals("El rival abandono la partida", MatchResult.translateReason("OPPONENT_LEFT"));
        assertEquals("OTRO", MatchResult.translateReason("OTRO"));
        assertEquals("Desconocido", MatchResult.translateReason(null));
    }

    @Test
    void describeElResumenConEstadisticas() {
        MatchResult result = new MatchResult("ABC123", "PACK_DEFEATED", 60, "p2", "Beto",
                List.of(new MatchResult.PlayerResult("p2", "Beto", "mono", 3, 2, 1, 4, 6)));

        String text = result.describe();

        assertTrue(text.contains("ABC123"));
        assertTrue(text.contains("Beto"));
        assertTrue(text.contains("mono"));
        assertTrue(text.contains("Ganador: Beto"));
        assertTrue(text.contains("Vivos: 3"));
    }
}
