package org.example.ui.duplicados;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.duplicados.AnalizadorDuplicacion;
import org.example.duplicados.Duplicacion;
import org.example.duplicados.Modulo;

import java.util.List;

/**
 * Interfaz JavaFX para el User Story: Verificación de Código Duplicado.
 *
 * El usuario pega su código Java en un TextArea y presiona "Analizar".
 * El motor AnalizadorDuplicacion procesa el texto y muestra:
 *  - Los módulos (clases/métodos) detectados.
 *  - Las duplicaciones encontradas con su porcentaje de similitud.
 *  - La propuesta de consolidación para cada par duplicado.
 */
public class DuplicadosUI {

    private TextArea               txtCodigo;
    private ListView<Modulo>       listModulos;
    private ListView<Duplicacion>  listDuplicaciones;
    private TextArea               txtPropuesta;
    private Label                  lblEstado;

    private final ObservableList<Modulo>      modulosObs       = FXCollections.observableArrayList();
    private final ObservableList<Duplicacion> duplicacionesObs = FXCollections.observableArrayList();

    // Refactoring (bad smell: Long Method) — show() antes construía toda la UI
    // en un solo método de más de 100 líneas. Ahora delega en dos métodos privados.
    public void show(Stage stage) {
        HBox root = new HBox(construirPanelIzquierdo(), new Separator(), construirPanelDerecho());
        root.setAlignment(Pos.TOP_LEFT);

        stage.setTitle("US — Verificación de código duplicado");
        stage.setScene(new Scene(root, 780, 600));
        stage.show();
    }

    // ─── Construcción de paneles ──────────────────────────────────

    private VBox construirPanelIzquierdo() {
        Label lblEntrada = new Label("Pega tu código Java aquí:");
        lblEntrada.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        txtCodigo = new TextArea();
        txtCodigo.setPromptText(
                "// Ejemplo:\npublic class MiClase {\n    public int calcular(int a, int b) {\n        return a + b;\n    }\n}"
        );
        txtCodigo.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        txtCodigo.setPrefHeight(320);
        VBox.setVgrow(txtCodigo, Priority.ALWAYS);

        Button btnAnalizar = new Button("🔍 Analizar código");
        btnAnalizar.setMaxWidth(Double.MAX_VALUE);
        btnAnalizar.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 16;");
        btnAnalizar.setOnAction(e -> ejecutarAnalisis());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarTodo());

        lblEstado = new Label("");
        lblEstado.setWrapText(true);

        VBox panel = new VBox(8, lblEntrada, txtCodigo, new Separator(),
                btnAnalizar, btnLimpiar, lblEstado);
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(340);
        panel.setStyle("-fx-background-color: #f9f9f9;");
        return panel;
    }

    private VBox construirPanelDerecho() {
        Label lblModulos = new Label("Módulos detectados:");
        lblModulos.setStyle("-fx-font-weight: bold;");

        listModulos = new ListView<>(modulosObs);
        listModulos.setPrefHeight(130);
        listModulos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Modulo m, boolean empty) {
                super.updateItem(m, empty);
                // Refactoring (bad smell: Duplicate Code) — patrón de reset de celda
                // extraído al método limpiarCelda() compartido entre ambas listas.
                if (empty || m == null) { limpiarCelda(this); return; }
                setText(String.format("[%s]  %s  (líneas %d–%d)",
                        m.getTipo().toUpperCase(), m.getNombre(),
                        m.getLineaInicio(), m.getLineaFin()));
                setStyle(m.getTipo().equals("clase")
                        ? "-fx-text-fill: #7B1FA2;"
                        : "-fx-text-fill: #1565C0;");
            }
        });

        Label lblDuplicaciones = new Label("Duplicaciones encontradas:");
        lblDuplicaciones.setStyle("-fx-font-weight: bold;");

        listDuplicaciones = new ListView<>(duplicacionesObs);
        listDuplicaciones.setPrefHeight(150);
        listDuplicaciones.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Duplicacion d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) { limpiarCelda(this); return; }
                float sim = d.getSimilitud();
                setText(String.format("%.1f%%  |  %s  ↔  %s",
                        sim * 100,
                        d.getOrigen().getNombre(),
                        d.getDestino().getNombre()));
                if      (sim >= 0.90f) setStyle("-fx-text-fill: #b71c1c; -fx-font-weight: bold;");
                else if (sim >= 0.75f) setStyle("-fx-text-fill: #e65100;");
                else                   setStyle("-fx-text-fill: #f9a825;");
            }
        });

        listDuplicaciones.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionada) -> {
                    if (seleccionada != null) txtPropuesta.setText(seleccionada.getPropuesta());
                }
        );

        Label lblPropuesta = new Label("Propuesta de consolidación:");
        lblPropuesta.setStyle("-fx-font-weight: bold;");

        txtPropuesta = new TextArea();
        txtPropuesta.setEditable(false);
        txtPropuesta.setWrapText(true);
        txtPropuesta.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        txtPropuesta.setPrefHeight(140);
        txtPropuesta.setPromptText("Selecciona una duplicación para ver la propuesta...");

        VBox panel = new VBox(8,
                lblModulos, listModulos, new Separator(),
                lblDuplicaciones, listDuplicaciones, new Separator(),
                lblPropuesta, txtPropuesta);
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(420);
        VBox.setVgrow(txtPropuesta, Priority.ALWAYS);
        return panel;
    }

    // ─── Helpers de celda ────────────────────────────────────────

    // Refactoring (bad smell: Duplicate Code) — resetea el estado visual de una celda.
    // Antes este bloque if (empty || x == null) se repetía igual en ambas cellFactory.
    private void limpiarCelda(ListCell<?> celda) {
        celda.setText(null);
        celda.setStyle("");
    }

    // ─── Lógica de análisis ──────────────────────────────────────

    private void ejecutarAnalisis() {
        String codigo = txtCodigo.getText();
        if (codigo == null || codigo.isBlank()) {
            setEstado("⚠ Escribe o pega código Java antes de analizar.", Color.ORANGE);
            return;
        }

        modulosObs.clear();
        duplicacionesObs.clear();
        txtPropuesta.clear();

        AnalizadorDuplicacion analizador = new AnalizadorDuplicacion();
        analizador.cargarDesdeCodigo(codigo);

        List<Modulo> modulos = analizador.getModulos();
        if (modulos.isEmpty()) {
            setEstado("⚠ No se detectaron clases ni métodos. Verifica que el código sea Java válido.", Color.ORANGE);
            return;
        }

        modulosObs.addAll(modulos);

        List<Duplicacion> duplicaciones = analizador.analizar();
        if (duplicaciones.isEmpty()) {
            setEstado(
                    String.format("✔ %d módulo(s) analizados. No se encontraron duplicaciones con umbral ≥ 60%%.",
                            modulos.size()),
                    Color.GREEN
            );
        } else {
            duplicacionesObs.addAll(duplicaciones);
            setEstado(
                    String.format("⚠ %d módulo(s) analizados. Se encontraron %d duplicación(es). Selecciona una para ver la propuesta.",
                            modulos.size(), duplicaciones.size()),
                    Color.web("#b71c1c")
            );
            listDuplicaciones.getSelectionModel().select(0);
        }
    }

    private void limpiarTodo() {
        txtCodigo.clear();
        modulosObs.clear();
        duplicacionesObs.clear();
        txtPropuesta.clear();
        setEstado("", Color.BLACK);
    }

    private void setEstado(String mensaje, Color color) {
        lblEstado.setText(mensaje);
        lblEstado.setTextFill(color);
    }
}