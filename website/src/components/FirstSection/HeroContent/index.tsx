import { TypeAnimation } from 'react-type-animation';

import styles from "./styles.module.css"
import Link from 'next/link';
import { enSequence } from '@/utils';
import { createSeoKeywordsFromSequence } from '@/helpers';

export const HeroContent = () => {

    return (
        <div className="MainContainer">
            <div className={styles.TitlesWrapper}>
                <h1 className='VisuallyHidden'>
                    Axelix makes Spring Boot {createSeoKeywordsFromSequence}
                </h1>
                <div className={`TextLarge ${styles.Title}`}>
                    Axelix makes Spring Boot <TypeAnimation sequence={enSequence} repeat={Infinity} />
                </div>
                <p className={`TextMedium ${styles.SubTitle}`}>
                    Open Source software to monitor, test and debug your Spring Boot deployments at scale
                </p>
            </div>

            <div className={styles.LinksWrapper}>
                <Link
                    href="#"
                    className={styles.LinkPrimary}
                >
                    Get Started
                </Link>
                <a
                    href="#installation"
                    className={styles.LinkSecondary}
                >
                    Installation
                </a>
            </div>
        </div>
    )
}