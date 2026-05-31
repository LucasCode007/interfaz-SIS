package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class UserStory {

    private String id;
    private String titulo;
    private boolean completada;
    private List<TestCase<?, ?>> testCases;

    public UserStory(String id, String titulo) {
        this.id         = id;
        this.titulo     = titulo;
        this.completada = false;
        this.testCases  = new ArrayList<>();
    }

    public void agregarTestCase(TestCase<?, ?> testCase) {
        testCases.add(testCase);
    }

    public boolean todosEnGreen() {
        if (testCases.isEmpty()) return false;
        return testCases.stream().allMatch(TestCase::estaEnGreen);
    }

    public boolean completar() {
        if (!todosEnGreen()) return false;
        this.completada = true;
        return true;
    }

    public int contarPorEstado(Estado estado) {
        return (int) testCases.stream()
                .filter(tc -> tc.getEstado() == estado)
                .count();
    }

    // Getters
    public String getId()          { return id; }
    public String getTitulo()      { return titulo; }
    public boolean isCompletada()  { return completada; }
}
