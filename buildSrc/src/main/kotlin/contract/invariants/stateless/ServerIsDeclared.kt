package contract.invariants.stateless

import contract.ContractDocument
import contract.ContractDocument.Companion.SERVER
import contract.ContractDocument.Companion.SERVER_SIDES

/**
 * The document must declare which side answers the call: the producer of every payload — and
 * with it the applicable evolution rules — is derived from it.
 *
 * @author Mikhail Polivakha
 */
object ServerIsDeclared : StatelessContractInvariant {

    override fun check(document: ContractDocument): List<String> =
        if (document.sides.isNotEmpty() && document.sides.all { side -> side in SERVER_SIDES }) emptyList()
        else listOf("the 'info' block must declare '$SERVER' as one of $SERVER_SIDES, "
            + "or as a list combining them")
}
