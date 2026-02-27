import { StaticImageData } from "next/image";
import { ELanguage } from "@/models";
import { ReactNode } from "react";

export interface IWhatCanDoCardData {
    image: string;
    title: string;
    description: string;
    category: string;
    documentationLink: string;
}

export interface IZigzagSectionData {
    title: string,
    description: string;
    href: string,
    image: StaticImageData
}

export interface IInstallationStep {

    /**
     * The name of the step.
     */
    name: string;

    /**
     * The code samples groups that are used for illustration purposes.
     */
    codeSampleGroups: ICodeSampleGroup[];
}

export interface ICodeSampleGroup {

    /**
     * The group of the code samples is something that the elements in the provided array
     * of {@link codeSamples} have in common.
     */
    group: string;

    /**
     * The array of code samples
     */
    codeSamples: ICodeSample[];
}

export interface ICodeSample {

    /**
     * The title that should be used when displaying the code sample.
     */
    codeSampleTitle: string;

    /**
     * The programming/markup language used for code snippet.
     */
    language: ELanguage;

    /**
     * The code sample associated with this code format.
     */
    codeSample: string;
}

export interface IInstallationInstructions {
    /**
     * The installation option name.
     */
    option: string;

    /**
     * The installation steps.
     */
    steps: IInstallationStep[],

    /**
     * The overall instructions about the given installation option.
     */
    description: string,
}

export interface IFAQItem {
    /**
     * FAQ question.
     */
    question: string;

    /**
     * Structured data answer (plain text).
     */
    structuredDataAnswer: string;

    /**
     * FAQ answer for UI rendering.
     */
    answer: ReactNode;
}