package control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import client.model.GameState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.message.Message;
import common.message.MessageType;
import control.udp.UdpProtocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class NetworkClientIntegrationTest {

    @Test
    void conectaEnviaAccionesYRecibeEventosDelServidor() throws Exception {
        AtomicReference<Throwable> serverError = new AtomicReference<>();
        AtomicReference<String> receivedCreate = new AtomicReference<>();
        AtomicReference<String> receivedSpecies = new AtomicReference<>();
        AtomicReference<String> receivedView = new AtomicReference<>();
        AtomicReference<String> receivedMove = new AtomicReference<>();
        AtomicReference<String> receivedAttack = new AtomicReference<>();

        try (DatagramSocket serverSocket = new DatagramSocket(
                0, InetAddress.getLoopbackAddress())) {

            Thread server = new Thread(() -> runFakeServer(
                    serverSocket,
                    serverError,
                    receivedCreate,
                    receivedSpecies,
                    receivedView,
                    receivedMove,
                    receivedAttack
            ), "fake-udp-server");
            server.setDaemon(true);
            server.start();

            NetworkClient client = new NetworkClient();
            CountDownLatch connected = new CountDownLatch(1);
            CountDownLatch created = new CountDownLatch(1);
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch stateReceived = new CountDownLatch(1);
            AtomicReference<String> matchCode = new AtomicReference<>();
            AtomicReference<String> eventError = new AtomicReference<>();
            AtomicReference<GameState> lastState = new AtomicReference<>();

            client.addEventListener(new ClientEventListener() {
                @Override
                public void onConnected() {
                    connected.countDown();
                }

                @Override
                public void onMatchCreated(String code, String playerId, String playerName) {
                    matchCode.set(code);
                    created.countDown();
                }

                @Override
                public void onGameStarted(GameState gameState) {
                    lastState.set(gameState);
                    started.countDown();
                }

                @Override
                public void onError(String message) {
                    eventError.set(message);
                }
            });

            client.addObserver(gameState -> {
                if (gameState != null) {
                    lastState.set(gameState);
                    stateReceived.countDown();
                }
            });

            try {
                client.connectToServer("127.0.0.1", serverSocket.getLocalPort());

                assertTrue(connected.await(5, TimeUnit.SECONDS),
                        () -> "No llego onConnected: " + eventError.get());
                assertTrue(client.isConnected());

                client.sendCreateMatch("Ana");
                assertTrue(created.await(5, TimeUnit.SECONDS),
                        () -> "No llego MATCH_CREATED: " + eventError.get());
                assertEquals("ABC123", matchCode.get());

                client.sendSelectSpecies("jaguar");
                assertTrue(started.await(5, TimeUnit.SECONDS),
                        () -> "No llego GAME_STARTED: " + eventError.get());
                assertEquals("ABC123", lastState.get().getMatchCode());
                assertEquals(1, lastState.get().getMyAnimals().size());

                client.sendViewZone("z1");
                assertTrue(stateReceived.await(5, TimeUnit.SECONDS),
                        () -> "No llego STATE_UPDATE: " + eventError.get());
                assertEquals(1, lastState.get().getVisibleAnimals().size());

                client.sendMoveAnimals(List.of(1), 100, 200);
                client.sendAttack(List.of(1), 99);

                server.join(5000);
                assertFalse(server.isAlive(), "El servidor falso no termino");
                assertNull(serverError.get(), () -> "Fallo el servidor falso: " + serverError.get());
            } finally {
                client.disconnect();
            }
        }

        assertEquals(MessageType.CREATE_MATCH, receivedCreate.get());
        assertEquals(MessageType.SELECT_ANIMAL, receivedSpecies.get());
        assertEquals(MessageType.VIEW_ZONE, receivedView.get());
        assertEquals(MessageType.MOVE_ANIMALS, receivedMove.get());
        assertEquals(MessageType.ATTACK, receivedAttack.get());
    }

    // ------------------------------------------------------------------
    // Servidor UDP minimo para la prueba
    // ------------------------------------------------------------------

    private static void runFakeServer(DatagramSocket socket,
                                      AtomicReference<Throwable> serverError,
                                      AtomicReference<String> receivedCreate,
                                      AtomicReference<String> receivedSpecies,
                                      AtomicReference<String> receivedView,
                                      AtomicReference<String> receivedMove,
                                      AtomicReference<String> receivedAttack) {
        try {
            socket.setSoTimeout(5000);
            InetSocketAddress client = handshake(socket);

            int sendSequence = 0;
            int expectedSequence = 0;

            while (true) {
                UdpProtocol.Packet packet = receive(socket);
                if (packet == null) {
                    continue;
                }

                switch (packet.type()) {
                    case UdpProtocol.DATA -> {
                        if (packet.sequence() == expectedSequence) {
                            expectedSequence++;
                        }
                        sendAck(socket, client, expectedSequence - 1);

                        Message message = Message.fromJson(new String(packet.payload(), 1, packet.payload().length - 1, StandardCharsets.UTF_8));

                        switch (message.getType()) {
                            case MessageType.CREATE_MATCH -> {
                                receivedCreate.set(message.getType());

                                JsonObject createdPayload = new JsonObject();
                                createdPayload.addProperty("matchCode", "ABC123");
                                createdPayload.addProperty("playerId", "p1");
                                createdPayload.addProperty("playerName", "Ana");
                                createdPayload.addProperty("playerNumber", 1);
                                sendMessage(socket, client, sendSequence++,
                                        MessageType.MATCH_CREATED, createdPayload);
                            }
                            case MessageType.SELECT_ANIMAL -> {
                                receivedSpecies.set(message.getType());
                                sendMessage(socket, client, sendSequence++,
                                        MessageType.GAME_STARTED,
                                        payload(GAME_STARTED_JSON));
                            }
                            case MessageType.VIEW_ZONE -> {
                                receivedView.set(message.getType());
                                sendMessage(socket, client, sendSequence++,
                                        MessageType.STATE_UPDATE,
                                        payload(STATE_UPDATE_JSON));
                            }
                            case MessageType.MOVE_ANIMALS ->
                                    receivedMove.set(message.getType());
                            case MessageType.ATTACK -> {
                                receivedAttack.set(message.getType());
                                return;
                            }
                            default -> {
                                // Ignorar tipos que el test no espera.
                            }
                        }
                    }
                    case UdpProtocol.PING -> sendControl(socket, client, UdpProtocol.PONG);
                    case UdpProtocol.BYE -> {
                        return;
                    }
                    default -> {
                        // ACK y control: no requieren accion.
                    }
                }
            }
        } catch (Throwable error) {
            serverError.set(error);
        }
    }

    private static InetSocketAddress handshake(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[UdpProtocol.MAX_DATAGRAM_SIZE];

        while (true) {
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
            socket.receive(datagram);

            UdpProtocol.Packet packet = UdpProtocol.decode(
                    datagram.getData(), datagram.getLength());

            if (packet != null && packet.type() == UdpProtocol.HELLO) {
                InetSocketAddress client = (InetSocketAddress) datagram.getSocketAddress();
                sendControl(socket, client, UdpProtocol.HELLO_ACK);
                return client;
            }
        }
    }

    private static UdpProtocol.Packet receive(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[UdpProtocol.MAX_DATAGRAM_SIZE];

        try {
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
            socket.receive(datagram);
            return UdpProtocol.decode(datagram.getData(), datagram.getLength());
        } catch (SocketTimeoutException timeout) {
            throw new IOException("El cliente no envio nada en 5 segundos", timeout);
        }
    }

    private static void sendAck(DatagramSocket socket, InetSocketAddress client, int ack)
            throws IOException {
        byte[] datagram = UdpProtocol.encode(UdpProtocol.ACK, 0, ack, new byte[0]);
        socket.send(new DatagramPacket(datagram, datagram.length, client));
    }

    private static void sendControl(DatagramSocket socket, InetSocketAddress client, byte type)
            throws IOException {
        byte[] datagram = UdpProtocol.encode(type, 0, 0, new byte[0]);
        socket.send(new DatagramPacket(datagram, datagram.length, client));
    }

    private static void sendMessage(DatagramSocket socket,
                                    InetSocketAddress client,
                                    int sequence,
                                    String type,
                                    JsonObject payload) throws IOException {
        byte[] json = new Message(type, payload).toJson().getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[json.length + 1];
        body[0] = 1; // ultimo (y unico) trozo
        System.arraycopy(json, 0, body, 1, json.length);
        byte[] datagram = UdpProtocol.encode(UdpProtocol.DATA, sequence, 0, body);
        socket.send(new DatagramPacket(datagram, datagram.length, client));
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
                {"id":"z1","name":"PA�ramo","image":"paramo.svg","x":0,"y":0,"width":320,"height":240,"hasFlag":false,"exits":[]}
              ],
              "pack":{
                "species":"jaguar","damageCapacity":2,"alive":1,"total":1,
                "animals":[{"id":1,"energy":20,"x":10,"y":20,"zoneId":"z1"}]
              }
            }
            """;

    private static final String STATE_UPDATE_JSON = """
            {
              "matchCode":"ABC123",
              "playerName":"Ana",
              "matchDuration":5,
              "opponentName":"Beto",
              "gameOver":false,
              "playerWon":false,
              "availableZones":["z1"],
              "viewedZone":{
                "id":"z1","name":"PA�ramo","image":"paramo.svg","x":0,"y":0,"width":320,"height":240,
                "hasFlag":false,"exits":[]
              },
              "animals":[
                {"id":1,"species":"jaguar","energy":20,"x":20,"y":20,"alive":true,"zoneId":"z1","owner":"ME","targetX":20,"targetY":20,"moving":false}
              ],
              "coffeeCups":[],
              "myPack":{"species":"jaguar","alive":1,"lost":0,"total":1,"botsKilled":0,"coffeeCups":0,"damageCapacity":2},
              "myAnimals":[
                {"id":1,"species":"jaguar","energy":20,"x":20,"y":20,"alive":true,"zoneId":"z1","owner":"ME","targetX":20,"targetY":20,"moving":false}
              ]
            }
            """;
}
