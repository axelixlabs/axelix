import {Light as SyntaxHighlighter} from "react-syntax-highlighter";

import {atomOneDark} from "react-syntax-highlighter/dist/esm/styles/hljs";
import properties from "react-syntax-highlighter/dist/esm/languages/hljs/properties";
import yaml from 'react-syntax-highlighter/dist/esm/languages/hljs/yaml';
import xml from 'react-syntax-highlighter/dist/esm/languages/hljs/xml';
import kotlin from 'react-syntax-highlighter/dist/esm/languages/hljs/kotlin';
import bash from 'react-syntax-highlighter/dist/esm/languages/hljs/bash';
import groovy from 'react-syntax-highlighter/dist/esm/languages/hljs/groovy';

import {ELanguage} from "@/models";
import dedent from "dedent";

SyntaxHighlighter.registerLanguage(ELanguage.PROPERTIES, properties);
SyntaxHighlighter.registerLanguage(ELanguage.YAML, yaml);
SyntaxHighlighter.registerLanguage(ELanguage.XML, xml);
SyntaxHighlighter.registerLanguage(ELanguage.KOTLIN, kotlin);
SyntaxHighlighter.registerLanguage(ELanguage.SHELL, bash);
SyntaxHighlighter.registerLanguage(ELanguage.GROOVY, groovy);

interface IProps {

    /**
     * The language of the code
     */
    language: ELanguage,

    /**
     * The actual code snippet.
     */
    codeSnippet: string
}

export const CodeSnippet = ({ language, codeSnippet } : IProps ) => {
    return (
        <SyntaxHighlighter
            language={language}
            style={atomOneDark}
            showLineNumbers
            customStyle={{
                backgroundColor: '#0d1116',
            }}
        >
            {dedent(codeSnippet)}
        </SyntaxHighlighter>
    );
}