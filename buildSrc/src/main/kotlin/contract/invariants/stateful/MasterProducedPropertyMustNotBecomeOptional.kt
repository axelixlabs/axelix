package contract.invariants.stateful

import contract.ContractDocument

/**
 * A 'required' property of a master-produced payload is a promise every released starter builds
 * on: the Master always sends it. Withdrawing 'required' is therefore a hidden partial removal —
 * the Master reserves the right to stop sending the property while starters within the
 * compatibility window still rely on its presence. The regulation knows only one way to stop
 * sending a master-produced property: deprecate it, keep sending it, and remove it once the
 * window has passed.
 *
 * Starter-produced payloads are free to weaken 'required': the Master is never older than the
 * starter it talks to, so its model already tolerates the absence.
 *
 * @author Mikhail Polivakha
 */
object MasterProducedPropertyMustNotBecomeOptional : StatefulContractInvariant {

    override fun check(baseline: ContractDocument?, current: ContractDocument): List<String> {
        if (baseline == null) {
            // a document born now promised nothing: no released starter relies on its properties
            return emptyList()
        }

        val baselineProperties = baseline.properties
            .associateBy { property -> property.schemaName to property.name }

        return current.properties.mapNotNull { currentProperty ->
            val released = baselineProperties[currentProperty.schemaName to currentProperty.name]
                ?: return@mapNotNull null

            if (released.required && !currentProperty.required
                && currentProperty.schemaName in current.masterProducedSchemas) {
                ("${currentProperty.location} is produced by the Master and is 'required' in the released "
                    + "contract (${baseline.axelixVersion}): withdrawing 'required' is a hidden removal, "
                    + "deprecate the property instead and keep it required until the window passes")
            } else {
                null
            }
        }
    }
}
