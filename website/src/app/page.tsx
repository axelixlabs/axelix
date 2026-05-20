import {
  Capabilities,
  Enterprise,
  Faq,
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
      <Faq />
    </>
  );
}
