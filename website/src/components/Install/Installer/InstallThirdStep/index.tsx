import { CfgVariant, Method } from "../../../../../models"
import { K8sPropertiesSnippet } from "../Snippets/K8sPropertiesSnippet";
import { K8sYamlSnippet } from "../Snippets/K8sYamlSnippet";
import { PropertiesSnippet } from "../Snippets/PropertiesSnippet";
import { YamlSnippet } from "../Snippets/YamlSnippet";

interface IProps {
    method: Method;
    cfg: CfgVariant;
    activeSnippetRef: any
}

export const InstallThirdStep = ({ method, cfg, activeSnippetRef }: IProps) => {
    if (method === "k8s" && cfg === "yaml") {
        return <K8sYamlSnippet refEl={activeSnippetRef} />
    }

    if (method === "k8s" && cfg === "properties") {
        return <K8sPropertiesSnippet refEl={activeSnippetRef} />
    }

    if (cfg === "yaml") {
        return <YamlSnippet refEl={activeSnippetRef} />
    }

    return <PropertiesSnippet refEl={activeSnippetRef} />
} 