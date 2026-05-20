import {
  Capabilities,
  Enterprise,
  FAQ,
  Hero,
  Install,
  Problem,
  Strip,
} from "@/components";

export default function Page() {
  return (
    <>
      <Hero />
      <Strip />
      <Problem />
      <Capabilities />
      <Install />
      <Enterprise />
      <FAQ />
    </>
  );
}
