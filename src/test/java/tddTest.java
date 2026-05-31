import org.example.Estado;
import org.example.TestCase;
import org.example.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class tddTest {

    private UserStory userStory;
    private TestCase<Integer, Integer> testEnteroEntero;
    private TestCase<String, Integer>  testStringEntero;

    @BeforeEach
    void setUp() {
        userStory          = new UserStory("US-01", "Gestión de tareas");
        testEnteroEntero   = new TestCase<>("Verificar suma",    2,      4,    userStory);
        testStringEntero   = new TestCase<>("Contar caracteres", "hola", 4,    userStory);
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 1 — El developer puede crear un TestCase con
    //               nombre, entrada esperada y salida esperada
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C1-T1: TestCase guarda el nombre correctamente")
    void testCase_debeGuardarNombre() {
        assertEquals("Verificar suma", testEnteroEntero.getNombre());
    }

    @Test
    @DisplayName("C1-T2: TestCase guarda la entrada esperada (tipo Integer)")
    void testCase_debeGuardarEntradaEsperadaInteger() {
        assertEquals(2, testEnteroEntero.getEntradaEsperada());
    }

    @Test
    @DisplayName("C1-T3: TestCase guarda la salida esperada (tipo String)")
    void testCase_debeGuardarSalidaEsperadaString() {
        assertEquals(4, testStringEntero.getSalidaEsperada());
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 2 — Cada test se vincula obligatoriamente
    //               a una User Story
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C2-T4: TestCase creado sin UserStory lanza excepción")
    void testCase_sinUserStory_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                new TestCase<>("Sin story", "entrada", "salida", null)
        );
    }

    @Test
    @DisplayName("C2-T5: TestCase queda vinculado a su UserStory")
    void testCase_debeEstarVinculadoAUserStory() {
        assertEquals(userStory, testEnteroEntero.getUserStory());
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 3 — Los estados posibles son:
    //               Red (falla), Green (pasa), Refactorizado
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C3-T6: El estado inicial de un TestCase es RED")
    void testCase_estadoInicial_debeSerRed() {
        assertEquals(Estado.RED, testEnteroEntero.getEstado());
    }

    @Test
    @DisplayName("C3-T7: El estado se puede actualizar a GREEN")
    void testCase_debePermitirCambioAGreen() {
        testEnteroEntero.actualizarEstado(Estado.GREEN);
        assertEquals(Estado.GREEN, testEnteroEntero.getEstado());
    }

    @Test
    @DisplayName("C3-T8: El estado se puede actualizar a REFACTORIZADO")
    void testCase_debePermitirCambioARefactorizado() {
        testEnteroEntero.actualizarEstado(Estado.REFACTORIZADO);
        assertEquals(Estado.REFACTORIZADO, testEnteroEntero.getEstado());
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 4 — El sistema bloquea completar una UserStory
    //               si algún test no está en Green
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C4-T9: UserStory no puede completarse si hay tests en RED")
    void userStory_conTestEnRed_noPuedeCompletarse() {
        userStory.agregarTestCase(testEnteroEntero); // RED por defecto
        assertFalse(userStory.completar());
    }

    @Test
    @DisplayName("C4-T10: UserStory puede completarse si todos los tests están en GREEN")
    void userStory_conTodosEnGreen_puedeCompletarse() {
        testEnteroEntero.actualizarEstado(Estado.GREEN);
        userStory.agregarTestCase(testEnteroEntero);
        assertTrue(userStory.completar());
    }

    // ─────────────────────────────────────────────────────────────
    // CONDICIÓN 5 — Se muestra el conteo de tests por estado
    //               en cada UserStory
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("C5-T11: contarPorEstado devuelve correctamente los tests en RED")
    void userStory_debeContarTestsEnRed() {
        userStory.agregarTestCase(testEnteroEntero); // RED por defecto
        assertEquals(1, userStory.contarPorEstado(Estado.RED));
    }

    @Test
    @DisplayName("C5-T12: contarPorEstado devuelve correctamente los tests en GREEN")
    void userStory_debeContarTestsEnGreen() {
        testEnteroEntero.actualizarEstado(Estado.GREEN);
        userStory.agregarTestCase(testEnteroEntero);
        assertEquals(1, userStory.contarPorEstado(Estado.GREEN));
    }
}