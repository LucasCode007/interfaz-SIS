import org.example.duplicados.AnalizadorDuplicacion;
import org.example.duplicados.Duplicacion;
import org.example.duplicados.Modulo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el User Story: Verificación de código duplicado.
 *
 * Validan el motor AnalizadorDuplicacion y los modelos Modulo/Duplicacion.
 * El código se pasa directamente como String, tal como lo introduce el usuario
 * en el TextArea de DuplicadosUI.
 */
class duplicadosTest {

    // Refactoring (bad smell: Long inline string literals) — los strings de código
    // Java embebidos en los tests con concatenación de '+' se extraen como constantes
    // estáticas con nombre descriptivo, igual que CODIGO_CON_DUPLICACION
    // y CODIGO_SIN_DUPLICACION que ya lo hacían correctamente.

    private static final String CODIGO_CON_DUPLICACION =
            "public class A {\n" +
                    "    public double calcularPrecio(double precio, int cantidad) {\n" +
                    "        double subtotal = precio * cantidad;\n" +
                    "        double impuesto = subtotal * 0.15;\n" +
                    "        double total = subtotal + impuesto;\n" +
                    "        if (total > 1000) {\n" +
                    "            total = total * 0.90;\n" +
                    "        }\n" +
                    "        return total;\n" +
                    "    }\n" +
                    "    public double calcularCosto(double monto, int unidades) {\n" +
                    "        double subtotal = monto * unidades;\n" +
                    "        double impuesto = subtotal * 0.15;\n" +
                    "        double total = subtotal + impuesto;\n" +
                    "        if (total > 1000) {\n" +
                    "            total = total * 0.90;\n" +
                    "        }\n" +
                    "        return total;\n" +
                    "    }\n" +
                    "}\n";

    private static final String CODIGO_SIN_DUPLICACION =
            "public class B {\n" +
                    "    public String clasificarEdad(int edad) {\n" +
                    "        if (edad < 0) {\n" +
                    "            return \"invalido\";\n" +
                    "        } else if (edad < 12) {\n" +
                    "            return \"nino\";\n" +
                    "        } else if (edad < 18) {\n" +
                    "            return \"adolescente\";\n" +
                    "        }\n" +
                    "        return \"adulto\";\n" +
                    "    }\n" +
                    "    public void imprimirTabla(int limite) {\n" +
                    "        for (int i = 1; i <= limite; i++) {\n" +
                    "            for (int j = 1; j <= limite; j++) {\n" +
                    "                System.out.println(i + \" x \" + j);\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n";

    private static final String CODIGO_MISMA_LOGICA_DISTINTOS_NOMBRES =
            "public class C {\n" +
                    "    public int calcularA(int x, int y) {\n" +
                    "        int result = x * y;\n" +
                    "        result = result + 10;\n" +
                    "        return result;\n" +
                    "    }\n" +
                    "    public int calcularB(int alpha, int beta) {\n" +
                    "        int total = alpha * beta;\n" +
                    "        total = total + 10;\n" +
                    "        return total;\n" +
                    "    }\n" +
                    "}\n";

    private static final String CODIGO_TRES_METODOS_SIMILARES =
            "public class D {\n" +
                    "    public int calcularA(int x, int y) {\n" +
                    "        int result = x * y;\n" +
                    "        result = result + 10;\n" +
                    "        return result;\n" +
                    "    }\n" +
                    "    public int calcularB(int a, int b) {\n" +
                    "        int result = a * b;\n" +
                    "        result = result + 10;\n" +
                    "        return result;\n" +
                    "    }\n" +
                    "    public int calcularC(int n, int m) {\n" +
                    "        int result = n * m;\n" +
                    "        result = result + 10;\n" +
                    "        if (result > 100) { result = 0; }\n" +
                    "        return result;\n" +
                    "    }\n" +
                    "}\n";

    private AnalizadorDuplicacion analizador;

    @BeforeEach
    void setUp() {
        analizador = new AnalizadorDuplicacion();
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 1 — El analizador detecta métodos con lógica idéntica
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C1-T1: Métodos con lógica idéntica son detectados como duplicación")
    void analizar_metodosIdenticos_detectaDuplicacion() {
        analizador.cargarDesdeCodigo(CODIGO_CON_DUPLICACION);
        List<Duplicacion> resultado = analizador.analizar();

        assertFalse(resultado.isEmpty(),
                "Deben detectarse duplicaciones entre métodos con la misma lógica");
        assertTrue(resultado.get(0).getSimilitud() >= 0.60f,
                "La similitud detectada debe superar el umbral configurado");
    }

    @Test
    @DisplayName("C1-T2: Métodos con lógica distinta no generan duplicación")
    void analizar_metodosDistintos_noDetectaDuplicacion() {
        analizador.cargarDesdeCodigo(CODIGO_SIN_DUPLICACION);
        List<Duplicacion> resultado = analizador.analizar();

        assertTrue(resultado.isEmpty(),
                "No deben reportarse duplicaciones entre métodos con lógica diferente");
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 2 — Detecta duplicación aunque los nombres de variables difieran
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C2-T3: Detecta duplicación aunque los nombres de variables difieran")
    void analizar_mismaLogicaDistintosNombres_detectaDuplicacion() {
        analizador.cargarDesdeCodigo(CODIGO_MISMA_LOGICA_DISTINTOS_NOMBRES);
        List<Duplicacion> resultado = analizador.analizar();

        assertFalse(resultado.isEmpty(),
                "Debe detectar duplicación aunque los nombres de variables sean distintos");
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 3 — El resultado se ordena de mayor a menor similitud
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C3-T4: El resultado se ordena por similitud descendente")
    void analizar_resultadoOrdenadoPorSimilitudDescendente() {
        analizador.cargarDesdeCodigo(CODIGO_TRES_METODOS_SIMILARES);
        List<Duplicacion> resultado = analizador.analizar();

        assertTrue(resultado.size() >= 2, "Deben encontrarse al menos 2 duplicaciones");
        for (int i = 0; i < resultado.size() - 1; i++) {
            assertTrue(
                    resultado.get(i).getSimilitud() >= resultado.get(i + 1).getSimilitud(),
                    "Cada elemento debe tener similitud >= que el siguiente"
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 4 — Maneja código vacío o sin métodos sin lanzar excepciones
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C4-T5: Código vacío retorna lista vacía sin excepción")
    void analizar_codigoVacio_retornaListaVaciaSinExcepcion() {
        analizador.cargarDesdeCodigo("");
        List<Duplicacion> resultado = assertDoesNotThrow(() -> analizador.analizar(),
                "Analizar código vacío no debe lanzar excepciones");
        assertTrue(resultado.isEmpty(),
                "Con código vacío no hay módulos y por tanto no hay duplicaciones");
    }

    @Test
    @DisplayName("C4-T6: Código sin bloques no genera módulos ni duplicaciones")
    void analizar_codigoSinBloques_noGeneraModulosNiDuplicaciones() {
        analizador.cargarDesdeCodigo("int x = 5;\nString s = \"hola\";");

        assertTrue(analizador.getModulos().isEmpty(),
                "Código sin clases/métodos no debe generar módulos");
        assertTrue(analizador.analizar().isEmpty(),
                "Sin módulos no puede haber duplicaciones");
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 5 — El modelo Modulo expone su información correctamente
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C5-T7: Modulo almacena y expone nombre, tipo y rango de líneas")
    void modulo_getters_devuelvenDatosCorrectos() {
        List<String> bloques = Arrays.asList(
                "double VAR = VAR * VAR;",
                "if (VAR > NUM) {",
                "return VAR;"
        );
        Modulo modulo = new Modulo("calcularTotal", "metodo", 10, 15, bloques);

        assertEquals("calcularTotal", modulo.getNombre());
        assertEquals("metodo",        modulo.getTipo());
        assertEquals(10,              modulo.getLineaInicio());
        assertEquals(15,              modulo.getLineaFin());
        assertEquals(3,               modulo.extraerBloques().size());
    }

    @Test
    @DisplayName("C5-T8: Modulo.toString incluye tipo, nombre y líneas")
    void modulo_toString_incluyeInformacionEsencial() {
        Modulo modulo = new Modulo("miMetodo", "metodo", 5, 12,
                Arrays.asList("int VAR = NUM;", "return VAR;"));

        String resultado = modulo.toString();
        assertTrue(resultado.contains("miMetodo"), "Debe contener el nombre");
        assertTrue(resultado.contains("metodo"),   "Debe contener el tipo");
        assertTrue(resultado.contains("5"),        "Debe contener la línea de inicio");
        assertTrue(resultado.contains("12"),       "Debe contener la línea de fin");
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 6 — Duplicacion genera propuesta de consolidación correcta
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C6-T9: Duplicacion.getPropuesta contiene los módulos y la similitud")
    void duplicacion_getPropuesta_contieneInformacionEsencial() {
        List<String> bloques = Arrays.asList("double VAR = VAR * VAR;", "return VAR;");
        Modulo origen  = new Modulo("calcularDescuentoCliente",   "metodo", 5,  12, bloques);
        Modulo destino = new Modulo("calcularDescuentoProveedor", "metodo", 20, 27, bloques);
        Duplicacion dup = new Duplicacion(origen, destino, 0.95f);

        String propuesta = dup.getPropuesta();

        assertTrue(propuesta.contains("calcularDescuentoCliente"),
                "La propuesta debe mencionar el módulo origen");
        assertTrue(propuesta.contains("calcularDescuentoProveedor"),
                "La propuesta debe mencionar el módulo destino");
        assertTrue(propuesta.contains("95") && propuesta.contains("%"),
                "La propuesta debe mostrar la similitud formateada");
        assertTrue(propuesta.contains("PROPUESTA DE CONSOLIDACIÓN"),
                "La propuesta debe tener la cabecera correcta");
    }

    @Test
    @DisplayName("C6-T10: Duplicacion expone origen, destino y similitud correctamente")
    void duplicacion_getters_devuelvenDatosCorrectos() {
        List<String> bloques = Arrays.asList("int VAR = VAR + VAR;", "return VAR;");
        Modulo origen  = new Modulo("metodoA", "metodo", 1,  8,  bloques);
        Modulo destino = new Modulo("metodoB", "metodo", 15, 22, bloques);
        Duplicacion dup = new Duplicacion(origen, destino, 0.80f);

        assertEquals(origen,  dup.getOrigen());
        assertEquals(destino, dup.getDestino());
        assertEquals(0.80f,   dup.getSimilitud(), 0.001f);
    }
}
