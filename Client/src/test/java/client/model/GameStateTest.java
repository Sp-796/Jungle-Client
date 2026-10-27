package client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import common.model.Point2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import ui.Observer;

class GameStateTest {

    @Test
    void notificaALosObservadoresRegistradosUnaSolaVez() {
        GameState state = new GameState();
        AtomicInteger calls = new AtomicInteger();
        Observer observer = current -> calls.incrementAndGet();

        state.addObserver(observer);
        state.addObserver(observer);
        state.notifyObservers();

        assertEquals(1, calls.get());

        state.removeObserver(observer);
        state.notifyObservers();
        assertEquals(1, calls.get());
    }

    @Test
    void eliminaCaminosDuplicadosEnLosDosSentidos() {
        Zone zoneOne = new Zone("z1", "Zona 1", "paramo.svg", 0, 0, 100, 100, false);
        Zone zoneTwo = new Zone("z2", "Zona 2", "manglar.svg", 200, 0, 100, 100, false);

        Point2D centerOne = zoneOne.getCenter();
        Point2D centerTwo = zoneTwo.getCenter();

        zoneOne.addPath(new Path("z1", "z2", centerOne, centerTwo));
        zoneTwo.addPath(new Path("z2", "z1", centerTwo, centerOne));

        Map<String, Zone> zones = new LinkedHashMap<>();
        zones.put("z1", zoneOne);
        zones.put("z2", zoneTwo);

        GameState state = new GameState();
        state.setMapZones(zones);

        List<Path> paths = state.getAllPaths();

        assertEquals(1, paths.size());
        assertSame(zoneOne, state.getZone("z1"));
        assertNull(state.getZone(null));
        assertNull(state.getZone("desconocida"));
    }

    @Test
    void calculaElCentroDeLaZona() {
        Zone zone = new Zone("z1", "Zona 1", "paramo.svg", 10, 20, 100, 60, false);
        Point2D center = zone.getCenter();

        assertEquals(60, center.getX(), 1e-9);
        assertEquals(50, center.getY(), 1e-9);
    }
}
