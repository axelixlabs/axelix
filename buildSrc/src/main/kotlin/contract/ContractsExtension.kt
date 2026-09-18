package contract

import org.gradle.api.provider.Property

/**
 * Configuration of the 'contracts' convention plugin
 *
 * @author Mikhail Polivakha
 */
interface ContractsExtension {

    /**
     * The base package the generated contract classes of this module live in. The feature name
     * of the contract document is appended to it, e.g. a base package of {@code a.b.contract}
     * puts the classes of the 'logger' feature documents into {@code a.b.contract.logger}.
     */
    val modelBasePackage: Property<String>
}
