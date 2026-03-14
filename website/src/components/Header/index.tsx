"use client"
import { GithubIcon, LogoIcon } from "@/assets";
import { NavigationBar } from "./NavigationBar";
import styles from "./styles.module.css";

import { useState } from "react";

export const Header = () => {
  const [isBurgerOpen, setIsBurgerOpen] = useState<boolean>(false)

  return (
    <header className={styles.MainWrapper}>
      <div className={`MainContainer ${styles.InnerWrapper}`}>
        <LogoIcon className={styles.Logo} />
        <div className={styles.LinksAndSigninWrapper}>
          <div className={styles.DesktopMenuWrapper}>
            <NavigationBar />
          </div>

          <a href="https://github.com/axelixlabs/axelix" target="_blank" rel="noopener noreferrer" className={styles.GitHubWrapper}>
            <GithubIcon />
            GitHub
          </a>
        </div>
        <div className={`MainContainer ${styles.MobileMenuWrapper} ${isBurgerOpen ? styles.MobileMenuOpened : ""}`}>
          <NavigationBar />
        </div>
        <div className={`${styles.Burger} ${isBurgerOpen ? styles.OpenedBurger : ""}`} onClick={() => setIsBurgerOpen(!isBurgerOpen)}>
          <span className={styles.BurgerCenterLine} />
        </div>
      </div>
    </header>
  );
};
