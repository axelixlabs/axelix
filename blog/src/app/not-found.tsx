import Link from "next/link";

export default function NotFound() {
  return (
    <div className="page">
      <div className="wrap" style={{ padding: "120px 0", textAlign: "center" }}>
        <h1 style={{ fontSize: "clamp(40px, 6vw, 80px)", fontWeight: 500, margin: 0 }}>404</h1>
        <p style={{ color: "var(--ink-3)", marginTop: 12 }}>
          That page wandered off. Let&apos;s get you back.
        </p>
        <p style={{ marginTop: 24 }}>
          <Link className="ext-link" href="/">
            ← Back to the blog
          </Link>
        </p>
      </div>
    </div>
  );
}
