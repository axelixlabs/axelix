"use client";
import Link from "next/link";
import { type ReactNode, useEffect, useRef, useState } from "react";
import { AiInstallPrompt } from "../AiInstallPrompt";
import { Logo } from "../Logo";
import { GITHUB_URL } from "@/lib/blog-metadata";
import styles from "./styles.module.css";

// Mirrors the marketing site's header (website/src/components/Header).
// Links are absolute because the blog is served under its own basePath.
const SITE_URL = "https://axelix.io";
const DOCS_URL = `${SITE_URL}/docs/product/introduction`;
const GET_STARTED_HREF = `${SITE_URL}/#install`;
const ENTERPRISE_HREF = `${SITE_URL}/enterprise`;

interface MenuLink {
  title: string;
  description: string;
  href: string;
  internal?: boolean;
}

interface Menu {
  key: string;
  label: string;
  links: MenuLink[];
}

const MENUS: Menu[] = [
  {
    key: "product",
    label: "Product",
    links: [
      { title: "Why", description: "Why now, and why we build Axelix.", href: `${SITE_URL}/#problem` },
      {
        title: "What it does",
        description: "Catch code, config and library problems, and debug live services.",
        href: `${SITE_URL}/#quality`,
      },
      { title: "Install", description: "Add Axelix to a Spring Boot service.", href: GET_STARTED_HREF },
      { title: "FAQ", description: "License, safety and platform support.", href: `${SITE_URL}/#faq` },
    ],
  },
  {
    key: "resources",
    label: "Resources",
    links: [
      { title: "Docs", description: "Guides and reference.", href: DOCS_URL },
      { title: "Blog", description: "Engineering articles from the team.", href: "/", internal: true },
    ],
  },
  {
    key: "company",
    label: "Company",
    links: [
      { title: "About", description: "Who builds Axelix and why.", href: `${SITE_URL}/about` },
      { title: "Community", description: "Contribute, events and talks.", href: `${SITE_URL}/community` },
      { title: "Contact", description: "Email, issues and security reports.", href: `${SITE_URL}/contact` },
    ],
  },
];

// Grace period before the menu closes once the pointer leaves the header.
const CLOSE_DELAY_MS = 180;

const MenuLinkItem = ({ link, className, onClick, children }: {
  link: MenuLink;
  className: string;
  onClick: () => void;
  children: ReactNode;
}) =>
  link.internal ? (
    <Link href={link.href} className={className} onClick={onClick}>
      {children}
    </Link>
  ) : (
    <a href={link.href} className={className} onClick={onClick}>
      {children}
    </a>
  );

const Chevron = () => (
  <svg
    className={styles.Caret}
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <path d="M6 9l6 6l6 -6" />
  </svg>
);

const ExternalLinks = ({ fullWidth, onLinkClick }: { fullWidth?: boolean; onLinkClick?: () => void }) => {
  const wide = fullWidth ? styles.LinkFullWidth : "";

  return (
    <div className={`${styles.ExternalLinks} ${fullWidth ? styles.ExternalLinksFullWidth : ""}`}>
      {/* The burger menu already lists Docs and Blog under Resources. */}
      {!fullWidth && (
        <>
          <a href={DOCS_URL} className={styles.TextLink} onClick={onLinkClick}>
            Docs
          </a>
          <Link href="/" className={`${styles.TextLink} ${styles.TextLinkActive}`} onClick={onLinkClick}>
            Blog
          </Link>
        </>
      )}
      <a
        href={GITHUB_URL}
        target="_blank"
        rel="noopener noreferrer"
        className={`${styles.GitHubLink} ${wide}`}
        onClick={onLinkClick}
      >
        ★ GitHub
      </a>
      <a href={GET_STARTED_HREF} className={`${styles.GetStartedLink} ${wide}`} onClick={onLinkClick}>
        Get started
      </a>
    </div>
  );
};

const BurgerMenu = () => {
  const [isOpen, setIsOpen] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const handleClickOutside = (e: MouseEvent) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);

  const close = () => setIsOpen(false);

  return (
    <div ref={wrapperRef} className={styles.Burger}>
      <button
        type="button"
        className={styles.BurgerButton}
        aria-label="Menu"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((prev) => !prev)}
      >
        <span className={`${styles.Line} ${isOpen ? styles.Line1Open : ""}`} />
        <span className={`${styles.Line} ${isOpen ? styles.Line2Open : ""}`} />
        <span className={`${styles.Line} ${isOpen ? styles.Line3Open : ""}`} />
      </button>

      {isOpen && (
        <div className={styles.MobileMenu}>
          <AiInstallPrompt />
          <div className={styles.Divider} />
          <nav className={styles.MobileLinks}>
            {MENUS.map(({ key, label, links }) => (
              <div key={key} className={styles.Group}>
                <span className={styles.GroupHeading}>{label}</span>
                {links.map((link) => (
                  <MenuLinkItem key={link.title} link={link} className={styles.MobileLink} onClick={close}>
                    {link.title}
                  </MenuLinkItem>
                ))}
              </div>
            ))}
            <a href={ENTERPRISE_HREF} className={styles.MobileLink} onClick={close}>
              Enterprise
            </a>
          </nav>
          <div className={styles.Divider} />
          <ExternalLinks onLinkClick={close} fullWidth />
        </div>
      )}
    </div>
  );
};

export const TopNav = () => {
  const [openKey, setOpenKey] = useState<string | null>(null);
  const headerRef = useRef<HTMLElement>(null);
  const closeTimer = useRef<number | undefined>(undefined);

  const cancelClose = () => window.clearTimeout(closeTimer.current);

  const openMenu = (key: string) => {
    cancelClose();
    setOpenKey(key);
  };

  const closeMenu = (delay: number) => {
    cancelClose();
    closeTimer.current = window.setTimeout(() => setOpenKey(null), delay);
  };

  useEffect(() => {
    if (!openKey) {
      return;
    }

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        setOpenKey(null);
      }
    };

    const handlePointerDown = (e: PointerEvent) => {
      if (headerRef.current && !headerRef.current.contains(e.target as Node)) {
        setOpenKey(null);
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    document.addEventListener("pointerdown", handlePointerDown);
    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.removeEventListener("pointerdown", handlePointerDown);
    };
  }, [openKey]);

  useEffect(() => () => window.clearTimeout(closeTimer.current), []);

  return (
    <header
      ref={headerRef}
      className={styles.Top}
      onMouseEnter={cancelClose}
      onMouseLeave={() => closeMenu(CLOSE_DELAY_MS)}
      onBlur={(e) => {
        if (!e.currentTarget.contains(e.relatedTarget)) {
          setOpenKey(null);
        }
      }}
    >
      <div className={styles.Bar}>
        <a href={`${SITE_URL}/`} className={styles.Brand} aria-label="Axelix home" onMouseEnter={() => closeMenu(0)}>
          <Logo />
        </a>
        <nav className={styles.Nav}>
          {MENUS.map(({ key, label, links }) => {
            const isOpen = key === openKey;

            return (
              <div key={key} className={styles.NavItemWrapper}>
                <button
                  type="button"
                  aria-expanded={isOpen}
                  className={`${styles.NavItem} ${isOpen ? styles.NavItemOpen : ""}`}
                  onMouseEnter={() => openMenu(key)}
                  onClick={() => openMenu(key)}
                >
                  {label}
                  <Chevron />
                </button>
                {isOpen && (
                  <div className={styles.CompactMenu}>
                    {links.map((link) => (
                      <MenuLinkItem
                        key={link.title}
                        link={link}
                        className={styles.MenuLink}
                        onClick={() => setOpenKey(null)}
                      >
                        <span className={styles.MenuLinkTitle}>{link.title}</span>
                        <span className={styles.MenuLinkDescription}>{link.description}</span>
                      </MenuLinkItem>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
          <a href={ENTERPRISE_HREF} className={styles.NavItem} onMouseEnter={() => closeMenu(0)}>
            Enterprise
          </a>
        </nav>
        <div className={styles.ExternalLinksWrapper} onMouseEnter={() => closeMenu(0)}>
          <ExternalLinks />
        </div>
        <BurgerMenu />
      </div>
    </header>
  );
};
