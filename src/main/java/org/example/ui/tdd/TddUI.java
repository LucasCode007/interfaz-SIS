package org.example.ui.tdd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Estado;
import org.example.model.TestCase;
import org.example.model.UserStory;

public class TddUI {

    // ── Estado global ─────────────────────────────────────────────
    private final ObservableList<UserStory> listaUS        = FXCollections.observableArrayList();
    private final ObservableList<TestCase> listaTests = FXCollections.observableArrayList();
    private UserStory usActiva = null;

    // ── Labels que se actualizan dinámicamente ────────────────────
    private Label lblUSActiva;
    private Label lblConteo;
    private Label lblResultadoCompletar;
    private ListView<TestCase> listViewTests;

    public void show(Stage stage) {

        // ══════════════════════════════════════════════════════════
        // PANEL IZQUIERDO — Lista de UserStories
        // ══════════════════════════════════════════════════════════
        Label lblTituloUS = new Label("User Stories");
        lblTituloUS.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField txtId     = new TextField(); txtId.setPromptText("ID (ej: US-01)");
        TextField txtTitulo = new TextField(); txtTitulo.setPromptText("Título");
        Button    btnCrear  = new Button("+ Crear User Story");
        Label     lblErrorUS = new Label("");
        lblErrorUS.setTextFill(Color.RED);

        btnCrear.setMaxWidth(Double.MAX_VALUE);
        btnCrear.setOnAction(e -> crearUserStory(txtId, txtTitulo, lblErrorUS));

        ListView<UserStory> listViewUS = new ListView<>(listaUS);
        listViewUS.setPrefHeight(300);
        listViewUS.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserStory us, boolean empty) {
                super.updateItem(us, empty);
                if (empty || us == null) {
                    setText(null); setStyle("");
                } else {
                    setText(us.getId() + " — " + us.getTitulo()
                            + (us.isCompletada() ? "  ✔" : ""));
                    setStyle(us.isCompletada()
                            ? "-fx-text-fill: green;"
                            : "-fx-text-fill: #222;");
                }
            }
        });

        listViewUS.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionada) -> {
                    if (seleccionada != null) cargarUS(seleccionada, listViewUS);
                }
        );

        VBox panelIzq = new VBox(8,
                lblTituloUS,
                new Separator(),
                txtId, txtTitulo, btnCrear, lblErrorUS,
                new Separator(),
                new Label("Selecciona una US:"),
                listViewUS
        );
        panelIzq.setPadding(new Insets(16));
        panelIzq.setPrefWidth(230);
        panelIzq.setStyle("-fx-background-color: #f9f9f9;");

        // ══════════════════════════════════════════════════════════
        // PANEL DERECHO — TestCases de la US activa
        // ══════════════════════════════════════════════════════════
        lblUSActiva = new Label("Selecciona una User Story");
        lblUSActiva.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField txtNombre  = new TextField(); txtNombre.setPromptText("Nombre del test");
        TextField txtEntrada = new TextField(); txtEntrada.setPromptText("Entrada esperada");
        TextField txtSalida  = new TextField(); txtSalida.setPromptText("Salida esperada");
        Button    btnAgregar = new Button("+ Agregar TestCase");
        Label     lblErrorTC = new Label("");
        lblErrorTC.setTextFill(Color.RED);

        btnAgregar.setMaxWidth(Double.MAX_VALUE);
        btnAgregar.setDisable(true);
        btnAgregar.setOnAction(e ->
                agregarTestCase(txtNombre, txtEntrada, txtSalida, lblErrorTC, listViewUS)
        );

        listViewTests = new ListView<>(listaTests);
        listViewTests.setPrefHeight(160);
        listViewTests.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TestCase tc, boolean empty) {
                super.updateItem(tc, empty);
                if (empty || tc == null) { setText(null); setStyle(""); }
                else {
                    setText(tc.getNombre()
                            + "  |  Entrada: " + tc.getEntradaEsperada()
                            + "  |  Salida: "  + tc.getSalidaEsperada()
                            + "  →  [" + tc.getEstado() + "]");
                    switch (tc.getEstado()) {
                        case RED           -> setStyle("-fx-text-fill: red;");
                        case GREEN         -> setStyle("-fx-text-fill: green;");
                        case REFACTORIZADO -> setStyle("-fx-text-fill: #1565C0;");
                    }
                }
            }
        });

        Label      lblCambio   = new Label("Cambiar estado del test seleccionado:");
        ComboBox<Estado> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll(Estado.RED, Estado.GREEN, Estado.REFACTORIZADO);
        cmbEstado.setValue(Estado.GREEN);

        Button btnCambiar = new Button("Aplicar");
        Label  lblErrorCambio = new Label("");
        lblErrorCambio.setTextFill(Color.RED);
        btnCambiar.setDisable(true);

        btnCambiar.setOnAction(e -> {
            TestCase sel = listViewTests.getSelectionModel().getSelectedItem(); // Modificado
            if (sel == null) {
                lblErrorCambio.setText("⚠ Selecciona un test de la lista.");
                return;
            }
            sel.actualizarEstado(cmbEstado.getValue());
            listViewTests.refresh();
            listViewUS.refresh();
            lblErrorCambio.setText("");
            actualizarConteo();
        });

        lblConteo = new Label("Conteo: —");
        lblConteo.setStyle("-fx-font-size: 12px;");

        lblResultadoCompletar = new Label("");

        Button btnCompletar = new Button("Completar User Story");
        btnCompletar.setStyle("-fx-font-weight: bold;");
        btnCompletar.setMaxWidth(Double.MAX_VALUE);
        btnCompletar.setOnAction(e -> {
            if (usActiva == null) return;
            boolean ok = usActiva.completar();
            if (ok) {
                lblResultadoCompletar.setTextFill(Color.GREEN);
                lblResultadoCompletar.setText("✔ UserStory completada exitosamente.");
                listViewUS.refresh();
            } else {
                lblResultadoCompletar.setTextFill(Color.RED);
                lblResultadoCompletar.setText("✘ No se puede completar: hay tests que no están en GREEN.");
            }
        });

        // Habilitar botones al seleccionar una US
        listViewUS.getSelectionModel().selectedItemProperty().addListener(
                (obs, ant, sel) -> {
                    boolean haySeleccion = sel != null;
                    btnAgregar.setDisable(!haySeleccion);
                    btnCambiar.setDisable(!haySeleccion);
                }
        );

        VBox panelDer = new VBox(8,
                lblUSActiva,
                new Separator(),
                new Label("Agregar TestCase:"),
                txtNombre, txtEntrada, txtSalida, btnAgregar, lblErrorTC,
                new Separator(),
                new Label("Tests registrados:"),
                listViewTests,
                new Separator(),
                lblCambio,
                new HBox(8, cmbEstado, btnCambiar),
                lblErrorCambio,
                lblConteo,
                new Separator(),
                btnCompletar,
                lblResultadoCompletar
        );
        panelDer.setPadding(new Insets(16));
        panelDer.setPrefWidth(420);

        // ══════════════════════════════════════════════════════════
        // LAYOUT PRINCIPAL
        // ══════════════════════════════════════════════════════════
        HBox root = new HBox(panelIzq, new Separator(), panelDer);
        root.setAlignment(Pos.TOP_LEFT);

        stage.setTitle("US — Gestión de Casos de Prueba (TDD)");
        stage.setScene(new Scene(root, 680, 580));
        stage.show();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void crearUserStory(TextField txtId, TextField txtTitulo, Label lblError) {
        if (txtId.getText().isBlank() || txtTitulo.getText().isBlank()) {
            lblError.setText("⚠ Completa ID y Título.");
            return;
        }
        boolean idDuplicado = listaUS.stream()
                .anyMatch(us -> us.getId().equalsIgnoreCase(txtId.getText().trim()));
        if (idDuplicado) {
            lblError.setText("⚠ Ya existe una US con ese ID.");
            return;
        }
        listaUS.add(new UserStory(txtId.getText().trim(), txtTitulo.getText().trim()));
        txtId.clear();
        txtTitulo.clear();
        lblError.setText("");
    }

    private void cargarUS(UserStory us, ListView<UserStory> listViewUS) {
        usActiva = us;
        lblUSActiva.setText("TestCases de: " + us.getId() + " — " + us.getTitulo());
        listaTests.clear();
        listaTests.addAll(us.getTestCases()); // Modificado: Eliminado el casteo, es más limpio
        lblResultadoCompletar.setText("");
        actualizarConteo();
    }

    private void agregarTestCase(TextField txtNombre, TextField txtEntrada,
                                 TextField txtSalida, Label lblError,
                                 ListView<UserStory> listViewUS) {
        if (usActiva == null) return;
        if (txtNombre.getText().isBlank()) {
            lblError.setText("⚠ El nombre es obligatorio.");
            return;
        }
        TestCase tc = new TestCase(
                txtNombre.getText().trim(),
                txtEntrada.getText().trim(),
                txtSalida.getText().trim(),
                usActiva
        );
        usActiva.agregarTestCase(tc);
        listaTests.add(tc);
        txtNombre.clear(); txtEntrada.clear(); txtSalida.clear();
        lblError.setText("");
        listViewUS.refresh();
        actualizarConteo();
    }

    private void actualizarConteo() {
        if (usActiva == null) return;
        int red    = usActiva.contarPorEstado(Estado.RED);
        int green  = usActiva.contarPorEstado(Estado.GREEN);
        int refact = usActiva.contarPorEstado(Estado.REFACTORIZADO);
        lblConteo.setText("RED: " + red + "   GREEN: " + green + "   REFACT: " + refact);
    }
}