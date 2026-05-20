import type { SidebarsConfig } from '@docusaurus/plugin-content-docs';

import productPages from './docs/product/_position';
import uiGuidePages from './docs/ui-guide/_position';
import featuresPages from './docs/features/_position';
import masterPages from './docs/setting-up-master-ui/_position';
import springBootStarterPages from './docs/setting-up-spring-boot-service/_position';
import morePages from './docs/more/_position';

const sidebars: SidebarsConfig = {
  productSidebar: productPages,
  uiGuideSidebar: uiGuidePages,
  featuresSidebar: featuresPages,
  masterSidebar: masterPages,
  springBootStarterSidebar: springBootStarterPages,
  moreSidebar: morePages,
};

export default sidebars;
