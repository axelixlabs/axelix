import Image from 'next/image';
import LinkedinIcon from '@/assets/icons/linkedin.svg'
import EmailIcon from '@/assets/icons/email.svg'
import XIcon from '@/assets/icons/x.svg'

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
                            <Image src={LinkedinIcon} alt="Linkedin Icon" />
                        </a>
                    </li>
                    <li>
                        <a href="mailto:placeholder" target="_blank" rel="noopener noreferrer">
                            <Image src={EmailIcon} alt="Email Icon" />
                        </a>
                    </li>
                    <li>
                        <a href="#" target="_blank" rel="noopener noreferrer">
                            <Image src={XIcon} alt="X Icon" />
                        </a>
                    </li>
                </ul>
            </nav>
        </footer>
    )
}