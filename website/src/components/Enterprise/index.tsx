
import styles from "./styles.module.css";
import { EnterpriseTopSection } from "./EnterpriseTopSection";
import { EnterpriseStack } from "./EnterpriseStack";
import { EnterpriseExtensionList } from "./EnterpriseExtensionList";
import { EnterpriseFooter } from "./EnterpriseFooter";

export const Enterprise = () => {
  return (
    <section className={styles.MainWrapper} id="enterprise">
      <div className="wrap">
        <EnterpriseTopSection />

        <div className={styles.ContentWrapper}>
          <div>
            <EnterpriseStack />
            <div className={styles.ThirdCardFooter}>
              Same surface · same MCP · one product
            </div>
          </div>
          <EnterpriseExtensionList />
        </div>

        <EnterpriseFooter />
      </div>
    </section>
  );
};
