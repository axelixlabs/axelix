import useBaseUrl from '@docusaurus/useBaseUrl';
import ThemedImage from '@theme/ThemedImage';
import styles from './styles.module.css'

export const TargetThemeIcon = () => {
  return (
    <ThemedImage
      alt="Target icon"
      className={styles.TargetIcon}
      width={15}
      height={15}
      sources={{
        dark: useBaseUrl('/img/feature/icons/target-light.svg'),
        light: useBaseUrl('/img/feature/icons/target-dark.svg'),
      }}
    />
  );
}