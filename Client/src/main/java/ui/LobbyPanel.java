package ui;

import constants.IConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla de conexion, lobby y eleccion de especie.
 *
 * El servidor maneja dos fases antes de jugar:
 *  1. CREATE_MATCH / JOIN_MATCH: crear o entrar con un codigo.
 *  2. SELECT_ANIMAL: cada jugador elige una especie distinta.
 *
 * Esta vista no habla con la red: solo notifica intenciones a traves de
 * callbacks que conecta {@link MainWindow}.
 */
@SuppressWarnings({"serial", "this-escape"})
public class LobbyPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND = new Color(14, 28, 20);
    private static final Color CARD = new Color(24, 42, 30);
    private static final Color FOREGROUND = new Color(235, 240, 232);
    private static final Color MUTED = new Color(165, 180, 165);
    private static final Color ACCENT = new Color(120, 210, 135);
    private static final Color BAD = new Color(245, 110, 110);
    private static final Color WARN = new Color(245, 200, 90);

    private final JTextField nicknameField = new JTextField(IConstants.defaultNickname, 18);
    private final JTextField hostField = new JTextField(IConstants.defaultServerIP, 15);
    private final JTextField portField = new JTextField(String.valueOf(IConstants.defaultServerPort), 6);
    private final JTextField codeField = new JTextField(8);

    private final JButton createButton = new JButton("Crear partida");
    private final JButton joinButton = new JButton("Unirse");

    private final JLabel matchLabel = new JLabel("Sin partida");
    private final JLabel waitingLabel = new JLabel(" ");
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel opponentLabel = new JLabel(" ");

    private final JComboBox<SpeciesItem> speciesSelector = new JComboBox<>();
    private final JButton confirmSpeciesButton = new JButton("Confirmar especie");
    private final JPanel speciesPanel = new JPanel(new BorderLayout(0, 8));

    private Consumer<CreateRequest> onCreate = request -> {
    };
    private Consumer<JoinRequest> onJoin = request -> {
    };
    private Consumer<String> onSpecies = species -> {
    };

    private boolean busy;
    private boolean locked;
    private boolean speciesConfirmed;

    public LobbyPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        wireActions();
        refreshEnabled();
    }

    // ------------------------------------------------------------------
    // Construccion visual
    // ------------------------------------------------------------------

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Jungle Attack");
        title.setForeground(ACCENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 34f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Crea una partida o unite con el codigo de tu rival");
        subtitle.setForeground(MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(Box.createVerticalStrut(6));
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(18));
        return header;
    }

    private JPanel buildCenter() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 95, 70), 1),
                new EmptyBorder(20, 24, 20, 24)
        ));
        card.setMaximumSize(new Dimension(620, 600));

        card.add(buildConnectionForm());
        card.add(Box.createVerticalStrut(16));
        card.add(buildMatchInfo());
        card.add(Box.createVerticalStrut(12));
        card.add(buildSpeciesSection());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        wrapper.add(card, constraints);
        return wrapper;
    }

    private JPanel buildConnectionForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, "Nickname", nicknameField);
        addField(panel, gbc, 1, "Servidor", hostField);
        addField(panel, gbc, 2, "Puerto", portField);

        createButton.setPreferredSize(new Dimension(150, 30));
        joinButton.setPreferredSize(new Dimension(110, 30));
        codeField.setToolTipText("Codigo que muestra el jugador que creo la partida");

        JPanel joinRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        joinRow.setOpaque(false);
        JLabel codeLabel = new JLabel("Codigo:");
        codeLabel.setForeground(MUTED);
        codeLabel.setFont(codeLabel.getFont().deriveFont(Font.BOLD, 12f));
        joinRow.add(codeLabel);
        joinRow.add(codeField);
        joinRow.add(joinButton);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        actions.add(createButton);
        actions.add(joinRow);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(actions, gbc);

        return panel;
    }

    private JPanel buildMatchInfo() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        matchLabel.setForeground(FOREGROUND);
        matchLabel.setFont(matchLabel.getFont().deriveFont(Font.BOLD, 16f));
        matchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        waitingLabel.setForeground(WARN);
        waitingLabel.setFont(waitingLabel.getFont().deriveFont(Font.PLAIN, 13f));
        waitingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        opponentLabel.setForeground(MUTED);
        opponentLabel.setFont(opponentLabel.getFont().deriveFont(Font.PLAIN, 12f));
        opponentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(matchLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(waitingLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(opponentLabel);
        return panel;
    }

    private JPanel buildSpeciesSection() {
        speciesPanel.setOpaque(false);
        speciesPanel.setVisible(false);
        speciesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Elige tu especie");
        title.setForeground(FOREGROUND);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));

        speciesSelector.setPreferredSize(new Dimension(260, 28));
        speciesSelector.setRenderer((list, value, index, selected, focused) -> {
            JLabel label = new JLabel(value == null ? "" : value.displayName());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            label.setBackground(selected ? new Color(60, 95, 70) : CARD);
            label.setForeground(FOREGROUND);
            return label;
        });

        confirmSpeciesButton.setPreferredSize(new Dimension(190, 30));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(speciesSelector);
        actions.add(confirmSpeciesButton);

        speciesPanel.add(title, BorderLayout.NORTH);
        speciesPanel.add(actions, BorderLayout.CENTER);
        return speciesPanel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
        footer.add(statusLabel, BorderLayout.WEST);
        return footer;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String title, JTextField field) {
        JLabel label = new JLabel(title + ":");
        label.setForeground(MUTED);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void wireActions() {
        createButton.addActionListener(event -> handleCreate());
        joinButton.addActionListener(event -> handleJoin());
        confirmSpeciesButton.addActionListener(event -> handleSpecies());
        codeField.addActionListener(event -> handleJoin());
        nicknameField.addActionListener(event -> handleCreate());
        portField.addActionListener(event -> handleCreate());

        createButton.setFocusPainted(false);
        joinButton.setFocusPainted(false);
        confirmSpeciesButton.setFocusPainted(false);

        createButton.setBackground(new Color(45, 105, 60));
        createButton.setForeground(Color.WHITE);
        joinButton.setBackground(new Color(45, 80, 110));
        joinButton.setForeground(Color.WHITE);
        confirmSpeciesButton.setBackground(new Color(80, 90, 45));
        confirmSpeciesButton.setForeground(Color.WHITE);
    }

    private void handleCreate() {
        String nickname = nicknameField.getText() == null ? "" : nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            setStatus("Escribe un nickname para jugar.", true);
            return;
        }
        if (nickname.length() > IConstants.maxNicknameLength) {
            setStatus("El nickname no puede pasar de " + IConstants.maxNicknameLength + " caracteres.", true);
            return;
        }

        Integer port = parsePort();
        if (port == null) {
            return;
        }

        String host = hostField.getText() == null ? "" : hostField.getText().trim();
        if (host.isEmpty()) {
            setStatus("Escribe la direccion del servidor.", true);
            return;
        }

        setBusy(true);
        setStatus("Conectando con " + host + ":" + port + "...", false);
        onCreate.accept(new CreateRequest(nickname, host, port));
    }

    private void handleJoin() {
        String nickname = nicknameField.getText() == null ? "" : nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            setStatus("Escribe un nickname para jugar.", true);
            return;
        }
        if (nickname.length() > IConstants.maxNicknameLength) {
            setStatus("El nickname no puede pasar de " + IConstants.maxNicknameLength + " caracteres.", true);
            return;
        }

        String code = codeField.getText() == null ? "" : codeField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            setStatus("Escribe el codigo de la partida.", true);
            return;
        }

        Integer port = parsePort();
        if (port == null) {
            return;
        }

        String host = hostField.getText() == null ? "" : hostField.getText().trim();
        if (host.isEmpty()) {
            setStatus("Escribe la direccion del servidor.", true);
            return;
        }

        setBusy(true);
        setStatus("Conectando con " + host + ":" + port + "...", false);
        onJoin.accept(new JoinRequest(nickname, host, port, code));
    }

    private void handleSpecies() {
        SpeciesItem item = (SpeciesItem) speciesSelector.getSelectedItem();
        if (item == null) {
            setStatus("Selecciona una especie.", true);
            return;
        }

        speciesConfirmed = true;
        setBusy(true);
        setStatus("Enviando seleccion de " + item.displayName() + "...", false);
        onSpecies.accept(item.id());
    }

    private Integer parsePort() {
        String text = portField.getText() == null ? "" : portField.getText().trim();
        try {
            int port = Integer.parseInt(text);
            if (port < 1 || port > 65535) {
                setStatus("El puerto debe estar entre 1 y 65535.", true);
                return null;
            }
            return port;
        } catch (NumberFormatException invalid) {
            setStatus("El puerto debe ser un numero.", true);
            return null;
        }
    }

    // ------------------------------------------------------------------
    // API para MainWindow
    // ------------------------------------------------------------------

    public void setOnCreate(Consumer<CreateRequest> listener) {
        onCreate = listener == null ? request -> {
        } : listener;
    }

    public void setOnJoin(Consumer<JoinRequest> listener) {
        onJoin = listener == null ? request -> {
        } : listener;
    }

    public void setOnSpecies(Consumer<String> listener) {
        onSpecies = listener == null ? species -> {
        } : listener;
    }

    /** El jugador creo la partida y espera al rival. */
    public void showWaiting(String matchCode) {
        locked = true;
        busy = false;
        speciesConfirmed = false;
        matchLabel.setText("Partida: " + safe(matchCode));
        waitingLabel.setText("Esperando a que se una otro jugador...");
        opponentLabel.setText("Comparte el codigo " + safe(matchCode) + " con tu rival.");
        speciesPanel.setVisible(false);
        refreshEnabled();
    }

    /**
     * Muestra la seleccion de especie. Se usa tanto al entrar a la partida como
     * despues de cada ANIMAL_SELECTION (cuando el rival elige).
     */
    public void showSelection(String matchCode,
                              List<String> availableSpecies,
                              String yourSpecies,
                              String opponentSpecies) {
        locked = true;
        busy = false;
        matchLabel.setText("Partida: " + safe(matchCode));
        speciesPanel.setVisible(true);
        updateSpeciesList(availableSpecies, yourSpecies);

        if (yourSpecies != null && !yourSpecies.isBlank()) {
            speciesConfirmed = true;
            waitingLabel.setText("Elegiste " + HUDPanel.speciesName(yourSpecies) + ". Esperando al rival...");
        } else {
            speciesConfirmed = false;
            waitingLabel.setText("Elige una especie para comenzar.");
        }

        if (opponentSpecies != null && !opponentSpecies.isBlank()) {
            opponentLabel.setText("El rival ya eligio " + HUDPanel.speciesName(opponentSpecies) + ".");
        } else {
            opponentLabel.setText("El rival todavia no eligio especie.");
        }

        refreshEnabled();
    }

    private void updateSpeciesList(List<String> availableSpecies, String selectedSpecies) {
        speciesSelector.removeAllItems();

        if (availableSpecies != null) {
            for (String speciesId : availableSpecies) {
                speciesSelector.addItem(new SpeciesItem(speciesId, HUDPanel.speciesName(speciesId)));
            }
        }

        if (selectedSpecies != null && !selectedSpecies.isBlank()) {
            boolean present = false;
            for (int index = 0; index < speciesSelector.getItemCount(); index++) {
                SpeciesItem item = speciesSelector.getItemAt(index);
                if (item != null && item.id().equals(selectedSpecies)) {
                    speciesSelector.setSelectedIndex(index);
                    present = true;
                    break;
                }
            }
            if (!present) {
                SpeciesItem item = new SpeciesItem(selectedSpecies, HUDPanel.speciesName(selectedSpecies));
                speciesSelector.addItem(item);
                speciesSelector.setSelectedItem(item);
            }
        }

        speciesSelector.setEnabled(!availableSpeciesIsEmpty());
    }

    private boolean availableSpeciesIsEmpty() {
        return speciesSelector.getItemCount() == 0;
    }

    /** Bloquea el formulario mientras se espera al rival o empieza la partida. */
    public void setLobbyLocked(boolean value) {
        locked = value;
        refreshEnabled();
    }

    /** Deshabilita temporalmente los botones mientras se conecta. */
    public void setBusy(boolean value) {
        busy = value;
        refreshEnabled();
    }

    public void setStatus(String message, boolean error) {
        statusLabel.setForeground(error ? BAD : MUTED);
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    /** Permite reintentar la eleccion si el servidor rechazo la especie. */
    public void allowSpeciesRetry() {
        busy = false;
        speciesConfirmed = false;
        refreshEnabled();
    }

    /** Vuelve al estado inicial para crear o unirse a otra partida. */
    public void resetForNewMatch() {
        locked = false;
        busy = false;
        speciesConfirmed = false;
        codeField.setText("");
        matchLabel.setText("Sin partida");
        waitingLabel.setText(" ");
        opponentLabel.setText(" ");
        speciesPanel.setVisible(false);
        speciesSelector.removeAllItems();
        setStatus("Listo para una nueva partida.", false);
        refreshEnabled();
    }

    public String getNickname() {
        return nicknameField.getText() == null ? "" : nicknameField.getText().trim();
    }

    private void refreshEnabled() {
        boolean formEnabled = !busy && !locked;

        nicknameField.setEnabled(formEnabled);
        hostField.setEnabled(formEnabled);
        portField.setEnabled(formEnabled);
        codeField.setEnabled(formEnabled);

        createButton.setEnabled(formEnabled);
        joinButton.setEnabled(formEnabled);

        boolean canConfirm = !busy && locked
                && speciesPanel.isVisible()
                && !speciesConfirmed
                && speciesSelector.getItemCount() > 0;

        speciesSelector.setEnabled(canConfirm);
        confirmSpeciesButton.setEnabled(canConfirm);
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "----" : value;
    }

    // ------------------------------------------------------------------
    // DTOs de las acciones del lobby
    // ------------------------------------------------------------------

    public record CreateRequest(String nickname, String host, int port) {
    }

    public record JoinRequest(String nickname, String host, int port, String code) {
    }

    public record SpeciesItem(String id, String displayName) {

        @Override
        public String toString() {
            return displayName;
        }
    }
}
