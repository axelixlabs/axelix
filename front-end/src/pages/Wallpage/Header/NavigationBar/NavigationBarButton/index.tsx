import styles from "./styles.module.css";

interface NavigationBarButtonProps {
  text: string;
}

export const NavigationBarButton = (props: NavigationBarButtonProps) => {
  return (
    <div>
      <button className={styles.NavigationButton}>{props.text}</button>
    </div>
  );
};
