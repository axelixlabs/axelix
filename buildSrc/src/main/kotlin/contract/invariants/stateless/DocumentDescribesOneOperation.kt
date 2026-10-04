package contract.invariants.stateless

import contract.ContractDocument

/**
 * Every contract document must describe exactly one operation: the document is the unit of
 * versioning, and a version of a document holding several operations cannot honestly represent
 * a change to just one of them.
 *
 * @author Mikhail Polivakha
 */
object DocumentDescribesOneOperation : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> =
        when {
            document.operations.isEmpty() ->
                listOf("the document must describe exactly one operation, but describes none")
            document.operations.size > 1 ->
                listOf("the document must describe exactly one operation, but describes "
                    + "${document.operations.size}: ${document.operations.joinToString(", ")}")
            else -> emptyList()
        }
}
