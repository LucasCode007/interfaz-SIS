package org.example.duplicados;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un bloque de código (clase o método) extraído del código analizado.
 * Almacena su nombre, tipo, líneas de inicio/fin y el contenido normalizado
 * que se usa para comparación estructural.
 */
public class Modulo {

    private final String       nombre;
    private final String       tipo;          // "clase" o "metodo"
    private final int          lineaInicio;
    private final int          lineaFin;
    private final List<String> bloques;       // líneas normalizadas (sin nombres de variables)

    public Modulo(String nombre, String tipo, int lineaInicio, int lineaFin, List<String> bloques) {
        this.nombre      = nombre;
        this.tipo        = tipo;
        this.lineaInicio = lineaInicio;
        this.lineaFin    = lineaFin;
        this.bloques     = new ArrayList<>(bloques);
    }

    /** Devuelve las líneas normalizadas para comparación estructural. */
    public List<String> extraerBloques() {
        return bloques;
    }

    public String getNombre()      { return nombre; }
    public String getTipo()        { return tipo; }
    public int    getLineaInicio() { return lineaInicio; }
    public int    getLineaFin()    { return lineaFin; }

    @Override
    public String toString() {
        return String.format("%s '%s' (líneas %d–%d)", tipo, nombre, lineaInicio, lineaFin);
    }
}