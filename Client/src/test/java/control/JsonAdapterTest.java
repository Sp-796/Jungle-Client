package control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import client.model.Animal;
import client.model.GameState;
import client.model.MatchResult;
import client.model.Zone;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.message.Message;
import common.message.MessageType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonAdapterTest {

    private static final String GAME_STARTED_JSON = """
            {
              "matchCode": "ABC123",
              "playerId": "p1",
              "playerName": "Ana",
              "species": "jaguar",
              "flagColor": "Guaria Morada",
              "opponentName": "Beto",
              "opponentSpecies": "mono",
              "yourZoneId": "z1",
              "worldWidth": 1280,
              "worldHeight": 720,
              "animalSpeed": 50,
              "tickRateMs": 50,
              "zones": [
                {"id":"z1","name":"Páramo","image":"paramo.svg","x":0,"y":0,"width":320,"height":240,"hasFlag":false,"exits":["z2"]},
                {"id":"z2","name":"Manglar","image":"manglar.svg","x":320,"y":0,"width":320,"height":240,"hasFlag":true,"exits":["z1"]}
              ],
              "pack": {
                "species":"jaguar","damageCapacity":2,"alive":2,"total":2,
                "animals":[
                  {"id":1,"energy":20,"x":10,"y":20,"zoneId":"z1"},
                  {"id":2,"energy":18,"x":40,"y":50,"zoneId":"z1"}
                ]
              }
            }
            """;

    private static final String STATE_UPDATE_JSON = """
            {
              "matchCode":"ABC123",
              "playerName":"Ana",
              "matchDuration":12,
              "opponentName":"Beto",
              "gameOver":false,
              "playerWon":false,
              "availableZones":["z1","z2"],
              "viewedZone":{
                "id":"z1","name":"Páramo","image":"paramo.svg","x":0,"y":0,"width":320,"height":240,
                "hasFlag":true,"flagOwnerId":"p1","flagColor":"Guaria Morada","flagX":280,"flagY":210,
                "exits":["z2"]
              },
              "animals":[
                {"id":1,"species":"jaguar","energy":19,"x":15,"y":20,"alive":true,"zoneId":"z1","owner":"ME","targetX":100,"targetY":20,"moving":true},
                {"id":2,"species":"jaguar","energy":18,"x":40,"y":50,"alive":true,"zoneId":"z1","owner":"ME","targetX":40,"targetY":50,"moving":false},
                {"id":99,"species":"mono","energy":10,"x":200,"y":150,"alive":true,"zoneId":"z1","owner":"OPPONENT","targetX":200,"targetY":150,"moving":false},
                {"id":100,"species":"tucan","energy":10,"x":180,"y":120,"alive":true,"zoneId":"z1","owner":"BOT","targetX":180,"targetY":120,"moving":false}
              ],
              "coffeeCups":[{"id":"cup-1","x":120,"y":80}],
              "myPack":{"species":"jaguar","alive":2,"lost":0,"total":2,"botsKilled":3,"coffeeCups":1,"damageCapacity":4},
              "myAnimals":[
                {"id":1,"species":"jaguar","energy":19,"x":15,"y":20,"alive":true,"zoneId":"z1","owner":"ME","targetX":100,"targetY":20,"moving":true},
                {"id":2,"species":"jaguar","energy":18,"x":40,"y":50,"alive":true,"zoneId":"z1","owner":"ME","targetX":40,"targetY":50,"moving":false}
              ]
            }
            """;

    @Test
    void adaptaGameStartedConMapaPackYZonaInicial() {
        JsonAdapter adapter = new JsonAdapter();

        GameState state = adapter.adaptGameStarted(payload(GAME_STARTED_JSON));

        assertEquals("ABC123", state.getMatchCode());
        assertEquals("Ana", state.getPlayerName());
        assertEquals("Beto", state.getOpponentName());
        assertEquals("jaguar", state.getMySpecies());
        assertEquals("z1", state.getCurrentZoneId());
        assertEquals(2, state.getMapZones().size());

        Zone start = state.getZone("z1");
        assertNotNull(start);
        start.setFlag("p1", "Guaria Morada", 10, 10);
        assertTrue(start.isVisible());
        assertSame(start, state.getViewedZone());
        assertTrue(state.getAvailableZones().contains("z1"));

        assertEquals(2, state.getMyAnimals().size());
        assertEquals(2, state.getVisibleAnimals().size());
        assertEquals("jaguar", state.getMyAnimals().get(0).getSpecies());
        assertEquals("ME", state.getMyAnimals().get(0).getOwner());
        assertEquals(2, state.getAliveAnimalsPlayer());
        assertEquals(0, state.getDeadAnimalsPlayer());
        assertEquals(1, state.getAllPaths().size());
    }

    @Test
    void adaptaStateUpdateReutilizaAnimalesYAplicaNiebla() {
        JsonAdapter adapter = new JsonAdapter();
        GameState started = adapter.adaptGameStarted(payload(GAME_STARTED_JSON));

        Animal original = started.getMyAnimals().get(0);
        GameState updated = adapter.adaptStateUpdate(payload(STATE_UPDATE_JSON));

        assertEquals("ABC123", updated.getMatchCode());
        assertEquals(12, updated.getMatchDuration());
        assertEquals(2, updated.getAliveAnimalsPlayer());
        assertEquals(0, updated.getDeadAnimalsPlayer());
        assertEquals(3, updated.getBotsKilled());
        assertEquals(1, updated.getCoffeeCupsCollected());
        assertEquals(4, updated.getDamageCapacity());
        assertEquals(List.of("z1", "z2"), updated.getAvailableZones());

        Zone viewed = updated.getViewedZone();
        assertNotNull(viewed);
        assertEquals("z1", viewed.getZoneId());
        assertTrue(viewed.isVisible());
        assertTrue(viewed.hasFlag());
        assertEquals("p1", viewed.getFlagOwnerId());
        assertEquals("Guaria Morada", viewed.getFlagColor());
        assertEquals(280, viewed.getFlagX(), 1e-9);
        assertEquals(210, viewed.getFlagY(), 1e-9);
        assertFalse(updated.getZone("z2").isVisible());

        assertEquals(1, viewed.getPresentCoffeeCups().size());
        assertEquals(1, updated.getVisibleCoffeeCups().size());
        assertEquals("cup-1", updated.getVisibleCoffeeCups().get(0).getCupId());

        assertEquals(4, updated.getVisibleAnimals().size());

        Animal reused = updated.getMyAnimals().get(0);
        assertSame(original, reused);
        assertEquals(100, reused.getTargetX(), 1e-9);
        assertEquals(20, reused.getTargetY(), 1e-9);
        assertTrue(reused.isMoving());
        assertEquals(10, reused.getX(), 1e-9);

        Animal opponent = updated.getVisibleAnimals().stream()
                .filter(animal -> animal.getId() == 99)
                .findFirst()
                .orElseThrow();

        assertEquals("OPPONENT", opponent.getOwner());
        assertFalse(opponent.isMine());
    }

    @Test
    void adaptaJsonToGameStateSegunElTipoDeMensaje() {
        JsonAdapter adapter = new JsonAdapter();

        GameState state = adapter.adaptJsonToGameState(
                new Message(MessageType.GAME_STARTED, payload(GAME_STARTED_JSON)).toJson());

        assertNotNull(state);
        assertEquals("ABC123", state.getMatchCode());
        assertNull(adapter.adaptJsonToGameState(
                new Message(MessageType.ERROR).put("message", "x").toJson()));
    }

    @Test
    void serializaAccionesConElFormatoDelServidor() {
        JsonAdapter adapter = new JsonAdapter();

        String json = adapter.adaptActionToJson(MessageType.MOVE_ANIMALS, Map.of(
                "animalIds", List.of(3, 4),
                "targetX", 812.0,
                "targetY", 344.0
        ));

        Message message = Message.fromJson(json);

        assertEquals(MessageType.MOVE_ANIMALS, message.getType());
        assertEquals(2, message.getArray("animalIds").size());
        assertEquals(812.0, message.getDouble("targetX", 0), 1e-9);
        assertEquals(344.0, message.getDouble("targetY", 0), 1e-9);
    }

    @Test
    void adaptaGameOverConEstadisticasDeAmbosJugadores() {
        JsonAdapter adapter = new JsonAdapter();
        JsonObject payload = payload("""
                {
                  "matchCode":"ABC123",
                  "reason":"FLAG_TOUCHED",
                  "durationSeconds":75,
                  "winnerId":"p1",
                  "winnerName":"Ana",
                  "players":[
                    {"playerId":"p1","name":"Ana","species":"jaguar","alive":4,"lost":1,"botsKilled":2,"coffeeCups":3,"damageCapacity":5},
                    {"playerId":"p2","name":"Beto","species":"mono","alive":3,"lost":2,"botsKilled":1,"coffeeCups":0,"damageCapacity":3}
                  ]
                }
                """);

        MatchResult result = adapter.adaptGameOver(payload);

        assertTrue(result.isWinner("p1"));
        assertEquals("FLAG_TOUCHED", result.getReason());
        assertEquals(75, result.getDurationSeconds());
        assertEquals(2, result.getPlayers().size());
        assertEquals("Ana", result.getPlayers().get(0).getName());
        assertEquals(3, result.getPlayers().get(0).getCoffeeCups());
    }

    private static JsonObject payload(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
