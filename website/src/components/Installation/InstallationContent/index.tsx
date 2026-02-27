"use client"
import { useState } from 'react';
import styles from './styles.module.css';
import { ICodeSampleGroup, IInstallationInstructions, IInstallationStep } from '@/models';
import { Copy } from '@/components';
import { CodeSnippet } from "./CodeSnippet";
import { getInstallationSelectOptions } from '@/helpers';
import { Select } from 'antd';

interface IProps {
    instructions: IInstallationInstructions
}

export const InstallationContent = ({ instructions }: IProps) => {
    const steps = instructions.steps;

    const firstStep = steps[0]

    const [currentStep, setCurrentStep] = useState<IInstallationStep>(firstStep);
    const [selectedGroup, setSelectedGroup] = useState<ICodeSampleGroup>(currentStep.codeSampleGroups[0])

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.HeaderBar}>
                <ul className={styles.Tabs}>
                    {steps.map((step, index) => (
                        <li key={index + step.name}>
                            <button
                                type="button"
                                onClick={() => {
                                    setCurrentStep(step)
                                    setSelectedGroup(step.codeSampleGroups[0])
                                }}
                                className={`${styles.Tab} ${currentStep === step ? styles.ActiveTab : ''}`}
                            >
                                {index + 1}. {step.name}
                            </button>
                        </li>
                    ))}
                </ul>

                {
                    currentStep.codeSampleGroups.length > 1 && (
                        <Select
                            value={selectedGroup.group}
                            onChange={(value) => {
                                setSelectedGroup(
                                    currentStep.codeSampleGroups.find(codeGroup => codeGroup.group === value)!
                                )
                            }}
                            options={getInstallationSelectOptions(currentStep.codeSampleGroups)}
                            className={styles.FormatSelect}
                        />
                    )
                }
            </div>

            <div className={styles.CodeArea}>
                {
                    selectedGroup.codeSamples.map(({ codeSample, codeSampleTitle, language }) => {
                        return (
                            <div key={codeSampleTitle + language} className={styles.Container}>
                                <header className={styles.CodeSamplesHeader}>
                                    <h3 className={styles.CodeSampleTitle}>{codeSampleTitle}</h3>
                                    <Copy text={codeSample} />
                                </header>
                                <CodeSnippet language={language} codeSnippet={codeSample} />
                            </div>
                        )
                    })
                }
            </div>
        </div>
    );
};
