export interface IBean {
  beanName: string;
  scope: string;
  className: string;
  aliases: string[];
  dependencies: string[];
}

export interface IBeansSliceData {
  loading: boolean;
  error: string;
  beans: IBean[];
  beansSearchText: string;
  filteredBeans: IBean[];
}
