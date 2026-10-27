package common.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void serializaYReconstruyeElSobreCompleto() {
        JsonObject payload = new JsonObject();
        payload.addProperty("animalIds", List.of(3, 4).toString());
        payload.addProperty("targetX", 812.5);
        payload.addProperty("targetY", 344);

        Message original = new Message(MessageType.MOVE_ANIMALS, payload);
        String json = original.toJson();

        Message rebuilt = Message.fromJson(json);

        assertEquals(MessageType.MOVE_ANIMALS, rebuilt.getType());
        assertEquals(812.5, rebuilt.getDouble("targetX", 0), 1e-9);
        assertEquals(344, rebuilt.getInt("targetY", 0));
        assertTrue(rebuilt.getPayload().has("animalIds"));
    }

    @Test
    void construyePayloadConPutYDevuelveValoresPorDefecto() {
        Message message = new Message(MessageType.ATTACK)
                .put("targetAnimalId", 9)
                .put("animalIds", new int[]{1, 2});

        assertEquals(9, message.getInt("targetAnimalId", -1));
        assertEquals(-1, message.getInt("noExiste", -1));
        assertEquals("sin-valor", message.getString("noExiste") == null ? "sin-valor" : "otro");
        assertEquals(2, message.getArray("animalIds").size());
    }

    @Test
    void rechazaMensajesInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> Message.fromJson(""));
        assertThrows(IllegalArgumentException.class, () -> Message.fromJson("   "));
        assertThrows(IllegalArgumentException.class, () -> Message.fromJson("{\"payload\":{}}"));
    }

    @Test
    void toleraPayloadAusenteAlLeer() {
        Message message = Message.fromJson("{\"type\":\"STATE_UPDATE\"}");

        assertEquals(MessageType.STATE_UPDATE, message.getType());
        assertTrue(message.getPayload().isJsonObject());
        assertFalse(message.getPayload().has("animals"));
        assertEquals(0, message.getArray("animals").size());
    }
}
