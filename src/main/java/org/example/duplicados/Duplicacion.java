package org.example.duplicados;

/**
 * Representa una duplicación estructural detectada entre dos módulos.
 *
 * Almacena los módulos de origen y destino, el porcentaje de similitud
 * y genera la propuesta textual de consolidación que se muestra en la UI.
 */
public class Duplicacion {

    private final Modulo origen;
    private final Modulo destino;
    private final float  similitud;  // 0.0 – 1.0

    // Refactoring (bad smell: Long Method en getPropuesta) — el texto fijo
    // de la acción se extrae como constante para que getPropuesta sea más legible.
    private static final String ACCION_CONSOLIDACION =
            "Extraer la lógica compartida a un método/clase utilitaria común.";

    public Duplicacion(Modulo origen, Modulo destino, float similitud) {
        this.origen    = origen;
        this.destino   = destino;
        this.similitud = similitud;
    }

    /**
     * Genera el texto de la propuesta de consolidación indicando
     * qué módulos están involucrados y cuál sería el punto de extracción.
     */
    public String getPropuesta() {
        return String.format(
                "[PROPUESTA DE CONSOLIDACIÓN]\n" +
                        "  Módulo A : %s\n" +
                        "  Módulo B : %s\n" +
                        "  Similitud: %.1f%%\n" +
                        "  Acción   : %s\n" +
                        "  Sugerencia: crear método '%s' y reemplazar ambas implementaciones.",
                origen,
                destino,
                similitud * 100,
                ACCION_CONSOLIDACION,
                generarNombreExtraccion()
        );
    }

    /**
     * Sugiere un nombre para el método extraído buscando el prefijo común
     * entre los nombres de los dos módulos involucrados.
     */
    private String generarNombreExtraccion() {
        String nombreA = origen.getNombre().replaceAll("[^a-zA-Z0-9]", "");
        String nombreB = destino.getNombre().replaceAll("[^a-zA-Z0-9]", "");

        // Refactoring (bad smell: Inconsistent naming) — la variable era 'i'
        // pero representa la longitud del prefijo común, no un índice de iteración.
        int longitudPrefijo = calcularLongitudPrefijo(nombreA, nombreB);
        String prefijo = (longitudPrefijo >= 3) ? nombreA.substring(0, longitudPrefijo) : nombreA;
        return prefijo + "Compartido";
    }

    private int calcularLongitudPrefijo(String a, String b) {
        int longitudMaxima = Math.min(a.length(), b.length());
        int longitudPrefijo = 0;
        while (longitudPrefijo < longitudMaxima && a.charAt(longitudPrefijo) == b.charAt(longitudPrefijo)) {
            longitudPrefijo++;
        }
        return longitudPrefijo;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public Modulo getOrigen()    { return origen; }
    public Modulo getDestino()   { return destino; }
    public float  getSimilitud() { return similitud; }

    @Override
    public String toString() {
        return String.format("Duplicacion[%s <-> %s | %.1f%%]",
                origen.getNombre(), destino.getNombre(), similitud * 100);
    }
}
