// Paquete comun de mensajes: contiene el sobre de todos los mensajes del protocolo.
package common.message;

// Importa Gson para serializar y deserializar JSON.
import com.google.gson.Gson;
// Importa JsonArray para manejar listas en el payload.
import com.google.gson.JsonArray;
// Importa JsonElement para leer campos del payload.
import com.google.gson.JsonElement;
// Importa JsonObject para el payload generico.
import com.google.gson.JsonObject;

/**
 * Sobre (envelope) de todos los mensajes del protocolo.
 */
// Clase que representa un mensaje del protocolo con tipo y payload.
public class Message {

    // Gson compartido.
    private static final Gson GSON = new Gson();

    // Tipo del mensaje.
    private String type;
    // Payload generico.
    private JsonObject payload;

    // Constructor vacio requerido por Gson.
    public Message() {
        // Inicializa el payload vacio.
        this.payload = new JsonObject();
    }

    // Constructor con solo el tipo.
    public Message(String type) {
        // Delega al constructor completo.
        this(type, new JsonObject());
    }

    // Constructor con tipo y payload.
    public Message(String type, JsonObject payload) {
        // Asigna el tipo.
        this.type = type;
        // Asigna el payload.
        this.payload = payload == null ? new JsonObject() : payload;
    }

    // Getter del tipo.
    public String getType() {
        return type;
    }

    // Setter del tipo.
    public void setType(String type) {
        this.type = type;
    }

    // Getter del payload.
    public JsonObject getPayload() {
        if (payload == null) {
            payload = new JsonObject();
        }
        return payload;
    }

    // Setter del payload.
    public void setPayload(JsonObject payload) {
        this.payload = payload;
    }

    // Agrega un campo al payload.
    public Message put(String key, Object value) {
        getPayload().add(key, GSON.toJsonTree(value));
        return this;
    }

    // Lee un String del payload.
    public String getString(String key) {
        JsonElement element = getPayload().get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    // Lee un int del payload.
    public int getInt(String key, int defaultValue) {
        JsonElement element = getPayload().get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        return element.getAsInt();
    }

    // Lee un double del payload.
    public double getDouble(String key, double defaultValue) {
        JsonElement element = getPayload().get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        return element.getAsDouble();
    }

    // Lee un array del payload.
    public JsonArray getArray(String key) {
        JsonElement element = getPayload().get(key);
        if (element == null || !element.isJsonArray()) {
            return new JsonArray();
        }
        return element.getAsJsonArray();
    }

    // Serializa a JSON.
    public String toJson() {
        return GSON.toJson(this);
    }

    // Deserializa desde JSON.
    public static Message fromJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Mensaje vacio");
        }
        Message message = GSON.fromJson(json, Message.class);
        if (message == null || message.getType() == null || message.getType().isBlank()) {
            throw new IllegalArgumentException("El mensaje no tiene campo 'type'");
        }
        return message;
    }

    // Representacion textual.
    @Override
    public String toString() {
        return "Message{" + type + ", " + getPayload() + "}";
    }
}