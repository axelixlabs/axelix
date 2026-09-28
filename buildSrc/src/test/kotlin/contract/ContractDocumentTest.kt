package contract

import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for {@link ContractDocument#starterProducedSchemas} and {@link ContractDocument#masterProducedSchemas}.
 *
 * @author Nikita Kirillov
 */
class ContractDocumentTest {

    companion object {
        private const val CURRENT_VERSION = "1.2.0-SNAPSHOT"
    }

    @Test
    fun `a single declared side splits produced schemas by bearer`() {
        val document = document("compliant.yaml")

        assertEquals(emptySet<String>(), document.starterProducedSchemas)
        assertEquals(setOf("LogLevelChangeRequest"), document.masterProducedSchemas)
    }

    @Test
    fun `declaring starter and master together produces every schema for each`() {
        val document = document("stateless/ServerIsDeclared/server-declared-as-list.yaml")

        assertEquals(setOf("LogLevelChangeRequest"), document.starterProducedSchemas)
        assertEquals(setOf("LogLevelChangeRequest"), document.masterProducedSchemas)
    }

    private fun document(name: String): ContractDocument =
        ContractDocument.parse(File(javaClass.getResource("/contract/$name")!!.toURI()), CURRENT_VERSION)
}
