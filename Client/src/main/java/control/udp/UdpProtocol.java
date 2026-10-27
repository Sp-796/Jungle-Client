// Paquete de control UDP del cliente: contiene el formato de los datagramas.
package control.udp;

// Importa las constantes del cliente.
import constants.IConstants;
// Importa ByteBuffer para buffers.
import java.nio.ByteBuffer;
// Importa StandardCharsets para UTF-8.
import java.nio.charset.StandardCharsets;

/**
 * Formato de los datagramas del transporte UDP.
 */
// Clase final con utilidades de protocolo UDP.
public final class UdpProtocol {

    // Magic number "JA".
    public static final int MAGIC = 0x4A41;

    // Version del protocolo.
    public static final byte VERSION = 1;

    // Tipos de datagrama.
    public static final byte DATA = 1;
    public static final byte ACK = 2;
    public static final byte HELLO = 3;
    public static final byte HELLO_ACK = 4;
    public static final byte BYE = 5;
    public static final byte PING = 6;
    public static final byte PONG = 7;

    // Tamano del encabezado.
    public static final int HEADER_SIZE = 12;

    // Tamano maximo del datagrama.
    public static final int MAX_DATAGRAM_SIZE = IConstants.udpMaxDatagramSize;

    // Datagrama decodificado.
    public record Packet(byte type, int sequence, int acknowledgement, byte[] payload) {

        // Convierte el payload a texto.
        public String payloadAsText() {
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    // Constructor privado.
    private UdpProtocol() {
    }

    // Codifica un datagrama.
    public static byte[] encode(byte type, int sequence, int acknowledgement, byte[] payload) {
        byte[] body = payload == null ? new byte[0] : payload;
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + body.length);
        buffer.putShort((short) MAGIC);
        buffer.put(VERSION);
        buffer.put(type);
        buffer.putInt(sequence);
        buffer.putInt(acknowledgement);
        buffer.put(body);
        return buffer.array();
    }

    // Codifica un datagrama con texto.
    public static byte[] encodeText(byte type, int sequence, int acknowledgement, String text) {
        byte[] body = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
        return encode(type, sequence, acknowledgement, body);
    }

    // Decodifica un datagrama.
    public static Packet decode(byte[] data, int length) {
        if (data == null || length < HEADER_SIZE) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);
        int magic = buffer.getShort() & 0xFFFF;
        byte version = buffer.get();
        byte type = buffer.get();
        int sequence = buffer.getInt();
        int acknowledgement = buffer.getInt();

        if (magic != MAGIC || version != VERSION) {
            return null;
        }

        byte[] payload = new byte[length - HEADER_SIZE];
        buffer.get(payload);
        return new Packet(type, sequence, acknowledgement, payload);
    }

    // Codifica un long.
    public static byte[] encodeLong(long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }
}