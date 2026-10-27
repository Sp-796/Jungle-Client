// Paquete de control del cliente: contiene el adaptador JSON.
package control;

// Importa Animal para parsear.
import client.model.Animal;
// Importa CoffeeCup para parsear.
import client.model.CoffeeCup;
// Importa GameState para el estado.
import client.model.GameState;
// Importa MatchResult para el resumen final.
import client.model.MatchResult;
// Importa Path para los caminos.
import client.model.Path;
// Importa Zone para las zonas.
import client.model.Zone;
// Importa Gson para JSON.
import com.google.gson.Gson;
// Importa JsonArray para arrays.
import com.google.gson.JsonArray;
// Importa JsonElement para elementos.
import com.google.gson.JsonElement;
// Importa JsonObject para objetos.
import com.google.gson.JsonObject;
// Importa Message para el protocolo.
import common.message.Message;
// Importa MessageType para los tipos.
import common.message.MessageType;
// Importa ArrayList para listas.
import java.util.ArrayList;
// Importa HashMap para mapas.
import java.util.HashMap;
// Importa LinkedHashMap para mapas ordenados.
import java.util.LinkedHashMap;
// Importa List para listas.
import java.util.List;
import java.util.Map;
// Importa Map para mapas.

/**
 * Patron Adapter.
 *
 * Traduce entre el JSON del protocolo y los objetos del cliente.
 */
// Clase que adapta JSON a objetos del cliente.
public class JsonAdapter {

    // Gson compartido.
    private static final Gson GSON = new Gson();

    // Mapa de zonas.
    private final Map<String, Zone> zones = new LinkedHashMap<>();
    // Mapa de animales por id.
    private final Map<Integer, Animal> animalsById = new HashMap<>();

    // Codigo de la partida.
    private String matchCode = "";
    // Nombre del jugador.
    private String playerName = "";
    // Nombre del oponente.
    private String opponentName = "";
    // Especie propia.
    private String mySpecies = "";

    // Convierte una accion a JSON.
    public String adaptActionToJson(String actionType, Object payload) {
        // Payload JSON.
        JsonObject jsonPayload;
        // Si es nulo, payload vacio.
        if (payload == null) {
            jsonPayload = new JsonObject();
        // Si es JsonObject, lo usa.
        } else if (payload instanceof JsonObject object) {
            jsonPayload = object;
        // Si no, lo convierte.
        } else {
            JsonElement tree = GSON.toJsonTree(payload);
            jsonPayload = tree.isJsonObject() ? tree.getAsJsonObject() : new JsonObject();
        }

        // Retorna el mensaje serializado.
        return new Message(actionType, jsonPayload).toJson();
    }

    // Convierte un mensaje completo a GameState.
    public GameState adaptJsonToGameState(String jsonString) {
        // Deserializa el mensaje.
        Message message = Message.fromJson(jsonString);

        // Retorna segun el tipo.
        return switch (message.getType()) {
            case MessageType.GAME_STARTED -> adaptGameStarted(message.getPayload());
            case MessageType.STATE_UPDATE -> adaptStateUpdate(message.getPayload());
            default -> null;
        };
    }

    // Prepara el adaptador para una partida nueva.
    public void reset() {
        // Limpia las zonas.
        zones.clear();
        // Limpia los animales.
        animalsById.clear();
        // Reinicia el codigo.
        matchCode = "";
        // Reinicia el nombre.
        playerName = "";
        // Reinicia el oponente.
        opponentName = "";
        // Reinicia la especie.
        mySpecies = "";
    }

    // Adapta GAME_STARTED.
    public GameState adaptGameStarted(JsonObject payload) {
        // Reinicia el adaptador.
        reset();

        // Lee el codigo.
        matchCode = string(payload, "matchCode", "");
        // Lee el nombre.
        playerName = string(payload, "playerName", "");
        // Lee el oponente.
        opponentName = string(payload, "opponentName", "");

        // Lee el pack.
        JsonObject pack = object(payload, "pack");
        // Lee la especie.
        mySpecies = string(payload, "species", "");
        if (mySpecies.isBlank() && pack != null) {
            mySpecies = string(pack, "species", "");
        }

        // Crea todas las zonas.
        JsonArray zoneArray = array(payload, "zones");
        if (zoneArray != null) {
            for (JsonElement element : zoneArray) {
                if (element != null && element.isJsonObject()) {
                    parseZone(element.getAsJsonObject());
                }
            }
        }

        // Crea los caminos.
        if (zoneArray != null) {
            for (JsonElement element : zoneArray) {
                if (element == null || !element.isJsonObject()) {
                    continue;
                }
                JsonObject zoneJson = element.getAsJsonObject();
                Zone from = zones.get(string(zoneJson, "id", null));
                JsonArray exits = array(zoneJson, "exits");

                if (from == null || exits == null) {
                    continue;
                }

                for (JsonElement exit : exits) {
                    if (exit == null || exit.isJsonNull()) {
                        continue;
                    }
                    Zone to = zones.get(exit.getAsString());
                    if (to != null) {
                        from.addPath(new Path(from.getZoneId(), to.getZoneId(),
                                from.getCenter(), to.getCenter()));
                    }
                }
            }
        }

        // Crea el estado.
        GameState state = new GameState();
        state.setMatchCode(matchCode);
        state.setPlayerName(playerName);
        state.setOpponentName(opponentName);
        state.setMySpecies(mySpecies);
        state.setMapZones(new LinkedHashMap<>(zones));
        state.setAvailableZones(new ArrayList<>());
        state.setCurrentZoneId(string(payload, "yourZoneId", null));
        state.setMatchDuration(0);
        state.setGameOver(false);
        state.setPlayerWon(false);

        // Zona inicial.
        Zone startZone = state.getZone(state.getCurrentZoneId());
        if (startZone != null) {
            startZone.setVisible(true);
            state.setViewedZone(startZone);
            state.getAvailableZones().add(startZone.getZoneId());
        }

        // Pack inicial.
        List<Animal> mine = new ArrayList<>();
        if (pack != null) {
            int alive = integer(pack, "alive", 0);
            int total = integer(pack, "total", 0);

            state.setAliveAnimalsPlayer(alive);
            state.setDeadAnimalsPlayer(Math.max(0, total - alive));
            state.setDamageCapacity(integer(pack, "damageCapacity", 0));

            JsonArray packAnimals = array(pack, "animals");
            if (packAnimals != null) {
                for (JsonElement element : packAnimals) {
                    if (element == null || !element.isJsonObject()) {
                        continue;
                    }
                    Animal animal = parseAnimal(element.getAsJsonObject(), mySpecies);
                    if (animal != null) {
                        animal.setOwner("ME");
                        mine.add(animal);
                    }
                }
            }
        }

        // El pack esta en la zona inicial.
        for (Animal animal : mine) {
            Zone zone = zones.get(animal.getZoneId());
            animal.setCurrentZone(zone);

            if (zone != null && zone.equals(startZone)) {
                state.getVisibleAnimals().add(animal);
                zone.addPresentAnimal(animal);
            }
        }

        state.setMyAnimals(mine);
        return state;
    }

    // Adapta STATE_UPDATE.
    public GameState adaptStateUpdate(JsonObject payload) {
        // Crea el estado.
        GameState state = new GameState();

        // Lee datos basicos.
        matchCode = string(payload, "matchCode", matchCode);
        playerName = string(payload, "playerName", playerName);
        opponentName = string(payload, "opponentName", opponentName);

        // Asigna al estado.
        state.setMatchCode(matchCode);
        state.setPlayerName(playerName);
        state.setOpponentName(opponentName);
        state.setMySpecies(mySpecies);
        state.setMapZones(new LinkedHashMap<>(zones));
        state.setMatchDuration(integer(payload, "matchDuration", 0));
        state.setGameOver(bool(payload, "gameOver", false));
        state.setPlayerWon(bool(payload, "playerWon", false));

        // Lee el pack.
        JsonObject myPack = object(payload, "myPack");
        if (myPack != null) {
            String packSpecies = string(myPack, "species", "");
            if (!packSpecies.isBlank()) {
                mySpecies = packSpecies;
                state.setMySpecies(mySpecies);
            }
        }

        // Limpia el contenido de todas las zonas.
        for (Zone zone : zones.values()) {
            zone.clearPresentAnimals();
            zone.clearCoffeeCups();
            zone.setVisible(false);
            zone.clearFlag();
        }

        // Zonas disponibles.
        state.setAvailableZones(strings(array(payload, "availableZones")));

        // Zona vista.
        JsonObject viewedJson = object(payload, "viewedZone");
        if (viewedJson != null) {
            String zoneId = string(viewedJson, "id", null);
            Zone viewed = zones.get(zoneId);

            if (viewed == null && zoneId != null) {
                viewed = parseZone(viewedJson);
            }

            if (viewed != null) {
                viewed.setVisible(true);
                state.setViewedZone(viewed);
                state.setCurrentZoneId(viewed.getZoneId());

                if (bool(viewedJson, "hasFlag", false)) {
                    viewed.setFlag(
                            string(viewedJson, "flagOwnerId", null),
                            string(viewedJson, "flagColor", ""),
                            number(viewedJson, "flagX", 0),
                            number(viewedJson, "flagY", 0)
                    );
                }

                // Animales visibles.
                JsonArray animalsJson = array(payload, "animals");
                if (animalsJson != null) {
                    for (JsonElement element : animalsJson) {
                        if (element == null || !element.isJsonObject()) {
                            continue;
                        }
                        Animal animal = parseAnimal(element.getAsJsonObject(), mySpecies);
                        if (animal != null) {
                            animal.setCurrentZone(viewed);
                            state.getVisibleAnimals().add(animal);
                            viewed.addPresentAnimal(animal);
                        }
                    }
                }

                // Tazas visibles.
                JsonArray cupsJson = array(payload, "coffeeCups");
                if (cupsJson != null) {
                    for (JsonElement element : cupsJson) {
                        if (element == null || !element.isJsonObject()) {
                            continue;
                        }
                        JsonObject cupJson = element.getAsJsonObject();
                        CoffeeCup cup = new CoffeeCup(
                                string(cupJson, "id", ""),
                                number(cupJson, "x", 0),
                                number(cupJson, "y", 0)
                        );
                        state.getVisibleCoffeeCups().add(cup);
                        viewed.addCoffeeCup(cup);
                    }
                }
            }
        }

        // Animales propios.
        List<Animal> mine = new ArrayList<>();
        JsonArray mineJson = array(payload, "myAnimals");
        if (mineJson != null) {
            for (JsonElement element : mineJson) {
                if (element == null || !element.isJsonObject()) {
                    continue;
                }
                Animal animal = parseAnimal(element.getAsJsonObject(), mySpecies);
                if (animal != null) {
                    animal.setOwner("ME");
                    Zone zone = zones.get(animal.getZoneId());
                    animal.setCurrentZone(zone);
                    mine.add(animal);
                }
            }
        }

        if (mine.isEmpty()) {
            for (Animal visible : state.getVisibleAnimals()) {
                if (visible.isMine()) {
                    mine.add(visible);
                }
            }
        }
        state.setMyAnimals(mine);

        // Estadisticas del pack.
        if (myPack != null) {
            state.setAliveAnimalsPlayer(integer(myPack, "alive", 0));
            state.setDeadAnimalsPlayer(integer(myPack, "lost", 0));
            state.setBotsKilled(integer(myPack, "botsKilled", 0));
            state.setCoffeeCupsCollected(integer(myPack, "coffeeCups", 0));
            state.setDamageCapacity(integer(myPack, "damageCapacity", 0));
        } else {
            int alive = 0;
            for (Animal animal : mine) {
                if (animal.isAlive()) {
                    alive++;
                }
            }
            state.setAliveAnimalsPlayer(alive);
            state.setDeadAnimalsPlayer(mine.size() - alive);
        }

        return state;
    }

    // Adapta GAME_OVER.
    public MatchResult adaptGameOver(JsonObject payload) {
        // Lista de jugadores.
        List<MatchResult.PlayerResult> players = new ArrayList<>();
        // Array de jugadores.
        JsonArray playersJson = array(payload, "players");

        if (playersJson != null) {
            for (JsonElement element : playersJson) {
                if (element == null || !element.isJsonObject()) {
                    continue;
                }
                JsonObject playerJson = element.getAsJsonObject();
                players.add(new MatchResult.PlayerResult(
                        string(playerJson, "playerId", ""),
                        string(playerJson, "name", ""),
                        string(playerJson, "species", ""),
                        integer(playerJson, "alive", 0),
                        integer(playerJson, "lost", 0),
                        integer(playerJson, "botsKilled", 0),
                        integer(playerJson, "coffeeCups", 0),
                        integer(playerJson, "damageCapacity", 0)
                ));
            }
        }

        // Retorna el MatchResult.
        return new MatchResult(
                string(payload, "matchCode", matchCode),
                string(payload, "reason", ""),
                integer(payload, "durationSeconds", 0),
                string(payload, "winnerId", ""),
                string(payload, "winnerName", ""),
                players
        );
    }

    // Parsea una zona.
    private Zone parseZone(JsonObject json) {
        // Lee el id.
        String zoneId = string(json, "id", null);
        if (zoneId == null) {
            return null;
        }

        // Busca la zona existente.
        Zone zone = zones.get(zoneId);
        if (zone == null) {
            // Crea la zona.
            zone = new Zone(
                    zoneId,
                    string(json, "name", zoneId),
                    string(json, "image", null),
                    number(json, "x", 0),
                    number(json, "y", 0),
                    number(json, "width", 0),
                    number(json, "height", 0),
                    bool(json, "hasFlag", false)
            );
            zones.put(zoneId, zone);
        } else {
            zone.setHasFlag(bool(json, "hasFlag", zone.hasFlag()));
        }

        return zone;
    }

    // Parsea un animal.
    private Animal parseAnimal(JsonObject json, String fallbackSpecies) {
        // Lee el id.
        int id = integer(json, "id", -1);
        if (id < 0) {
            return null;
        }

        // Lee los datos.
        String species = string(json, "species", fallbackSpecies);
        int energy = integer(json, "energy", 0);
        double x = number(json, "x", 0);
        double y = number(json, "y", 0);
        boolean alive = bool(json, "alive", energy > 0);
        String zoneId = string(json, "zoneId", null);
        String owner = string(json, "owner", "OPPONENT");
        double targetX = number(json, "targetX", x);
        double targetY = number(json, "targetY", y);
        boolean moving = bool(json, "moving", false);

        // Busca el animal existente.
        Animal animal = animalsById.get(id);
        if (animal == null) {
            // Crea el animal.
            animal = new Animal(id, species, energy, x, y);
            animalsById.put(id, animal);
            animal.setZoneId(zoneId);
        }

        // Aplica el estado del servidor.
        animal.applyServerState(species, energy, x, y, alive, zoneId, owner,
                targetX, targetY, moving);

        return animal;
    }

    // Utilidades JSON.
    private static JsonObject object(JsonObject parent, String key) {
        if (parent == null) {
            return null;
        }
        JsonElement element = parent.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static JsonArray array(JsonObject parent, String key) {
        if (parent == null) {
            return null;
        }
        JsonElement element = parent.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static String string(JsonObject object, String key, String defaultValue) {
        if (object == null) {
            return defaultValue;
        }
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        return element.getAsString();
    }

    private static int integer(JsonObject object, String key, int defaultValue) {
        if (object == null) {
            return defaultValue;
        }
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        try {
            return element.getAsInt();
        } catch (RuntimeException invalid) {
            return defaultValue;
        }
    }

    private static double number(JsonObject object, String key, double defaultValue) {
        if (object == null) {
            return defaultValue;
        }
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        try {
            return element.getAsDouble();
        } catch (RuntimeException invalid) {
            return defaultValue;
        }
    }

    private static boolean bool(JsonObject object, String key, boolean defaultValue) {
        if (object == null) {
            return defaultValue;
        }
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        try {
            return element.getAsBoolean();
        } catch (RuntimeException invalid) {
            return defaultValue;
        }
    }

    private static List<String> strings(JsonArray array) {
        List<String> values = new ArrayList<>();
        if (array == null) {
            return values;
        }
        for (JsonElement element : array) {
            if (element != null && !element.isJsonNull()) {
                values.add(element.getAsString());
            }
        }
        return values;
    }
}