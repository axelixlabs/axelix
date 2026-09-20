import { i18n } from "@/lib/i18n";
import { docsLlms } from "@/lib/source";

export const revalidate = false;

export async function GET(_req: Request, { params }: RouteContext<"/[lang]/llms-full.txt">) {
    const { lang } = await params;

    return new Response(await docsLlms.full(lang));
}

export function generateStaticParams() {
    return i18n.languages.map((lang) => ({ lang }));
}
