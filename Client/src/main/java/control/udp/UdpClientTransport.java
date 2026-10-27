// Paquete de control UDP del cliente: contiene el transporte UDP del cliente.
package control.udp;

// Importa las constantes del cliente.
import constants.IConstants;
// Importa IOException para errores de IO.
import java.io.IOException;
// Importa DatagramPacket para datagramas.
import java.net.DatagramPacket;
// Importa DatagramSocket para el socket.
import java.net.DatagramSocket;
// Importa InetAddress para direcciones.
import java.net.InetAddress;
// Importa PortUnreachableException.
import java.net.PortUnreachableException;
// Importa SocketTimeoutException.
import java.net.SocketTimeoutException;
// Importa SecureRandom para el id de sesion.
import java.security.SecureRandom;
// Importa BlockingQueue para la cola de entrada.
import java.util.concurrent.BlockingQueue;
// Importa LinkedBlockingQueue para la cola.
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Transporte UDP del cliente.
 */
// Clase final que implementa AutoCloseable para el transporte UDP.
public final class UdpClientTransport implements AutoCloseable {

    // Marcador de cierre.
    private static final String CLOSED = new String("\u0000closed");

    // Socket UDP.
    private final DatagramSocket socket;
    // Canal confiable.
    private final ReliableUdpChannel channel;
    // Cola de entrada.
    private final BlockingQueue<String> inbox = new LinkedBlockingQueue<>();

    // Indica si esta abierto.
    private volatile boolean open;
    // Hilo receptor.
    private Thread receiverThread;
    // Hilo de mantenimiento.
    private Thread housekeepingThread;

    // Constructor privado.
    private UdpClientTransport(DatagramSocket socket) {
        this.socket = socket;
        this.channel = new ReliableUdpChannel(datagram -> {
            DatagramPacket packet = new DatagramPacket(datagram, datagram.length);
            socket.send(packet);
        });
        this.channel.setReceiver(inbox::offer);
        this.open = true;
    }

    // Abre la conexion y completa el handshake.
    public static UdpClientTransport connect(String host, int port, int timeoutMs) throws IOException {
        // Resuelve la direccion.
        InetAddress address = InetAddress.getByName(host);
        // Crea el socket.
        DatagramSocket socket = new DatagramSocket();

        try {
            // Conecta el socket.
            socket.connect(address, port);
            // Timeout de recepcion.
            socket.setSoTimeout(IConstants.udpHandshakePollMs);
            // Buffer de recepcion.
            try {
                socket.setReceiveBufferSize(IConstants.udpReceiveBufferSize);
            } catch (IOException ignored) {
            }

            // Handshake.
            handshake(socket, timeoutMs);

            // Crea el transporte.
            UdpClientTransport transport = new UdpClientTransport(socket);
            // Inicia los hilos.
            transport.startThreads();
            // Retorna el transporte.
            return transport;
        } catch (IOException | RuntimeException error) {
            try {
                socket.close();
            } catch (RuntimeException ignored) {
            }
            if (error instanceof IOException io) {
                throw io;
            }
            throw new IOException("No se pudo abrir el canal UDP: " + error.getMessage(), error);
        }
    }

    // Realiza el handshake.
    private static void handshake(DatagramSocket socket, int timeoutMs) throws IOException {
        // Id de sesion aleatorio.
        long sessionId = new SecureRandom().nextLong();
        // Paquete HELLO.
        byte[] hello = UdpProtocol.encode(
                UdpProtocol.HELLO, 0, 0, UdpProtocol.encodeLong(sessionId));

        // Fecha limite.
        long deadline = System.nanoTime() + timeoutMs * 1_000_000L;
        // Buffer de recepcion.
        byte[] buffer = new byte[UdpProtocol.MAX_DATAGRAM_SIZE];

        // Mientras no expire el plazo.
        while (System.nanoTime() < deadline) {
            // Envia HELLO.
            socket.send(new DatagramPacket(hello, hello.length));

            try {
                // Recibe respuesta.
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);

                // Decodifica.
                UdpProtocol.Packet packet = UdpProtocol.decode(
                        response.getData(), response.getLength());

                // Si es HELLO_ACK, retorna.
                if (packet != null && packet.type() == UdpProtocol.HELLO_ACK) {
                    return;
                }
            } catch (SocketTimeoutException timeout) {
            } catch (PortUnreachableException unreachable) {
            }
        }

        // Si no respondio, lanza excepcion.
        throw new IOException("El servidor no respondio en " + timeoutMs + " ms");
    }

    // Inicia los hilos.
    private void startThreads() {
        // Hilo receptor.
        receiverThread = new Thread(this::receiveLoop, "udp-client-receiver");
        receiverThread.setDaemon(true);
        receiverThread.start();

        // Hilo de mantenimiento.
        housekeepingThread = new Thread(this::housekeepingLoop, "udp-client-housekeeping");
        housekeepingThread.setDaemon(true);
        housekeepingThread.start();
    }

    // Bucle de recepcion.
    private void receiveLoop() {
        byte[] buffer = new byte[UdpProtocol.MAX_DATAGRAM_SIZE];

        while (open) {
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(datagram);

                UdpProtocol.Packet packet = UdpProtocol.decode(
                        datagram.getData(), datagram.getLength());

                if (packet != null) {
                    channel.onPacket(packet, System.nanoTime());
                }
            } catch (SocketTimeoutException timeout) {
            } catch (PortUnreachableException unreachable) {
            } catch (IOException error) {
                if (open) {
                    closeInternal();
                }
                return;
            }
        }
    }

    // Bucle de mantenimiento.
    private void housekeepingLoop() {
        long lastPing = 0L;
        long timeoutNanos = IConstants.udpTimeoutMs * 1_000_000L;
        long keepAliveNanos = IConstants.udpKeepAliveMs * 1_000_000L;

        while (open) {
            try {
                Thread.sleep(IConstants.udpHousekeepingMs);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }

            long now = System.nanoTime();
            channel.tick(now);

            if (now - channel.lastReceivedNanos() > timeoutNanos) {
                closeInternal();
                return;
            }

            if (now - lastPing >= keepAliveNanos) {
                channel.sendControl(UdpProtocol.PING);
                lastPing = now;
            }
        }
    }

    // Envia una linea JSON.
    public boolean send(String message) {
        return open && channel.send(message);
    }

    // Recibe el siguiente mensaje.
    public String receive() {
        try {
            String line = inbox.take();
            return line == CLOSED ? null : line;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    // Indica si esta abierto.
    public boolean isOpen() {
        return open;
    }

    // Cierra el transporte.
    @Override
    public void close() {
        if (!open) {
            return;
        }

        channel.sendControl(UdpProtocol.BYE);
        channel.sendControl(UdpProtocol.BYE);
        closeInternal();
    }

    // Cierra internamente.
    private void closeInternal() {
        if (!open) {
            return;
        }

        open = false;
        channel.close();

        try {
            socket.close();
        } catch (RuntimeException ignored) {
        }

        inbox.offer(CLOSED);
    }
}