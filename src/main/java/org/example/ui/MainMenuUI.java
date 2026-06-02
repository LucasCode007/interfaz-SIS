package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.ui.duplicados.DuplicadosUI;
import org.example.ui.tdd.TddUI;

public class MainMenuUI {

    public void show(Stage stage) {
        Label titulo = new Label("Plataforma XP");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitulo = new Label("Selecciona el User Story a probar:");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        // ── Botón US: TDD (compañero) ─────────────────────────────
        Button btnTdd = new Button("US — Gestión de Casos de Prueba (TDD)");
        btnTdd.setPrefWidth(340);
        btnTdd.setStyle("-fx-font-size: 13px; -fx-padding: 10 20;");
        btnTdd.setOnAction(e -> new TddUI().show(new Stage()));

        // ── Botón US: Verificación de código duplicado (tuyo) ─────
        Button btnDuplicados = new Button("US — Verificación de código duplicado");
        btnDuplicados.setPrefWidth(340);
        btnDuplicados.setStyle("-fx-font-size: 13px; -fx-padding: 10 20;");
        btnDuplicados.setOnAction(e -> new DuplicadosUI().show(new Stage()));

        VBox layout = new VBox(16, titulo, subtitulo, btnTdd, btnDuplicados);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        stage.setTitle("Plataforma XP — Menú Principal");
        stage.setScene(new Scene(layout, 450, 280));
        stage.show();
    }
}
