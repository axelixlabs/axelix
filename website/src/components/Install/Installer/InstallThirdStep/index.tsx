/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { EInstallConfigurationVariant, EInstallMethod } from "@/models";

import { K8sPropertiesSnippet } from "../Snippets/K8sPropertiesSnippet";
import { K8sYamlSnippet } from "../Snippets/K8sYamlSnippet";
import { PropertiesSnippet } from "../Snippets/PropertiesSnippet";
import { YamlSnippet } from "../Snippets/YamlSnippet";

interface IProps {
    installMethod: EInstallMethod;
    installConfiguration: EInstallConfigurationVariant;
    activeSnippetRef: any;
}

export const InstallThirdStep = ({ installMethod, installConfiguration, activeSnippetRef }: IProps) => {
    if (installMethod === EInstallMethod.K8S && installConfiguration === EInstallConfigurationVariant.YAML) {
        return <K8sYamlSnippet refEl={activeSnippetRef} />;
    }

    if (installMethod === EInstallMethod.K8S && installConfiguration === EInstallConfigurationVariant.PROPERTIES) {
        return <K8sPropertiesSnippet refEl={activeSnippetRef} />;
    }

    if (installConfiguration === EInstallConfigurationVariant.YAML) {
        return <YamlSnippet refEl={activeSnippetRef} />;
    }

    return <PropertiesSnippet refEl={activeSnippetRef} />;
};
