"use client";
import { useEffect, useState } from "react";

export const ReadProgress = () => {
  const [progress, setProgress] = useState<number>(0);

  useEffect(() => {
    const onScroll = (): void => {
      const doc = document.documentElement;
      const max = doc.scrollHeight - doc.clientHeight;
      setProgress(max > 0 ? Math.min(100, (window.scrollY / max) * 100) : 0);
    };

    onScroll();

    window.addEventListener("scroll", onScroll, { passive: true });

    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return <div className="read-progress" style={{ width: `${progress}%` }} />;
};
