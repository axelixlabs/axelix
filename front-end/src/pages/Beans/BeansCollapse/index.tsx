import { useRef, useState } from "react";
import { Collapse, type CollapseProps } from "antd";

import type { IBean, IBeansCollapseHeaderRefs } from "models";

import { BeanCollapseChildren } from "./BeanCollapseChildren";
import { BeanCollapseLabels } from "./BeanCollapseLabels";

interface IProps {
  /**
   * List of beans: full or filtered
   */
  beans: IBean[];
}

export const BeansCollapse = ({ beans }: IProps) => {
  const [activeKey, setActiveKey] = useState<string | string[]>([]);

  const headerRefs = useRef<IBeansCollapseHeaderRefs>({});

  const createCollapseItems = (beans: IBean[]): CollapseProps["items"] => {
    return beans.map((bean) => ({
      key: bean.beanName,
      label: <BeanCollapseLabels bean={bean} headerRefs={headerRefs} />,
      children: (
        <BeanCollapseChildren
          beans={beans}
          bean={bean}
          headerRefs={headerRefs}
          setActiveKey={setActiveKey}
        />
      ),
    }));
  };

  return (
    <Collapse
      accordion
      activeKey={activeKey}
      items={createCollapseItems(beans)}
      onChange={(key) => setActiveKey(key)}
      bordered
      className="Collapse"
    />
  );
};
