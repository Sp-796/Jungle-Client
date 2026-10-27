// Paquete del modelo del cliente: contiene el resumen de la partida.
package client.model;

// Importa ArrayList para listas.
import java.util.ArrayList;
// Importa Collections para listas inmutables.
import java.util.Collections;
// Importa List para listas.
import java.util.List;

/**
 * Resumen que envia el servidor al terminar la partida (GAME_OVER).
 */
// Clase final que representa el resultado de la partida.
public final class MatchResult {

    // Codigo de la partida.
    private final String matchCode;
    // Razon de finalizacion.
    private final String reason;
    // Duracion en segundos.
    private final int durationSeconds;
    // Id del ganador.
    private final String winnerId;
    // Nombre del ganador.
    private final String winnerName;
    // Lista de resultados de jugadores.
    private final List<PlayerResult> players;

    // Constructor.
    public MatchResult(String matchCode,
                       String reason,
                       int durationSeconds,
                       String winnerId,
                       String winnerName,
                       List<PlayerResult> players) {
        // Asigna el codigo.
        this.matchCode = matchCode == null ? "" : matchCode;
        // Asigna la razon.
        this.reason = reason == null ? "" : reason;
        // Asigna la duracion.
        this.durationSeconds = durationSeconds;
        // Asigna el id del ganador.
        this.winnerId = winnerId;
        // Asigna el nombre del ganador.
        this.winnerName = winnerName == null ? "" : winnerName;
        // Copia la lista de jugadores.
        this.players = players == null ? new ArrayList<>() : new ArrayList<>(players);
    }

    // Indica si el jugador es el ganador.
    public boolean isWinner(String playerId) {
        // Compara el id.
        return playerId != null && playerId.equals(winnerId);
    }

    // Genera una descripcion textual del resultado.
    public String describe() {
        // StringBuilder para el texto.
        StringBuilder text = new StringBuilder();
        // Agrega el codigo.
        text.append("Partida ").append(matchCode).append('\n');
        // Agrega el motivo.
        text.append("Motivo: ").append(translateReason(reason)).append('\n');
        // Agrega la duracion.
        text.append("Duracion: ").append(durationSeconds).append(" s\n");
        // Agrega el ganador.
        text.append("Ganador: ").append(winnerName.isBlank() ? "(sin ganador)" : winnerName).append("\n\n");

        // Recorre los jugadores.
        for (PlayerResult player : players) {
            // Agrega el nombre y la especie.
            text.append(player.getName())
                    .append(" (").append(player.getSpecies()).append(")\n");
            // Agrega las estadisticas.
            text.append("  Vivos: ").append(player.getAlive())
                    .append(" | Perdidos: ").append(player.getLost())
                    .append(" | Bots eliminados: ").append(player.getBotsKilled())
                    .append(" | Cafe: ").append(player.getCoffeeCups())
                    .append(" | Dano: ").append(player.getDamageCapacity())
                    .append('\n');
        }

        // Retorna el texto.
        return text.toString();
    }

    // Traduce la razon a texto legible.
    public static String translateReason(String reason) {
        // Si es nulo, retorna desconocido.
        if (reason == null) {
            return "Desconocido";
        }
        // Traduce segun la razon.
        return switch (reason) {
            case "FLAG_TOUCHED" -> "Se toco la bandera del rival";
            case "PACK_DEFEATED" -> "Una manada se quedo sin animales";
            case "DISCONNECT" -> "Un jugador se desconecto";
            case "OPPONENT_LEFT" -> "El rival abandono la partida";
            default -> reason;
        };
    }

    // Getter del codigo.
    public String getMatchCode() {
        return matchCode;
    }

    // Getter de la razon.
    public String getReason() {
        return reason;
    }

    // Getter de la duracion.
    public int getDurationSeconds() {
        return durationSeconds;
    }

    // Getter del id del ganador.
    public String getWinnerId() {
        return winnerId;
    }

    // Getter del nombre del ganador.
    public String getWinnerName() {
        return winnerName;
    }

    // Getter de los jugadores.
    public List<PlayerResult> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    // Estadisticas de un participante.
    public static final class PlayerResult {

        // Id del jugador.
        private final String playerId;
        // Nombre.
        private final String name;
        // Especie.
        private final String species;
        // Animales vivos.
        private final int alive;
        // Animales perdidos.
        private final int lost;
        // Bots eliminados.
        private final int botsKilled;
        // Tazas de cafe.
        private final int coffeeCups;
        // Capacidad de dano.
        private final int damageCapacity;

        // Constructor.
        public PlayerResult(String playerId,
                            String name,
                            String species,
                            int alive,
                            int lost,
                            int botsKilled,
                            int coffeeCups,
                            int damageCapacity) {
            // Asigna el id.
            this.playerId = playerId == null ? "" : playerId;
            // Asigna el nombre.
            this.name = name == null ? "" : name;
            // Asigna la especie.
            this.species = species == null ? "" : species;
            // Asigna los vivos.
            this.alive = alive;
            // Asigna los perdidos.
            this.lost = lost;
            // Asigna los bots eliminados.
            this.botsKilled = botsKilled;
            // Asigna las tazas.
            this.coffeeCups = coffeeCups;
            // Asigna el dano.
            this.damageCapacity = damageCapacity;
        }

        // Getter del id.
        public String getPlayerId() {
            return playerId;
        }

        // Getter del nombre.
        public String getName() {
            return name;
        }

        // Getter de la especie.
        public String getSpecies() {
            return species;
        }

        // Getter de vivos.
        public int getAlive() {
            return alive;
        }

        // Getter de perdidos.
        public int getLost() {
            return lost;
        }

        // Getter de bots eliminados.
        public int getBotsKilled() {
            return botsKilled;
        }

        // Getter de tazas.
        public int getCoffeeCups() {
            return coffeeCups;
        }

        // Getter de dano.
        public int getDamageCapacity() {
            return damageCapacity;
        }
    }
}