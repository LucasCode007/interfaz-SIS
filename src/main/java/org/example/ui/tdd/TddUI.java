package org.example.ui.tdd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.model.Estado;
import org.example.model.TestCase;
import org.example.model.UserStory;

public class TddUI {

    private UserStory userStory;
    private ObservableList<TestCase<String, String>> listaTests;

    public void show(Stage stage) {
        listaTests = FXCollections.observableArrayList();

        // ── Panel izquierdo: crear UserStory ──────────────────────
        Label lblUS = new Label("1. Crear User Story");
        lblUS.setStyle("-fx-font-weight: bold;");

        TextField txtIdUS    = new TextField();  txtIdUS.setPromptText("ID (ej: US-01)");
        TextField txtTituloUS = new TextField(); txtTituloUS.setPromptText("Título");

        Button btnCrearUS = new Button("Crear User Story");
        Label  lblEstadoUS = new Label("");

        btnCrearUS.setOnAction(e -> {
            if (txtIdUS.getText().isBlank() || txtTituloUS.getText().isBlank()) {
                lblEstadoUS.setText("⚠ Completa ID y Título.");
                return;
            }
            userStory = new UserStory(txtIdUS.getText(), txtTituloUS.getText());
            lblEstadoUS.setText("✔ UserStory \"" + userStory.getTitulo() + "\" creada.");
            listaTests.clear();
        });

        // ── Panel izquierdo: crear TestCase ───────────────────────
        Label lblTC = new Label("2. Agregar TestCase");
        lblTC.setStyle("-fx-font-weight: bold;");

        TextField txtNombre  = new TextField(); txtNombre.setPromptText("Nombre del test");
        TextField txtEntrada = new TextField(); txtEntrada.setPromptText("Entrada esperada");
        TextField txtSalida  = new TextField(); txtSalida.setPromptText("Salida esperada");

        Button btnAgregarTC = new Button("Agregar TestCase");
        Label  lblEstadoTC  = new Label("");

        btnAgregarTC.setOnAction(e -> {
            if (userStory == null) {
                lblEstadoTC.setText("⚠ Primero crea una UserStory.");
                return;
            }
            if (txtNombre.getText().isBlank()) {
                lblEstadoTC.setText("⚠ El nombre es obligatorio.");
                return;
            }
            TestCase<String, String> tc = new TestCase<>(
                    txtNombre.getText(),
                    txtEntrada.getText(),
                    txtSalida.getText(),
                    userStory
            );
            userStory.agregarTestCase(tc);
            listaTests.add(tc);
            lblEstadoTC.setText("✔ Test \"" + tc.getNombre() + "\" agregado en RED.");
            txtNombre.clear(); txtEntrada.clear(); txtSalida.clear();
        });

        VBox panelIzq = new VBox(8,
                lblUS, txtIdUS, txtTituloUS, btnCrearUS, lblEstadoUS,
                new Separator(),
                lblTC, txtNombre, txtEntrada, txtSalida, btnAgregarTC, lblEstadoTC
        );
        panelIzq.setPadding(new Insets(16));
        panelIzq.setPrefWidth(240);

        // ── Panel derecho: lista de TestCases ─────────────────────
        Label lblLista = new Label("3. TestCases de la UserStory");
        lblLista.setStyle("-fx-font-weight: bold;");

        ListView<TestCase<String, String>> listView = new ListView<>(listaTests);
        listView.setPrefHeight(200);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TestCase<String, String> tc, boolean empty) {
                super.updateItem(tc, empty);
                if (empty || tc == null) { setText(null); }
                else { setText(tc.getNombre() + "  [" + tc.getEstado() + "]"); }
            }
        });

        // Cambiar estado del test seleccionado
        Label lblCambio = new Label("Cambiar estado del test seleccionado:");
        ComboBox<Estado> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll(Estado.RED, Estado.GREEN, Estado.REFACTORIZADO);
        cmbEstado.setValue(Estado.GREEN);

        Button btnCambiarEstado = new Button("Aplicar estado");
        Label  lblResultadoCambio = new Label("");

        // Conteo por estado
        Label lblConteo = new Label("Conteo: —");
        lblConteo.setStyle("-fx-font-size: 12px; -fx-text-fill: #444;");

        btnCambiarEstado.setOnAction(e -> {
            TestCase<String, String> seleccionado = listView.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                lblResultadoCambio.setText("⚠ Selecciona un test de la lista.");
                return;
            }
            seleccionado.actualizarEstado(cmbEstado.getValue());
            listView.refresh();
            lblResultadoCambio.setText("✔ Estado actualizado a " + cmbEstado.getValue());
            actualizarConteo(userStory, lblConteo);
        });


        // Completar UserStory
        Button btnCompletar = new Button("Completar UserStory");
        Label  lblResultadoCompletar = new Label("");
        btnCompletar.setStyle("-fx-font-weight: bold;");

        btnCompletar.setOnAction(e -> {
            if (userStory == null) {
                lblResultadoCompletar.setText("⚠ No hay UserStory activa.");
                return;
            }
            boolean completada = userStory.completar();
            if (completada) {
                lblResultadoCompletar.setStyle("-fx-text-fill: green;");
                lblResultadoCompletar.setText("✔ UserStory completada exitosamente.");
            } else {
                lblResultadoCompletar.setStyle("-fx-text-fill: red;");
                lblResultadoCompletar.setText("✘ No se puede completar: hay tests que no están en GREEN.");
            }
        });

        VBox panelDer = new VBox(8,
                lblLista, listView,
                lblCambio,
                new HBox(8, cmbEstado, btnCambiarEstado),
                lblResultadoCambio,
                new Separator(),
                lblConteo,
                btnCompletar, lblResultadoCompletar
        );
        panelDer.setPadding(new Insets(16));
        panelDer.setPrefWidth(300);

        // ── Layout principal ──────────────────────────────────────
        HBox root = new HBox(new Separator(), panelIzq, new Separator(), panelDer);
        root.setAlignment(Pos.TOP_LEFT);

        stage.setTitle("US — Gestión de Casos de Prueba (TDD)");
        stage.setScene(new Scene(root, 580, 450));
        stage.show();
    }

    private void actualizarConteo(UserStory us, Label lblConteo) {
        if (us == null) return;
        int red    = us.contarPorEstado(Estado.RED);
        int green  = us.contarPorEstado(Estado.GREEN);
        int refact = us.contarPorEstado(Estado.REFACTORIZADO);
        lblConteo.setText("RED: " + red + "  |  GREEN: " + green + "  |  REFACT: " + refact);
    }
}