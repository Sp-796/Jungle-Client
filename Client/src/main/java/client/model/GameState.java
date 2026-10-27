// Paquete del modelo del cliente: contiene el estado global de la partida.
package client.model;

// Importa Observer de la UI.
import ui.Observer;
// Importa ArrayList para listas.
import java.util.ArrayList;
// Importa LinkedHashMap para mapas ordenados.
import java.util.LinkedHashMap;
// Importa List para listas.
import java.util.List;
// Importa Map para mapas.
import java.util.Map;

/**
 * Estado global de la partida en el cliente.
 *
 * Es el Sujeto del patron Observer: las vistas se registran y reciben una
 * notificacion cada vez que llega un STATE_UPDATE. Los datos se llenan en
 * {@code control.JsonAdapter}, que traduce el JSON del servidor.
 */
// Clase que representa el estado del juego en el cliente (patron Observer).
public class GameState {

    // Mapa de zonas.
    private Map<String, Zone> mapZones = new LinkedHashMap<>();
    // Animales visibles.
    private List<Animal> visibleAnimals = new ArrayList<>();
    // Animales propios.
    private List<Animal> myAnimals = new ArrayList<>();
    // Tazas visibles.
    private List<CoffeeCup> visibleCoffeeCups = new ArrayList<>();
    // Zonas disponibles.
    private List<String> availableZones = new ArrayList<>();

    // Animales vivos del jugador.
    private int aliveAnimalsPlayer;
    // Animales muertos del jugador.
    private int deadAnimalsPlayer;
    // Bots eliminados.
    private int botsKilled;
    // Tazas consumidas.
    private int coffeeCupsCollected;
    // Capacidad de dano.
    private int damageCapacity;

    // Nombre del jugador.
    private String playerName = "";
    // Nombre del oponente.
    private String opponentName = "";
    // Codigo de la partida.
    private String matchCode = "";
    // Especie propia.
    private String mySpecies = "";
    // Id de la zona actual.
    private String currentZoneId;

    // Indica si la partida termino.
    private boolean isGameOver;
    // Indica si el jugador gano.
    private boolean playerWon;
    // Duracion de la partida.
    private int matchDuration;

    // Zona vista.
    private Zone viewedZone;

    // Lista de observadores.
    private final List<Observer> observers = new ArrayList<>();

    // Registra un observador.
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    // Remueve un observador.
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    // Notifica a los observadores.
    public void notifyObservers() {
        for (Observer observer : new ArrayList<>(observers)) {
            observer.update(this);
        }
    }

    // Retorna una zona por id.
    public Zone getZone(String zoneId) {
        if (zoneId == null) {
            return null;
        }
        return mapZones.get(zoneId);
    }

    // Retorna todos los caminos sin repetir.
    public List<Path> getAllPaths() {
        Map<String, Path> unique = new LinkedHashMap<>();
        for (Zone zone : mapZones.values()) {
            for (Path path : zone.getConnectedPaths()) {
                unique.putIfAbsent(path.getKey(), path);
            }
        }
        return new ArrayList<>(unique.values());
    }

    // Getter de mapZones.
    public Map<String, Zone> getMapZones() {
        return mapZones;
    }

    // Setter de mapZones.
    public void setMapZones(Map<String, Zone> mapZones) {
        this.mapZones = mapZones == null ? new LinkedHashMap<>() : mapZones;
    }

    // Getter de visibleAnimals.
    public List<Animal> getVisibleAnimals() {
        return visibleAnimals;
    }

    // Setter de visibleAnimals.
    public void setVisibleAnimals(List<Animal> visibleAnimals) {
        this.visibleAnimals = visibleAnimals == null ? new ArrayList<>() : visibleAnimals;
    }

    // Getter de myAnimals.
    public List<Animal> getMyAnimals() {
        return myAnimals;
    }

    // Setter de myAnimals.
    public void setMyAnimals(List<Animal> myAnimals) {
        this.myAnimals = myAnimals == null ? new ArrayList<>() : myAnimals;
    }

    // Getter de visibleCoffeeCups.
    public List<CoffeeCup> getVisibleCoffeeCups() {
        return visibleCoffeeCups;
    }

    // Setter de visibleCoffeeCups.
    public void setVisibleCoffeeCups(List<CoffeeCup> visibleCoffeeCups) {
        this.visibleCoffeeCups = visibleCoffeeCups == null ? new ArrayList<>() : visibleCoffeeCups;
    }

    // Getter de availableZones.
    public List<String> getAvailableZones() {
        return availableZones;
    }

    // Setter de availableZones.
    public void setAvailableZones(List<String> availableZones) {
        this.availableZones = availableZones == null ? new ArrayList<>() : availableZones;
    }

    // Getter de aliveAnimalsPlayer.
    public int getAliveAnimalsPlayer() {
        return aliveAnimalsPlayer;
    }

    // Setter de aliveAnimalsPlayer.
    public void setAliveAnimalsPlayer(int aliveAnimalsPlayer) {
        this.aliveAnimalsPlayer = aliveAnimalsPlayer;
    }

    // Getter de deadAnimalsPlayer.
    public int getDeadAnimalsPlayer() {
        return deadAnimalsPlayer;
    }

    // Setter de deadAnimalsPlayer.
    public void setDeadAnimalsPlayer(int deadAnimalsPlayer) {
        this.deadAnimalsPlayer = deadAnimalsPlayer;
    }

    // Getter de botsKilled.
    public int getBotsKilled() {
        return botsKilled;
    }

    // Setter de botsKilled.
    public void setBotsKilled(int botsKilled) {
        this.botsKilled = botsKilled;
    }

    // Getter de coffeeCupsCollected.
    public int getCoffeeCupsCollected() {
        return coffeeCupsCollected;
    }

    // Setter de coffeeCupsCollected.
    public void setCoffeeCupsCollected(int coffeeCupsCollected) {
        this.coffeeCupsCollected = coffeeCupsCollected;
    }

    // Getter de damageCapacity.
    public int getDamageCapacity() {
        return damageCapacity;
    }

    // Setter de damageCapacity.
    public void setDamageCapacity(int damageCapacity) {
        this.damageCapacity = damageCapacity;
    }

    // Getter de playerName.
    public String getPlayerName() {
        return playerName;
    }

    // Setter de playerName.
    public void setPlayerName(String playerName) {
        this.playerName = playerName == null ? "" : playerName;
    }

    // Getter de opponentName.
    public String getOpponentName() {
        return opponentName;
    }

    // Setter de opponentName.
    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName == null ? "" : opponentName;
    }

    // Getter de matchCode.
    public String getMatchCode() {
        return matchCode;
    }

    // Setter de matchCode.
    public void setMatchCode(String matchCode) {
        this.matchCode = matchCode == null ? "" : matchCode;
    }

    // Getter de mySpecies.
    public String getMySpecies() {
        return mySpecies;
    }

    // Setter de mySpecies.
    public void setMySpecies(String mySpecies) {
        this.mySpecies = mySpecies == null ? "" : mySpecies;
    }

    // Getter de currentZoneId.
    public String getCurrentZoneId() {
        return currentZoneId;
    }

    // Setter de currentZoneId.
    public void setCurrentZoneId(String currentZoneId) {
        this.currentZoneId = currentZoneId;
    }

    // Indica si la partida termino.
    public boolean isGameOver() {
        return isGameOver;
    }

    // Setter de gameOver.
    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    // Indica si el jugador gano.
    public boolean isPlayerWon() {
        return playerWon;
    }

    // Setter de playerWon.
    public void setPlayerWon(boolean playerWon) {
        this.playerWon = playerWon;
    }

    // Getter de matchDuration.
    public int getMatchDuration() {
        return matchDuration;
    }

    // Setter de matchDuration.
    public void setMatchDuration(int matchDuration) {
        this.matchDuration = matchDuration;
    }

    // Getter de viewedZone.
    public Zone getViewedZone() {
        return viewedZone;
    }

    // Setter de viewedZone.
    public void setViewedZone(Zone viewedZone) {
        this.viewedZone = viewedZone;
    }
}