import { AiInstallPrompt } from "@/components/AiInstallPrompt";

/** TODO: wrapper — `sidebar.banner` for `DocsLayout` (`[lang]/layout.tsx`). */
export const SidebarBanner = () => (
    <div className="xl:hidden">
        <AiInstallPrompt />
    </div>
);
