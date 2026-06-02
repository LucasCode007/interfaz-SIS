package org.example.model;

public class TestCase {

    private String nombre;
    private String entradaEsperada;
    private String salidaEsperada;
    private Estado estado;
    private UserStory userStory;

    public TestCase(String nombre, String entradaEsperada, String salidaEsperada, UserStory userStory) {
        if (userStory == null) {
            throw new IllegalArgumentException("Un TestCase debe estar vinculado a una UserStory.");
        }
        this.nombre          = nombre;
        this.entradaEsperada = entradaEsperada;
        this.salidaEsperada  = salidaEsperada;
        this.estado          = Estado.RED;
        this.userStory       = userStory;
    }

    public void actualizarEstado(Estado nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public boolean estaEnGreen() {
        return this.estado == Estado.GREEN;
    }

    public String getNombre()          { return nombre; }
    public String getEntradaEsperada() { return entradaEsperada; }
    public String getSalidaEsperada()  { return salidaEsperada; }
    public Estado getEstado()          { return estado; }
    public UserStory getUserStory()    { return userStory; }
}