package ui;

import client.model.GameState;
import client.model.Zone;
import constants.IConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Panel superior con las estadisticas de la partida.
 *
 * Es un observador del estado: cada STATE_UPDATE refresca los contadores, la
 * zona disponible y el resultado final. El combo de zonas es la unica forma de
 * pedir VIEW_ZONE, porque el servidor solo permite ver una zona a la vez.
 */
@SuppressWarnings({"serial", "this-escape"})
public class HUDPanel extends JPanel implements Observer {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND = new Color(18, 24, 20);
    private static final Color FOREGROUND = new Color(235, 235, 230);
    private static final Color MUTED = new Color(170, 180, 170);
    private static final Color GOOD = new Color(120, 225, 140);
    private static final Color BAD = new Color(245, 110, 110);
    private static final Color WARN = new Color(245, 200, 90);

    private final JLabel playerLabel = valueLabel("--");
    private final JLabel speciesLabel = valueLabel("--");
    private final JLabel matchLabel = valueLabel("--");
    private final JLabel opponentLabel = valueLabel("--");
    private final JLabel durationLabel = valueLabel("00:00");
    private final JLabel aliveLabel = valueLabel("0");
    private final JLabel deadLabel = valueLabel("0");
    private final JLabel botsLabel = valueLabel("0");
    private final JLabel coffeeLabel = valueLabel("0");
    private final JLabel damageLabel = valueLabel("0");
    private final JLabel messageLabel = new JLabel(" ");
    private final JComboBox<ZoneItem> zoneSelector = new JComboBox<>();
    private final JButton selectAllButton = new JButton("Todos");

    private Consumer<String> zoneSelectionListener = zoneId -> {
    };
    private Runnable selectAllAction = () -> {
    };

    private GameState currentState;
    private boolean updatingZoneSelector;
    private boolean gameOverMessageShown;
    private Timer messageTimer;

    public HUDPanel() {
        setLayout(new BorderLayout(0, 3));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(6, 10, 6, 10));

        add(buildStatsPanel(), BorderLayout.CENTER);
        add(buildMessageRow(), BorderLayout.SOUTH);

        zoneSelector.setPreferredSize(new Dimension(170, 26));
        zoneSelector.setRenderer((list, value, index, selected, focused) -> {
            JLabel label = new JLabel(value == null ? "" : value.displayName());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            label.setBackground(selected ? new Color(60, 90, 70) : BACKGROUND);
            label.setForeground(selected ? Color.WHITE : FOREGROUND);
            return label;
        });
        zoneSelector.addActionListener(event -> {
            if (updatingZoneSelector) {
                return;
            }
            ZoneItem selected = (ZoneItem) zoneSelector.getSelectedItem();
            if (selected != null) {
                zoneSelectionListener.accept(selected.zoneId());
            }
        });

        selectAllButton.setFocusable(false);
        selectAllButton.addActionListener(event -> selectAllAction.run());

        setDefaults();
    }

    // ------------------------------------------------------------------
    // Construccion visual
    // ------------------------------------------------------------------

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setOpaque(false);

        JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        firstRow.setOpaque(false);
        firstRow.add(stat("Jugador", playerLabel));
        firstRow.add(stat("Especie", speciesLabel));
        firstRow.add(stat("Partida", matchLabel));
        firstRow.add(stat("Rival", opponentLabel));
        firstRow.add(stat("Tiempo", durationLabel));

        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        secondRow.setOpaque(false);
        secondRow.add(stat("Vivos", aliveLabel));
        secondRow.add(stat("Perdidos", deadLabel));
        secondRow.add(stat("Bots", botsLabel));
        secondRow.add(stat("Cafe", coffeeLabel));
        secondRow.add(stat("Dano", damageLabel));

        JPanel zonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        zonePanel.setOpaque(false);
        JLabel zoneTitle = new JLabel("Zona:");
        zoneTitle.setForeground(MUTED);
        zoneTitle.setFont(zoneTitle.getFont().deriveFont(Font.BOLD, 11f));
        zonePanel.add(zoneTitle);
        zonePanel.add(zoneSelector);
        zonePanel.add(selectAllButton);
        secondRow.add(zonePanel);

        panel.add(firstRow);
        panel.add(secondRow);
        return panel;
    }

    private JPanel buildMessageRow() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        messageLabel.setForeground(WARN);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 12f));
        panel.add(messageLabel, BorderLayout.WEST);
        return panel;
    }

    private static JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(FOREGROUND);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        return label;
    }

    private static JPanel stat(String title, JLabel value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panel.setOpaque(false);
        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setForeground(MUTED);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 11f));
        panel.add(titleLabel);
        panel.add(value);
        return panel;
    }

    // ------------------------------------------------------------------
    // Observer
    // ------------------------------------------------------------------

    @Override
    public void update(GameState gameState) {
        currentState = gameState;

        if (gameState == null) {
            setDefaults();
            return;
        }

        playerLabel.setText(orDash(gameState.getPlayerName()));
        speciesLabel.setText(speciesName(gameState.getMySpecies()));
        matchLabel.setText(orDash(gameState.getMatchCode()));
        opponentLabel.setText(orDash(gameState.getOpponentName()));
        durationLabel.setText(formatDuration(gameState.getMatchDuration()));
        damageLabel.setText(String.valueOf(gameState.getDamageCapacity()));

        drawScoreboard(gameState.getAliveAnimalsPlayer(), gameState.getBotsKilled());
        drawCoffeeCounter(gameState.getCoffeeCupsCollected());
        showGameOverScreen(gameState.isGameOver(), gameState.isPlayerWon());
        updateZoneSelector(gameState);
    }

    /** Muestra vivos, perdidos y bots eliminados. */
    private void drawScoreboard(int aliveAnimalsPlayer, int botsKilled) {
        aliveLabel.setText(String.valueOf(aliveAnimalsPlayer));
        aliveLabel.setForeground(aliveAnimalsPlayer > 0 ? GOOD : BAD);

        deadLabel.setText(String.valueOf(currentState == null ? 0 : currentState.getDeadAnimalsPlayer()));
        botsLabel.setText(String.valueOf(botsKilled));
        botsLabel.setForeground(botsKilled > 0 ? GOOD : FOREGROUND);

        damageLabel.setForeground(currentState != null && currentState.getDamageCapacity() > IConstants.initialDamagePlayer
                ? GOOD : FOREGROUND);
    }

    /** Muestra la cantidad de tazas de cafe consumidas. */
    private void drawCoffeeCounter(int cups) {
        coffeeLabel.setText(String.valueOf(cups));
        coffeeLabel.setForeground(cups > 0 ? GOOD : FOREGROUND);
    }

    /** Resultado final en el HUD (ademas del dialogo de la ventana). */
    private void showGameOverScreen(boolean isGameOver, boolean playerWon) {
        if (!isGameOver) {
            if (gameOverMessageShown) {
                gameOverMessageShown = false;
                setMessage(" ");
            }
            return;
        }

        if (gameOverMessageShown) {
            return;
        }

        gameOverMessageShown = true;
        messageLabel.setForeground(playerWon ? GOOD : BAD);
        messageLabel.setText(playerWon
                ? "Partida finalizada: GANASTE"
                : "Partida finalizada: PERDISTE");
    }

    // ------------------------------------------------------------------
    // Zonas
    // ------------------------------------------------------------------

    private void updateZoneSelector(GameState gameState) {
        List<String> available = gameState.getAvailableZones();

        if (!sameZoneIds(available)) {
            updatingZoneSelector = true;
            try {
                zoneSelector.removeAllItems();
                for (String zoneId : available) {
                    Zone zone = gameState.getZone(zoneId);
                    String name = zone == null ? zoneId : zone.getDisplayName();
                    zoneSelector.addItem(new ZoneItem(zoneId, name));
                }
            } finally {
                updatingZoneSelector = false;
            }
        }

        zoneSelector.setEnabled(!available.isEmpty());
        selectAllButton.setEnabled(gameState.getAliveAnimalsPlayer() > 0);

        String currentZoneId = gameState.getCurrentZoneId();
        if (currentZoneId == null) {
            return;
        }

        updatingZoneSelector = true;
        try {
            for (int index = 0; index < zoneSelector.getItemCount(); index++) {
                ZoneItem item = zoneSelector.getItemAt(index);
                if (item != null && item.zoneId().equals(currentZoneId)) {
                    zoneSelector.setSelectedIndex(index);
                    break;
                }
            }
        } finally {
            updatingZoneSelector = false;
        }
    }

    private boolean sameZoneIds(List<String> available) {
        if (available.size() != zoneSelector.getItemCount()) {
            return false;
        }
        for (int index = 0; index < available.size(); index++) {
            ZoneItem item = zoneSelector.getItemAt(index);
            if (item == null || !item.zoneId().equals(available.get(index))) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // API para la ventana
    // ------------------------------------------------------------------

    public void setZoneSelectionListener(Consumer<String> listener) {
        zoneSelectionListener = listener == null ? zoneId -> {
        } : listener;
    }

    public void setSelectAllAction(Runnable action) {
        selectAllAction = action == null ? () -> {
        } : action;
    }

    /** Mensaje informativo temporal (se borra solo). */
    public void setMessage(String message) {
        showTransientMessage(message, WARN);
    }

    /** Mensaje de error temporal (se borra solo). */
    public void setError(String message) {
        showTransientMessage(message, BAD);
    }

    private void showTransientMessage(String message, Color color) {
        if (messageTimer != null) {
            messageTimer.stop();
        }

        messageLabel.setForeground(color);
        messageLabel.setText(message == null || message.isBlank() ? " " : message);

        if (message == null || message.isBlank()) {
            return;
        }

        messageTimer = new Timer(4500, event -> messageLabel.setText(" "));
        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    private void setDefaults() {
        playerLabel.setText("--");
        speciesLabel.setText("--");
        matchLabel.setText("--");
        opponentLabel.setText("--");
        durationLabel.setText("00:00");
        aliveLabel.setText("0");
        aliveLabel.setForeground(FOREGROUND);
        deadLabel.setText("0");
        botsLabel.setText("0");
        botsLabel.setForeground(FOREGROUND);
        coffeeLabel.setText("0");
        coffeeLabel.setForeground(FOREGROUND);
        damageLabel.setText("0");
        damageLabel.setForeground(FOREGROUND);

        updatingZoneSelector = true;
        try {
            zoneSelector.removeAllItems();
        } finally {
            updatingZoneSelector = false;
        }
        zoneSelector.setEnabled(false);
        selectAllButton.setEnabled(false);

        gameOverMessageShown = false;
        if (messageTimer != null) {
            messageTimer.stop();
        }
        messageLabel.setText(" ");
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private static String formatDuration(int seconds) {
        int safe = Math.max(0, seconds);
        int minutes = safe / 60;
        int remaining = safe % 60;
        return String.format("%02d:%02d", minutes, remaining);
    }

    private static String orDash(String value) {
        return value == null || value.isBlank() ? "--" : value;
    }

    /** Nombres legibles de las 7 especies del servidor. */
    public static String speciesName(String speciesId) {
        if (speciesId == null || speciesId.isBlank()) {
            return "--";
        }
        return switch (speciesId) {
            case "perezoso" -> "Perezoso";
            case "mono" -> "Mono";
            case "tucan" -> "Tucan";
            case "ranaroja" -> "Rana Roja";
            case "jaguar" -> "Jaguar";
            case "danta" -> "Danta";
            case "boaconstructora" -> "Boa Constrictora";
            default -> speciesId;
        };
    }

    /** Elemento del combo de zonas: guarda el id real del protocolo. */
    public record ZoneItem(String zoneId, String displayName) {

        @Override
        public String toString() {
            return displayName;
        }
    }
}
