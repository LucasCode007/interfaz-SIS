import org.example.model.Estado;
import org.example.model.TestCase;
import org.example.model.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class tddTest {

    private UserStory userStory;
    private TestCase testSuma;
    private TestCase testContar;

    @BeforeEach
    void setUp() {
        userStory  = new UserStory("US-01", "Gestión de tareas");
        // Ahora las entradas y salidas se pasan como String
        testSuma   = new TestCase("Verificar suma", "2", "4", userStory);
        testContar = new TestCase("Contar caracteres", "hola", "4", userStory);
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 1 — El developer puede crear un TestCase con
    //               nombre, entrada esperada y salida esperada
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C1-T1: TestCase guarda el nombre correctamente")
    void testCase_debeGuardarNombre() {
        assertEquals("Verificar suma", testSuma.getNombre());
    }

    @Test
    @DisplayName("C1-T2: TestCase guarda la entrada esperada (tipo String)")
    void testCase_debeGuardarEntradaEsperadaString() {
        assertEquals("2", testSuma.getEntradaEsperada());
    }

    @Test
    @DisplayName("C1-T3: TestCase guarda la salida esperada (tipo String)")
    void testCase_debeGuardarSalidaEsperadaString() {
        assertEquals("4", testContar.getSalidaEsperada());
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 2 — Cada test se vincula obligatoriamente
    //               a una User Story
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C2-T4: TestCase creado sin UserStory lanza excepción")
    void testCase_sinUserStory_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                new TestCase("Sin story", "entrada", "salida", null)
        );
    }

    @Test
    @DisplayName("C2-T5: TestCase queda vinculado a su UserStory")
    void testCase_debeEstarVinculadoAUserStory() {
        assertEquals(userStory, testSuma.getUserStory());
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 3 — Los estados posibles son:
    //               Red (falla), Green (pasa), Refactorizado
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C3-T6: El estado inicial de un TestCase es RED")
    void testCase_estadoInicial_debeSerRed() {
        assertEquals(Estado.RED, testSuma.getEstado());
    }

    @Test
    @DisplayName("C3-T7: El estado se puede actualizar a GREEN")
    void testCase_debePermitirCambioAGreen() {
        testSuma.actualizarEstado(Estado.GREEN);
        assertEquals(Estado.GREEN, testSuma.getEstado());
    }

    @Test
    @DisplayName("C3-T8: El estado se puede actualizar a REFACTORIZADO")
    void testCase_debePermitirCambioARefactorizado() {
        testSuma.actualizarEstado(Estado.REFACTORIZADO);
        assertEquals(Estado.REFACTORIZADO, testSuma.getEstado());
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 4 — El sistema bloquea completar una UserStory
    //               si algún test no está en Green
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C4-T9: UserStory no puede completarse si hay tests en RED")
    void userStory_conTestEnRed_noPuedeCompletarse() {
        userStory.agregarTestCase(testSuma); // RED por defecto
        assertFalse(userStory.completar());
    }

    @Test
    @DisplayName("C4-T10: UserStory puede completarse si todos los tests están en GREEN")
    void userStory_conTodosEnGreen_puedeCompletarse() {
        testSuma.actualizarEstado(Estado.GREEN);
        userStory.agregarTestCase(testSuma);
        assertTrue(userStory.completar());
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 5 — Se muestra el conteo de tests por estado
    //               en cada UserStory
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C5-T11: contarPorEstado devuelve correctamente los tests en RED")
    void userStory_debeContarTestsEnRed() {
        userStory.agregarTestCase(testSuma); // RED por defecto
        assertEquals(1, userStory.contarPorEstado(Estado.RED));
    }

    @Test
    @DisplayName("C5-T12: contarPorEstado devuelve correctamente los tests en GREEN")
    void userStory_debeContarTestsEnGreen() {
        testSuma.actualizarEstado(Estado.GREEN);
        userStory.agregarTestCase(testSuma);
        assertEquals(1, userStory.contarPorEstado(Estado.GREEN));
    }
}