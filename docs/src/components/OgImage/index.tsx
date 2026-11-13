import { OG_FONT_NAME } from "@/lib/shared";

/** Size of an OG image, as recommended by the Open Graph protocol. */
export const OG_IMAGE_WIDTH = 1200;
export const OG_IMAGE_HEIGHT = 630;

/*
 * Rendered by Satori (`next/og`), so the styles are inline, every element with more than
 * one child is `display: flex`, and colours are literals: Satori resolves no CSS variables.
 */
const COLORS = {
    /** `--color-fd-background` of the dark theme in `src/app/global.css` */
    background: "#141414",
    /** `--color-fd-muted-foreground` of the dark theme */
    muted: "#cccccc",
    // the card's own: pure white for the title, a dimmed white for the address
    text: "#ffffff",
    faint: "rgba(255, 255, 255, 0.45)",
    /** The brand green `#00ab55` (see `global.css`), fading out towards the top left. */
    glow: "radial-gradient(circle at 100% 100%, rgba(0, 171, 85, 0.5) 0%, rgba(0, 171, 85, 0) 80%)",
};

const PADDING = 72;
// lifts the text block off the bottom edge
const PADDING_BOTTOM = 170;
// keeps the 186.19 × 49.5 viewBox of `public/img/logo-dark.svg`
const LOGO_WIDTH = 150;
const LOGO_HEIGHT = 40;

/**
 * Step the title size down as it grows, so a long title still fits in three lines.
 *
 * @param title - the page title
 * @returns the font size in pixels
 */
function getTitleFontSize(title: string) {
    if (title.length <= 30) return 68;
    if (title.length <= 60) return 56;

    return 46;
}

type OgImageProps = {
    title: string;
    description?: string;
    /** The page address to print under the description, e.g. `axelix.io/docs/start`. */
    url: string;
    /** The Axelix wordmark, from `loadOgAssets`. */
    logoSrc: string;
};

/**
 * The Open Graph card of a documentation page: a dark surface with a brand-green glow,
 * the wordmark at the top, and the title, description and address at the bottom.
 *
 * TODO: custom — replaces the default template of Fumadocs' `generateOGImage`.
 */
export const OgImage = ({ title, description, url, logoSrc }: OgImageProps) => (
    <div
        style={{
            display: "flex",
            flexDirection: "column",
            justifyContent: "space-between",
            width: "100%",
            height: "100%",
            padding: PADDING,
            paddingBottom: PADDING_BOTTOM,
            backgroundColor: COLORS.background,
            backgroundImage: COLORS.glow,
            color: COLORS.text,
            fontFamily: OG_FONT_NAME,
        }}
    >
        <img src={logoSrc} alt="Axelix" width={LOGO_WIDTH} height={LOGO_HEIGHT} />

        <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
            {/* `lineClamp` works in Satori only on `display: block` */}
            <div
                style={{
                    display: "block",
                    fontSize: getTitleFontSize(title),
                    fontWeight: 500,
                    lineHeight: 1.1,
                    letterSpacing: "-0.015em",
                    lineClamp: 3,
                }}
            >
                {title}
            </div>
            {description && (
                <div
                    style={{
                        display: "block",
                        fontSize: 26,
                        fontWeight: 400,
                        lineHeight: 1.45,
                        color: COLORS.muted,
                        lineClamp: 2,
                    }}
                >
                    {description}
                </div>
            )}
            <div style={{ fontSize: 20, fontWeight: 400, color: COLORS.faint }}>{url}</div>
        </div>
    </div>
);
