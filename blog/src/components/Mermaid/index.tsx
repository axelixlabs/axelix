import { CodeBlock, Pre } from "fumadocs-ui/components/codeblock";
import { renderMermaidSVG } from "beautiful-mermaid";

interface IProps {
  chart: string;
}

/**
 * Renders a ```mermaid block — emitted as `<Mermaid chart="…" />` by
 * `remarkMdxMermaid` (see source.config.ts) — to an inline SVG at build time.
 * Falls back to a plain code block if the diagram fails to parse.
 */
export const Mermaid = ({ chart }: IProps) => {
  try {
    const svg = renderMermaidSVG(chart, {
      bg: "var(--color-fd-background)",
      fg: "var(--color-fd-foreground)",
      transparent: true,
      interactive: false,
    });
    return (
      <div
        className="mermaid"
        style={{ width: "100%", overflow: "hidden" }}
        dangerouslySetInnerHTML={{ __html: svg }}
      />
    );
  } catch {
    return (
      <CodeBlock title="Mermaid">
        <Pre>{chart}</Pre>
      </CodeBlock>
    );
  }
};
