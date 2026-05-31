package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.ui.tdd.TddUI;

public class MainMenuUI {

    public void show(Stage stage) {
        Label titulo = new Label("Plataforma XP");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitulo = new Label("Selecciona el User Story a probar:");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        // ── Botón US: TDD (tuyo) ──────────────────────────────────
        Button btnTdd = new Button("US — Gestión de Casos de Prueba (TDD)");
        btnTdd.setPrefWidth(340);
        btnTdd.setStyle("-fx-font-size: 13px; -fx-padding: 10 20;");
        btnTdd.setOnAction(e -> new TddUI().show(new Stage()));

        // ── Botón US: compañero (él lo conecta a su clase) ────────
        Button btnCompanero = new Button("US — " + "[User Story de tu compañero]");
        btnCompanero.setPrefWidth(340);
        btnCompanero.setStyle("-fx-font-size: 13px; -fx-padding: 10 20;");
        btnCompanero.setDisable(true); // tu compañero lo habilita cuando suba su UI

        VBox layout = new VBox(16, titulo, subtitulo, btnTdd, btnCompanero);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        stage.setTitle("Plataforma XP — Menú Principal");
        stage.setScene(new Scene(layout, 450, 280));
        stage.show();
    }
}