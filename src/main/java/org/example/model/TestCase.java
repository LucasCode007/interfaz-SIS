package org.example.model;

public class TestCase<I, O> {

    private String nombre;
    private I entradaEsperada;
    private O salidaEsperada;
    private Estado estado;
    private UserStory userStory;

    public TestCase(String nombre, I entradaEsperada, O salidaEsperada, UserStory userStory) {
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
    public I getEntradaEsperada()      { return entradaEsperada; }
    public O getSalidaEsperada()       { return salidaEsperada; }
    public Estado getEstado()          { return estado; }
    public UserStory getUserStory()    { return userStory; }
}
