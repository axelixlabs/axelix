/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
"use client";
import { PlayIcon } from "@/assets";

import { useEffect, useRef, useState } from "react";

import styles from "./styles.module.css";

const YOUTUBE_ID = "S8vTZbxuVvc";

const POSTER_SRC = "/hero-video-poster.jpg";

const VIDEO_SRC =
    `https://www.youtube-nocookie.com/embed/${YOUTUBE_ID}` +
    `?autoplay=1&loop=1&playlist=${YOUTUBE_ID}&rel=0&modestbranding=1&playsinline=1`;

export const HeroVideo = () => {
    const [playing, setPlaying] = useState<boolean>(false);
    const wrapperRef = useRef<HTMLDivElement>(null);
    const frameRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const wrapper = wrapperRef.current;
        const frame = frameRef.current;

        if (!wrapper || !frame) {
            return;
        }

        if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
            return;
        }

        let raf: number | null = null;

        const update = (): void => {
            raf = null;

            const viewportHeight = window.innerHeight;
            const wrapperTop = wrapper.getBoundingClientRect().top;
            const progress = Math.min(1, Math.max(0, (viewportHeight * 0.9 - wrapperTop) / (viewportHeight * 0.55)));

            frame.style.setProperty("--tilt-progress", (progress * progress * (3 - 2 * progress)).toFixed(4));
        };

        const onScroll = (): void => {
            raf ??= requestAnimationFrame(update);
        };

        window.addEventListener("scroll", onScroll, { passive: true });
        window.addEventListener("resize", onScroll, { passive: true });
        update();

        return () => {
            window.removeEventListener("scroll", onScroll);
            window.removeEventListener("resize", onScroll);

            if (raf !== null) {
                cancelAnimationFrame(raf);
            }
        };
    }, []);

    return (
        <div ref={wrapperRef} className={styles.MainWrapper}>
            <div ref={frameRef} className={styles.Frame}>
                <div className={styles.Screen}>
                    {playing ? (
                        <iframe
                            className={styles.Player}
                            src={VIDEO_SRC}
                            title="Axelix product preview"
                            allow="autoplay; fullscreen; encrypted-media; picture-in-picture"
                            allowFullScreen
                        />
                    ) : (
                        <button
                            className={styles.PosterButton}
                            type="button"
                            aria-label="Play the Axelix product preview"
                            onClick={() => setPlaying(true)}
                        >
                            <img
                                className={styles.Poster}
                                src={POSTER_SRC}
                                alt=""
                                width={1280}
                                height={720}
                                loading="lazy"
                                decoding="async"
                            />
                            <span className={styles.PlayButton}>
                                <PlayIcon width="22" height="22" />
                                PLAY
                            </span>
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};
