import { EmailIcon, LinkedinIcon, XIcon } from '@/assets';
import styles from './styles.module.css'

export const Footer = () => {
    const currentYear = new Date().getFullYear().toString();

    return (
        <footer className={`MainContainer ${styles.MainWrapper}`}>
            <div>
                © <time dateTime={currentYear}>{currentYear}</time> Axelix Labs. All rights reserved.
            </div>
            <nav>
                <ul className={styles.SocialMediaWrapper}>
                    <li>
                        <a href="#" target="_blank" rel="noopener noreferrer">
                            <LinkedinIcon />
                        </a>
                    </li>
                    <li>
                        <a href="mailto:placeholder" target="_blank" rel="noopener noreferrer">
                            <EmailIcon />
                        </a>
                    </li>
                    <li>
                        <a href="#" target="_blank" rel="noopener noreferrer">
                            <XIcon />
                        </a>
                    </li>
                </ul>
            </nav>
        </footer>
    )
}