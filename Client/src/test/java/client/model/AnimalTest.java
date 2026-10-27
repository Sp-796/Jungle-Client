package client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import constants.IConstants;
import org.junit.jupiter.api.Test;

class AnimalTest {

    @Test
    void interpolaHaciaElDestinoARitmoDelServidor() {
        Animal animal = new Animal(1, "jaguar", 20, 0, 0);
        animal.setTarget(100, 0);

        animal.updateLocalPosition(1000);

        assertEquals(IConstants.animalSpeed, animal.getX(), 1e-9);
        assertEquals(0, animal.getY(), 1e-9);

        animal.updateLocalPosition(1000);
        assertEquals(100, animal.getX(), 1e-9);
        assertFalse(animal.isMoving());
    }

    @Test
    void noMueveAnimalesMuertosNiConDeltaCero() {
        Animal alive = new Animal(1, "mono", 20, 10, 10);
        alive.setTarget(100, 100);

        alive.updateLocalPosition(0);
        assertEquals(10, alive.getX(), 1e-9);

        alive.setAlive(false);
        alive.updateLocalPosition(1000);
        assertEquals(10, alive.getX(), 1e-9);
    }

    @Test
    void conservaLaPosicionLocalSiElErrorEsPequenoYMantieneElTarget() {
        Animal animal = new Animal(1, "tucan", 20, 0, 0);

        animal.applyServerState("tucan", 18, 5, 0, true, "z1", "ME", 100, 0, true);

        assertEquals(0, animal.getX(), 1e-9);
        assertEquals(100, animal.getTargetX(), 1e-9);
        assertEquals("z1", animal.getZoneId());
        assertEquals("ME", animal.getOwner());
        assertTrue(animal.isMine());
        assertEquals(18, animal.getCurrentEnergy());
    }

    @Test
    void corrigeDeGolpeSiElErrorEsGrandeOCuandoNoSeMueve() {
        Animal animal = new Animal(1, "danta", 20, 0, 0);

        animal.applyServerState("danta", 20, 50, 60, true, "z2", "BOT", 50, 60, true);
        assertEquals(50, animal.getX(), 1e-9);
        assertEquals(60, animal.getY(), 1e-9);
        assertTrue(animal.isBot());

        animal.applyServerState("danta", 20, 70, 80, true, "z2", null, 70, 80, false);
        assertEquals(70, animal.getX(), 1e-9);
        assertEquals(80, animal.getY(), 1e-9);
        assertEquals("OPPONENT", animal.getOwner());
    }
}
