"use client"
import { useEffect, useRef } from 'react';
import { HeroContent } from './HeroContent';
import styles from "./styles.module.css"

export const FirstSection = () => {
    const videoRef = useRef<HTMLVideoElement>(null);

    useEffect(() => {
        if (videoRef.current) {
            videoRef.current.playbackRate = 0.5;
        }
    }, []);

    return (
        <section className={styles.MainWrapper}>
            <div className={styles.HeroWrapper}>
                <video
                    ref={videoRef}
                    autoPlay
                    loop
                    muted
                    playsInline
                    preload='auto'
                    // TODO: Add the poster in future
                    className={styles.HeroBackground}
                >
                    <source src="/first-section-bg.mp4" type="video/mp4" />
                </video>
            </div>

            <div className={styles.HeroContentWrapper}>
                <HeroContent />
            </div>
        </section>
    );
}