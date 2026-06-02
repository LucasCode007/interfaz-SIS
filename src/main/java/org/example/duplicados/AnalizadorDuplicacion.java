package org.example.duplicados;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de análisis estructural de código Java para detección de duplicaciones.
 *
 * Adaptado para UI: recibe el código como String (no como archivo .txt),
 * por lo que funciona con código pegado directamente en un TextArea.
 *
 * Responsabilidades:
 *  - Parsear el código fuente y extraer módulos (clases y métodos).
 *  - Normalizar el código para comparación estructural ignorando nombres.
 *  - Comparar pares de módulos usando similitud Jaccard + ratio de longitud.
 *  - Retornar la lista de Duplicacion encontradas ordenadas por similitud.
 *
 * Colaboradores: Modulo, Duplicacion
 */
public class AnalizadorDuplicacion {

    private final List<Modulo> modulos = new ArrayList<>();

    // Mínimo de similitud estructural para reportar un par como duplicado.
    private static final float UMBRAL_SIMILITUD  = 0.60f;

    // Refactoring (bad smell: Magic Number) — mínimo de líneas normalizadas
    // para que un bloque sea considerado módulo relevante (bloques triviales ignorados).
    private static final int   MIN_LINEAS_MODULO = 3;

    private static final Pattern PAT_CLASE = Pattern.compile(
            "^\\s*(public|private|protected|abstract|final|)\\s*(class|interface|enum)\\s+(\\w+).*\\{\\s*$"
    );
    private static final Pattern PAT_METODO = Pattern.compile(
            "^\\s*(public|private|protected|static|final|abstract|synchronized|)\\s*" +
                    "(\\w[\\w<>\\[\\]?,\\s]*)\\s+(\\w+)\\s*\\([^)]*\\)\\s*(throws\\s+\\w+)?\\s*\\{\\s*$"
    );

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract","assert","boolean","break","byte","case","catch","char","class",
            "const","continue","default","do","double","else","enum","extends","final",
            "finally","float","for","goto","if","implements","import","instanceof","int",
            "interface","long","native","new","null","package","private","protected",
            "public","return","short","static","strictfp","super","switch","synchronized",
            "this","throw","throws","transient","try","void","volatile","while","true","false",
            "String","List","Map","Set","ArrayList","HashMap","HashSet","Object",
            "System","Math","Arrays","Collections","Optional","var","record","sealed"
    ));

    // Refactoring (bad smell: Primitive Obsession) — agrupa los datos de un bloque
    // abierto en lugar de usar dos pilas paralelas de int[] y String[].
    private static class BloqueAbierto {
        final int    lineaInicio;
        final int    profundidadAlAbrir;
        final String nombre;
        final String tipo;

        BloqueAbierto(int lineaInicio, int profundidadAlAbrir, String nombre, String tipo) {
            this.lineaInicio        = lineaInicio;
            this.profundidadAlAbrir = profundidadAlAbrir;
            this.nombre             = nombre;
            this.tipo               = tipo;
        }
    }

    public AnalizadorDuplicacion() {}

    // ─── Carga ────────────────────────────────────────────────────────────────

    /**
     * Recibe el código Java como String (desde la UI), lo parsea y extrae módulos.
     *
     * @param codigoFuente texto Java a analizar (puede ser una o varias clases)
     */
    public void cargarDesdeCodigo(String codigoFuente) {
        List<String> lineas = Arrays.asList(codigoFuente.split("\\r?\\n", -1));
        modulos.clear();
        extraerModulos(lineas);
    }

    // ─── Extracción de módulos ────────────────────────────────────────────────

    // Refactoring (bad smell: Long Method) — extraerModulos era un método largo
    // que mezclaba detección de clases, métodos, conteo de llaves y cierre de bloques.
    // Se separó cada responsabilidad en su propio método privado.

    private void extraerModulos(List<String> lineas) {
        Deque<BloqueAbierto> pila       = new ArrayDeque<>();
        int                  profundidad = 0;

        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);

            if (esInicioDeClase(linea)) {
                pila.push(new BloqueAbierto(i + 1, profundidad, nombreDeClase(linea), "clase"));
                profundidad++;
                continue;
            }

            if (esInicioDeMetodo(linea)) {
                pila.push(new BloqueAbierto(i + 1, profundidad, nombreDeMetodo(linea), "metodo"));
                profundidad++;
                continue;
            }

            profundidad += deltaDeLlaves(linea);

            if (!pila.isEmpty() && profundidad <= pila.peek().profundidadAlAbrir) {
                cerrarBloque(pila, lineas, i, profundidad);
                profundidad = pila.isEmpty() ? profundidad : profundidad;
            }
        }
    }

    private boolean esInicioDeClase(String linea) {
        return PAT_CLASE.matcher(linea).find();
    }

    private boolean esInicioDeMetodo(String linea) {
        Matcher m = PAT_METODO.matcher(linea);
        if (!m.find()) return false;
        String nombre = m.group(3);
        return !nombre.matches("if|else|for|while|switch|try|catch|finally");
    }

    private String nombreDeClase(String linea) {
        Matcher m = PAT_CLASE.matcher(linea);
        m.find();
        return m.group(3);
    }

    private String nombreDeMetodo(String linea) {
        Matcher m = PAT_METODO.matcher(linea);
        m.find();
        return m.group(3);
    }

    private void cerrarBloque(Deque<BloqueAbierto> pila, List<String> lineas,
                              int lineaActual, int profundidad) {
        BloqueAbierto bloque      = pila.pop();
        int           lineaFin    = lineaActual + 1;
        List<String>  fragmento   = lineas.subList(bloque.lineaInicio - 1, lineaFin);
        List<String>  normalizados = normalizar(fragmento);

        if (normalizados.size() >= MIN_LINEAS_MODULO) {
            modulos.add(new Modulo(bloque.nombre, bloque.tipo,
                    bloque.lineaInicio, lineaFin, normalizados));
        }
    }

    // ─── Conteo de llaves ────────────────────────────────────────────────────

    /** Devuelve la variación neta de profundidad de llaves en una línea (ignorando strings). */
    private int deltaDeLlaves(String linea) {
        boolean enString = false;
        char    prev     = 0;
        int     delta    = 0;
        for (char c : linea.toCharArray()) {
            if (c == '"' && prev != '\\') enString = !enString;
            if (!enString) {
                if      (c == '{') delta++;
                else if (c == '}') delta--;
            }
            prev = c;
        }
        return delta;
    }

    // ─── Normalización ────────────────────────────────────────────────────────

    /**
     * Normaliza líneas de código para comparación estructural:
     * reemplaza nombres de variables y literales, conservando solo
     * la estructura sintáctica (palabras clave, operadores, llaves, etc.).
     */
    private List<String> normalizar(List<String> lineas) {
        List<String> resultado = new ArrayList<>();
        for (String linea : lineas) {
            String norm = linea.trim();
            if (norm.isEmpty() || norm.startsWith("//") || norm.startsWith("*")) continue;
            norm = norm.replaceAll("//.*$", "");
            norm = norm.replaceAll("\"[^\"]*\"", "STR");
            norm = norm.replaceAll("\\b\\d+(\\.\\d+)?[fFdDlL]?\\b", "NUM");
            norm = reemplazarIdentificadores(norm);
            norm = norm.replaceAll("\\s+", " ").trim();
            if (!norm.isEmpty()) resultado.add(norm);
        }
        return resultado;
    }

    private String reemplazarIdentificadores(String linea) {
        StringBuilder   sb  = new StringBuilder();
        StringTokenizer tok = new StringTokenizer(linea,
                " \t(){}[];,.<>=!&|+-*/%^~?:@\"", true);
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            sb.append(t.matches("[a-zA-Z_$][a-zA-Z0-9_$]*") && !KEYWORDS.contains(t) ? "VAR" : t);
        }
        return sb.toString();
    }

    // ─── Análisis de similitud ────────────────────────────────────────────────

    /**
     * Compara todos los pares de módulos y devuelve los que superen
     * el umbral, ordenados de mayor a menor similitud.
     */
    public List<Duplicacion> analizar() {
        List<Duplicacion> resultado = new ArrayList<>();
        for (int i = 0; i < modulos.size(); i++) {
            for (int j = i + 1; j < modulos.size(); j++) {
                Modulo a   = modulos.get(i);
                Modulo b   = modulos.get(j);
                float  sim = calcularSimilitud(a.extraerBloques(), b.extraerBloques());
                if (sim >= UMBRAL_SIMILITUD) {
                    resultado.add(new Duplicacion(a, b, sim));
                }
            }
        }
        resultado.sort((x, y) -> Float.compare(y.getSimilitud(), x.getSimilitud()));
        return resultado;
    }

    /**
     * Similitud Jaccard sobre líneas únicas normalizadas,
     * con bonus por similitud de longitud (70% Jaccard + 30% ratio longitud).
     */
    private float calcularSimilitud(List<String> a, List<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0f;

        Set<String> setA = new HashSet<>(a);
        Set<String> setB = new HashSet<>(b);

        Set<String> interseccion = new HashSet<>(setA);
        interseccion.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) return 0f;

        float jaccard       = (float) interseccion.size() / union.size();
        float ratioLongitud = (float) Math.min(a.size(), b.size()) / Math.max(a.size(), b.size());
        return 0.7f * jaccard + 0.3f * ratioLongitud;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public List<Modulo> getModulos() { return modulos; }
}