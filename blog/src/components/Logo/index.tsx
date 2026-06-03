import { LogoIcon } from "@/assets";

/** Axelix logo. Source of truth is `src/assets/icons/logo.svg` (imported as a
 *  component via SVGR); this shim just applies the shared class. */
export const Logo = () => {
  return <LogoIcon className="logo" aria-hidden="true" />;
};
