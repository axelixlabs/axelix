import { ICodeSampleGroup } from "@/models"
import { enSequence } from "@/utils";

export const getInstallationSelectOptions = (codeSampleGroups: ICodeSampleGroup[]) => {
    return codeSampleGroups.map(({ group }) => group).map((format) => ({
        label: format,
        value: format
    }))
}

export const createSeoKeywordsFromSequence = enSequence
    .filter((item): item is string => typeof item === 'string')
    .map((word) => word.replace('!', ''))
    .join(', ');