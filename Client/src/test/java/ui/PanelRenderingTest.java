package ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import client.model.GameState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import control.JsonAdapter;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/**
 * Prueba de humo de las vistas: construye y pinta los paneles sin abrir una
 * ventana. Detecta NullPointerException y recursos faltantes en el dibujo.
 */
class PanelRenderingTest {

    @Test
    void pintaMapaHudYLobbySinErrores() throws Exception {
        JsonAdapter adapter = new JsonAdapter();
        GameState started = adapter.adaptGameStarted(payload(GAME_STARTED_JSON));
        GameState updated = adapter.adaptStateUpdate(payload(STATE_UPDATE_JSON));

        SwingUtilities.invokeAndWait(() -> {
            MapPanel mapPanel = new MapPanel();
            mapPanel.setSize(1000, 600);
            mapPanel.update(started);
            mapPanel.setSelectedAnimalIds(List.of(1));
            mapPanel.doLayout();
            assertTrue(paint(mapPanel, 1000, 600).getWidth() == 1000);

            mapPanel.update(updated);
            mapPanel.doLayout();
            paint(mapPanel, 1000, 600);

            HUDPanel hudPanel = new HUDPanel();
            hudPanel.setSize(1200, 90);
            hudPanel.update(updated);
            hudPanel.setZoneSelectionListener(zoneId -> {
            });
            hudPanel.setSelectAllAction(() -> {
            });
            hudPanel.doLayout();
            paint(hudPanel, 1200, 90);

            hudPanel.update((GameState) null);
            hudPanel.doLayout();
            paint(hudPanel, 1200, 90);

            LobbyPanel lobbyPanel = new LobbyPanel();
            lobbyPanel.setSize(900, 700);
            lobbyPanel.showWaiting("ABC123");
            lobbyPanel.doLayout();
            paint(lobbyPanel, 900, 700);

            lobbyPanel.showSelection("ABC123", List.of("jaguar", "mono"), "", "mono");
            lobbyPanel.setStatus("Selecciona una especie.", false);
            lobbyPanel.doLayout();
            paint(lobbyPanel, 900, 700);

            lobbyPanel.showSelection("ABC123", List.of("jaguar"), "jaguar", "mono");
            lobbyPanel.doLayout();
            paint(lobbyPanel, 900, 700);

            lobbyPanel.resetForNewMatch();
            lobbyPanel.doLayout();
            paint(lobbyPanel, 900, 700);

            mapPanel.update((GameState) null);
            mapPanel.doLayout();
            paint(mapPanel, 1000, 600);
        });
    }

    private static BufferedImage paint(JComponent component, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            component.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static JsonObject payload(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static final String GAME_STARTED_JSON = """
            {
              "matchCode":"ABC123",
              "playerId":"p1",
              "playerName":"Ana",
              "species":"jaguar",
              "opponentName":"Beto",
              "yourZoneId":"z1",
              "zones":[
                {"id":"z1","name":"Páramo","image":"paramo.svg","x":0,"y":0,"width":320,"height":240,"hasFlag":false,"exits":["z2"]},
                {"id":"z2","name":"Manglar","image":"manglar.svg","x":320,"y":0,"width":320,"height":240,"hasFlag":false,"exits":["z1"]}
              ],
              "pack":{
                "species":"jaguar","damageCapacity":2,"alive":1,"total":1,
                "animals":[{"id":1,"energy":20,"x":80,"y":90,"zoneId":"z1"}]
              }
            }
            """;

    private static final String STATE_UPDATE_JSON = """
            {
              "matchCode":"ABC123",
              "playerName":"Ana",
              "matchDuration":3,
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
                {"id":1,"species":"jaguar","energy":19,"x":80,"y":90,"alive":true,"zoneId":"z1","owner":"ME","targetX":180,"targetY":120,"moving":true},
                {"id":99,"species":"mono","energy":10,"x":220,"y":150,"alive":true,"zoneId":"z1","owner":"OPPONENT","targetX":220,"targetY":150,"moving":false}
              ],
              "coffeeCups":[{"id":"cup-1","x":120,"y":80}],
              "myPack":{"species":"jaguar","alive":1,"lost":0,"total":1,"botsKilled":0,"coffeeCups":0,"damageCapacity":2},
              "myAnimals":[
                {"id":1,"species":"jaguar","energy":19,"x":80,"y":90,"alive":true,"zoneId":"z1","owner":"ME","targetX":180,"targetY":120,"moving":true}
              ]
            }
            """;
}
