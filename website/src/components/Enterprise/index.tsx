import { EnterpriseExtensionList } from "./EnterpriseExtensionList";
import { EnterpriseFooter } from "./EnterpriseFooter";
import { EnterpriseStack } from "./EnterpriseStack";
import { EnterpriseTopSection } from "./EnterpriseTopSection";
import styles from "./styles.module.css";

export const Enterprise = () => {
    return (
        <section className={styles.MainWrapper} id="enterprise">
            <div className="wrap">
                <EnterpriseTopSection />

                <div className={styles.ContentWrapper}>
                    <div>
                        <EnterpriseStack />
                        <div className={styles.ThirdCardFooter}>Same surface · same MCP · one product</div>
                    </div>
                    <EnterpriseExtensionList />
                </div>

                <EnterpriseFooter />
            </div>
        </section>
    );
};
