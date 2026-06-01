import { FAQFooter } from "./FAQFooter";
import { FAQList } from "./FAQList";
import { FAQTopSection } from "./FAQTopSection";
import styles from "./styles.module.css";

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
};
