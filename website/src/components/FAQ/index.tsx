import styles from "./styles.module.css";
import { FAQFooter } from "./FAQFooter";
import { FAQTopSection } from "./FAQTopSection";
import { FAQList } from "./FAQList";

export const FAQ = () => {
  return (
    <section className={styles.Faq} id="faq">
      <div className="wrap">
        <FAQTopSection />
        <FAQList />
        <FAQFooter />
      </div>
    </section>
  );
}
