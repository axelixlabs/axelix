"use client";

import { useEffect, useRef, useState } from "react";
import {
  CheckIcon,
  ChevronDownIcon,
  ComposeIcon,
  CopyIcon,
  DockerIcon,
  K8sIcon,
  ServerIcon,
} from "@/assets";
import styles from "./styles.module.css";

type Method = "docker" | "compose" | "k8s" | "bare";
type SbVariant = "sb2" | "sb3" | "sb4";
type CfgVariant = "yaml" | "properties";

const METHODS: { id: Method; label: string; icon: React.ReactNode }[] = [
  {
    id: "docker",
    label: "Docker",
    icon: <DockerIcon width="22" height="22" />,
  },
  {
    id: "compose",
    label: "Docker Compose",
    icon: <ComposeIcon width="22" height="22" />,
  },
  {
    id: "k8s",
    label: "Kubernetes",
    icon: <K8sIcon width="22" height="22" />,
  },
  {
    id: "bare",
    label: "Bare Metal",
    icon: <ServerIcon width="22" height="22" />,
  },
];

const METHODS_DATA: Record<Method, {
  description: string,
  href: string;
}> = {
  k8s: {
    description: "The Helm chart installs the master into your cluster. Apps discover it through cluster DNS — no extra wiring needed.",
    href: "https://axelix.io/docs/installation/configuring-master#run-on-kubernetes"
  },
  compose: {
    description: "Compose defines the master as a service in your stack. Bring it up once, then point your apps at it through the Compose network.",
    href: "https://axelix.io/docs/installation/configuring-master#run-with-docker-compose"
  },
  docker: {
    description: "The docker installation involves pulling an image, running it, and then launching your Spring Boot microservices with the configured Axelix starter.",
    href: "https://axelix.io/docs/installation/configuring-master#run-with-docker"
  },
  bare: {
    description: "Installing Axelix on bare metal without containerization is also possible by directly launching a JAR file",
    href: "https://axelix.io/docs/installation/configuring-master#run-as-a-jar"
  }
};

const STEP_NAMES: Record<1 | 2 | 3, string> = {
  1: "Run Axelix",
  2: "Add Starter",
  3: "Configure",
};

const SB_OPTIONS: { id: SbVariant; label: string }[] = [
  { id: "sb2", label: "Spring Boot 2" },
  { id: "sb3", label: "Spring Boot 3" },
  { id: "sb4", label: "Spring Boot 4" },
];

const CFG_OPTIONS: { id: CfgVariant; label: string }[] = [
  { id: "yaml", label: "yaml" },
  { id: "properties", label: "properties" },
];

function StarterMini({ artifact }: { artifact: string }) {
  const [copiedIdx, setCopiedIdx] = useState<number | null>(null);
  const timers = useRef<Array<ReturnType<typeof setTimeout> | null>>([
    null,
    null,
    null,
  ]);

  const blocks: { label: string; code: string }[] = [
    {
      label: "Gradle Kotlin DSL",
      code: `implementation("com.axelixlabs:${artifact}:1.0.0")`,
    },
    {
      label: "Gradle Groovy DSL",
      code: `implementation 'com.axelixlabs:${artifact}:1.0.0'`,
    },
    {
      label: "Maven",
      code: `<dependency>
    <groupId>com.axelixlabs</groupId>
    <artifactId>${artifact}</artifactId>
    <version>1.0.0</version>
</dependency>`,
    },
  ];

  async function copy(idx: number, text: string) {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedIdx(idx);
      if (timers.current[idx]) clearTimeout(timers.current[idx]!);
      timers.current[idx] = setTimeout(() => setCopiedIdx(null), 1200);
    } catch {
      /* clipboard blocked */
    }
  }

  return (
    <>
      {blocks.map((b, i) => (
        <div key={b.label} className={styles.Mini}>
          <div className={styles.MiniHead}>
            <span className={styles.MiniLabel}>{b.label}</span>
            <button
              type="button"
              className={styles.MiniCopy}
              onClick={() => copy(i, b.code)}
            >
              {copiedIdx === i ? <span>Copied</span> : (
                <>
                  <CopyIcon />
                  Copy
                </>
              )}
            </button>
          </div>
          <pre className={styles.MiniCode}>{b.code}</pre>
        </div>
      ))}
    </>
  );
}

const SB_ARTIFACT: Record<SbVariant, string> = {
  sb2: "axelix-spring-boot-2-starter",
  sb3: "axelix-spring-boot-3-starter",
  sb4: "axelix-spring-boot-4-starter",
};

export const Install = () => {
  const [method, setMethod] = useState<Method>("docker");
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [sb, setSb] = useState<SbVariant>("sb2");
  const [cfg, setCfg] = useState<CfgVariant>("yaml");
  const [openSelect, setOpenSelect] = useState<null | "sb" | "cfg">(null);
  const [copied, setCopied] = useState(false);
  const copyTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const selectRef = useRef<HTMLDivElement | null>(null);
  const activeSnippetRef = useRef<HTMLElement | null>(null);

  // Close any open dropdown when clicking outside
  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (!selectRef.current) return;
      if (!selectRef.current.contains(e.target as Node)) setOpenSelect(null);
    }
    document.addEventListener("click", onClick);
    return () => document.removeEventListener("click", onClick);
  }, []);

  // Reset copy state when switching
  useEffect(() => {
    setCopied(false);
    if (copyTimer.current) {
      clearTimeout(copyTimer.current);
      copyTimer.current = null;
    }
  }, [method, step, sb, cfg]);

  async function copyMain() {
    const el = activeSnippetRef.current;
    if (!el) return;
    const text = (el.innerText || "").replace(/ /g, " ");
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      if (copyTimer.current) clearTimeout(copyTimer.current);
      copyTimer.current = setTimeout(() => setCopied(false), 1200);
    } catch {
      /* clipboard blocked */
    }
  }

  return (
    <section className={styles.Install} id="install">
      <div className={`wrap ${styles.Wrap}`}>
        <div className={styles.Header}>
          <div>
            <span className={styles.Eyebrow}>Install</span>
            <h2 className={styles.H2}>
              Three commands.{" "}
              <span className={styles.Stroke}>
                No agent, no JVM flags, no redeploys.
              </span>
            </h2>
          </div>
          <p className={styles.Intro}>
            Pick how you run things — Docker, Compose, or Kubernetes. Spin up
            the master, drop the starter into your app, point it at the master.
            The service shows up in the console <em>and</em> on the MCP server
            within seconds.
          </p>
        </div>

        <div className={styles.Installer}>
          <aside className={styles.Meth}>
            {METHODS.map((m) => (
              <button
                key={m.id}
                className={`${styles.MethBtn} ${method === m.id ? styles.Active : ""}`}
                type="button"
                onClick={() => setMethod(m.id)}
              >
                <span className={styles.Ic}>
                  {m.icon}
                </span>
                {m.label}
              </button>
            ))}

            <div className={styles.MethFoot}>
              <p className={styles.Desc}>{METHODS_DATA[method].description}</p>
              <a className={styles.Docs} href={METHODS_DATA[method].href} target="_blank" rel="noopener noreferrer">
                Read Documentation <span className={styles.Arr}>→</span>
              </a>
            </div>
          </aside>

          <div className={styles.BodyPane}>
            <div className={styles.Codepane}>
              <div className={styles.TopBar}>
                <div className={styles.Tabs}>
                  {([1, 2, 3] as const).map((s) => (
                    <button
                      key={s}
                      type="button"
                      className={`${styles.Tab} ${step === s ? styles.Active : ""}`}
                      onClick={() => setStep(s)}
                    >
                      {s}. {STEP_NAMES[s]}
                    </button>
                  ))}
                </div>
                <div className={styles.FileInfo} ref={selectRef}>
                  {step === 1 && (
                    <div className={`${styles.Vgroup} ${styles.Active}`}>
                      <span className={styles.Vlabel}>Shell</span>
                    </div>
                  )}
                  {step === 2 && (
                    <div className={`${styles.Vgroup} ${styles.Active}`}>
                      <VariantSelect
                        label={SB_OPTIONS.find((o) => o.id === sb)!.label}
                        open={openSelect === "sb"}
                        onToggle={() =>
                          setOpenSelect(openSelect === "sb" ? null : "sb")
                        }
                        options={SB_OPTIONS}
                        active={sb}
                        onPick={(id) => {
                          setSb(id as SbVariant);
                          setOpenSelect(null);
                        }}
                      />
                    </div>
                  )}
                  {step === 3 && (
                    <div className={`${styles.Vgroup} ${styles.Active}`}>
                      <VariantSelect
                        label={cfg}
                        open={openSelect === "cfg"}
                        onToggle={() =>
                          setOpenSelect(openSelect === "cfg" ? null : "cfg")
                        }
                        options={CFG_OPTIONS}
                        active={cfg}
                        onPick={(id) => {
                          setCfg(id as CfgVariant);
                          setOpenSelect(null);
                        }}
                      />
                    </div>
                  )}
                </div>
              </div>

              <div className={styles.Body}>
                {step !== 2 && (
                  <button
                    className={styles.CopyFloat}
                    type="button"
                    title="Copy"
                    onClick={copyMain}
                  >
                    {copied ? <span>Copied</span> : (
                      <>
                        <CopyIcon />
                        <span>Copy</span>
                      </>
                    )}
                  </button>
                )}

                {/* STEP 1 */}
                {step === 1 && method === "docker" && (
                  <DockerSnippet refEl={activeSnippetRef} />
                )}
                {step === 1 && method === "compose" && (
                  <ComposeSnippet refEl={activeSnippetRef} />
                )}
                {step === 1 && method === "k8s" && (
                  <K8sSnippet refEl={activeSnippetRef} />
                )}
                {step === 1 && method === "bare" && (
                  <BareMetal refEl={activeSnippetRef} />
                )}

                {/* STEP 2 */}
                {step === 2 && (
                  <div
                    className={`${styles.Snippet} ${styles.Step2} ${styles.Active}`}
                    ref={(el) => {
                      activeSnippetRef.current = el;
                    }}
                  >
                    <StarterMini artifact={SB_ARTIFACT[sb]} />
                  </div>
                )}

                {/* STEP 3 */}
                {step === 3 && cfg === "yaml" && (
                  <YamlSnippet refEl={activeSnippetRef} />
                )}
                {step === 3 && cfg === "properties" && (
                  <PropertiesSnippet refEl={activeSnippetRef} />
                )}
              </div>

              <div className={styles.CtaRow}>
                <button
                  className={`${styles.NavBtn} ${styles.Prev}`}
                  type="button"
                  disabled={step <= 1}
                  onClick={() => step > 1 && setStep((step - 1) as 1 | 2 | 3)}
                >
                  ← {step > 1 ? STEP_NAMES[(step - 1) as 1 | 2 | 3] : "Previous"}
                </button>
                <span className={styles.Status}>Step {step} of 3</span>
                <button
                  className={`${styles.NavBtn} ${styles.Next}`}
                  type="button"
                  disabled={step === 3}
                  onClick={() => setStep((step + 1) as 1 | 2 | 3)}
                >
                  Next: {STEP_NAMES[(step + 1) as 1 | 2 | 3]} →
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className={styles.Meta}>
          <span className={styles.Chip}>Spring Boot 2 · 3 · 4</span>
          <span className={styles.Chip}>JVM 11 — 25</span>
          <span className={styles.Chip}>Docker · Helm</span>
        </div>
      </div>
    </section>
  );
}

function VariantSelect({
  label,
  open,
  onToggle,
  options,
  active,
  onPick,
}: {
  label: string;
  open: boolean;
  onToggle: () => void;
  options: { id: string; label: string }[];
  active: string;
  onPick: (id: string) => void;
}) {
  return (
    <div className={`${styles.Vselect} ${open ? styles.Open : ""}`}>
      <button
        className={styles.Vtrigger}
        type="button"
        onClick={(e) => {
          e.stopPropagation();
          onToggle();
        }}
      >
        <span className={styles.Vlabel}>{label}</span>
        <ChevronDownIcon className={styles.Chev} />
      </button>
      <div className={styles.Vmenu} role="listbox">
        {options.map((o) => (
          <button
            key={o.id}
            className={`${styles.Voption} ${active === o.id ? styles.Active : ""}`}
            type="button"
            role="option"
            onClick={(e) => {
              e.stopPropagation();
              onPick(o.id);
            }}
          >
            {o.label}
            <CheckIcon className={styles.Check} />
          </button>
        ))}
      </div>
    </div>
  );
}

/* ===== Snippets ===== */

type SnippetProps = { refEl: React.RefObject<HTMLElement | null> };

function DockerSnippet({ refEl }: SnippetProps) {
  return (
    <pre
      className={`${styles.Snippet} ${styles.Active}`}
      ref={(el) => {
        refEl.current = el;
      }}
    >
      <code>
        <span className={styles.Line}>
          <span className={styles.Co}># Run the docker image (optionally pulls an image)</span>
        </span>
        <span className={styles.Line}>
          <span className={styles.Cm}>docker run</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.Ar}>--publish</span>{" "}
          <span className={styles.St}>8080:8080</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.Co}># Important: change the algorithm and key for production use</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.Ar}>-e</span> AXELIX_AUTH_JWT_ALGORITHM=
          <span className={styles.St}>HMAC256</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.Ar}>-e</span> AXELIX_AUTH_JWT_SIGNING_KEY=
          <span className={styles.St}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.Ar}>--name</span>{" "}
          <span className={styles.St}>axelix</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.Ar}>--detach</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.St}>ghcr.io/axelixlabs/axelix:1.0.0</span>
        </span>
      </code>
    </pre>
  );
}

function ComposeSnippet({ refEl }: SnippetProps) {
  return (
    <pre
      className={`${styles.Snippet} ${styles.Active}`}
      ref={(el) => {
        refEl.current = el;
      }}
    >
      <code>
        <span className={styles.Line}><span className={styles.At}>services</span>:</span>
        <span className={styles.Line}>
          {"  "}<span className={styles.At}>axelix</span>:
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.At}>image</span>:{" "}
          <span className={styles.St}>ghcr.io/axelixlabs/axelix:1.0.0</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.At}>container_name</span>:{" "}
          <span className={styles.St}>axelix</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.At}>ports</span>:
        </span>
        <span className={styles.Line}>
          {"      "}- <span className={styles.St}>&quot;9444:8080&quot;</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.At}>environment</span>:
        </span>
        <span className={styles.Line}>
          {"      "}
          <span className={styles.Co}># Important: change for production use</span>
        </span>
        <span className={styles.Line}>
          {"      "}
          <span className={styles.At}>AXELIX_AUTH_JWT_ALGORITHM</span>:{" "}
          <span className={styles.St}>HMAC256</span>
        </span>
        <span className={styles.Line}>
          {"      "}
          <span className={styles.At}>AXELIX_AUTH_JWT_SIGNING_KEY</span>:{" "}
          <span className={styles.Kw}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.At}>restart</span>:{" "}
          <span className={styles.St}>unless-stopped</span>
        </span>

        <span className={styles.Line}>
          {"    "}
        </span>

        <span className={styles.Line}>
          {"  "}<span className={styles.At}>your-spring-boot-app</span>:
        </span>
        <span className={styles.Line}>
          {"    "}
            <span className={styles.At}>build</span>:{" "}
        </span>
        <span className={styles.Line}>
          {"      "}
            <span className={styles.At}>dockerfile</span>:{" "}
            <span className={styles.St}>/path/to/Dockerfile</span>
        </span>
        <span className={styles.Line}>
          {"      "}
            <span className={styles.At}>context</span>:{" "}
            <span className={styles.St}>.</span>
        </span>
        <span className={styles.Line}>
          {"    "}
            <span className={styles.At}>environment</span>:{" "}
        </span>
        <span className={styles.Line}>
          {"      "}
            <span className={styles.At}>- AXELIX_SBS_DISCOVERY_INSTANCE_NAME</span>:{" "}
            <span className={styles.St}>my-app</span>
        </span>
        <span className={styles.Line}>
          {"      "}
            <span className={styles.At}>- AXELIX_SBS_DISCOVERY_INSTANCE_ACTUATOR_URL</span>:{" "}
            <span className={styles.St}>http://my-app.com/actuator</span>
        </span>
        <span className={styles.Line}>
          {"      "}
            <span className={styles.At}>- AXELIX_SBS_DISCOVERY_MASTER_URL</span>:{" "}
            <span className={styles.St}>http://localhost:9444/api/internal/service/register</span>
        </span>

      </code>
    </pre>
  );
}

function K8sSnippet({ refEl }: SnippetProps) {
  return (
    <pre
      className={`${styles.Snippet} ${styles.Active}`}
      ref={(el) => {
        refEl.current = el;
      }}
    >
      <code>
        <span className={styles.Line}>
          <span className={styles.Co}># Install Axelix Master via Helm</span>
        </span>
        <span className={styles.Line}>
          <span className={styles.Co}># Important: Please, change the algorithm and the key for production use</span>
        </span>
        <span className={styles.Line}>
          <span className={styles.Cm}>helm repo add</span>{" "}
          <span className={styles.St}>axelixlabs</span>{" "}
          <span className={styles.St}>https://axelixlabs.github.io/helm-charts</span>
        </span>
        <span className={styles.Line}>
          <span className={styles.Cm}>helm repo update</span>{" "}
        </span>
        <span className={styles.Line}>
          <span className={styles.Cm}>helm install</span>{" "}
          <span className={styles.St}>axelix</span>{" "}
          <span className={styles.St}>axelixlabs/axelix</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.Ar}>--set</span>{" "}
          <span className={styles.St}>axelix.master.auth.jwt.algorithm=HMAC512</span>{" "}
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.Ar}>--set</span>{" "}
          <span className={styles.St}>axelix.master.auth.jwt.signingKey=8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>{" "}
        </span>
      </code>
    </pre>
  );
}

function BareMetal({ refEl }: SnippetProps) {
  return (
    <pre
      className={`${styles.Snippet} ${styles.Active}`}
      ref={(el) => {
        refEl.current = el;
      }}
    >
      <code>
        <span className={styles.Line}>
          <span className={styles.Co}># Download and run the Axelix Master JAR</span>
        </span>
        <span className={styles.Line}>
          <span className={styles.Co}># Important: Please, change the algorithm and the key for production use</span>
        </span>
        <span className={styles.Line}>
          <span className={styles.Cm}>java -jar axelix-1.0.0.jar</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
          <span className={styles.St}>--server.port=8080</span>{" "}
          <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
            <span className={styles.St}>--axelix.master.auth.jwt.algorithm=HMAC512</span>{" "}
            <span className={styles.Nl}>\</span>
        </span>
        <span className={styles.Line}>
          {"    "}
            <span className={styles.St}>--axelix.master.auth.jwt.signing-key=8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
        </span>
      </code>
    </pre>
  );
}

function YamlSnippet({ refEl }: SnippetProps) {
  return (
    <pre
      className={`${styles.Snippet} ${styles.Active}`}
      ref={(el) => {
        refEl.current = el;
      }}
    >
      <code>
        <span className={styles.Line}>
          <span className={styles.At}>axelix</span>:
        </span>
        <span className={styles.Line}>
          {"  "}<span className={styles.At}>sbs</span>:
        </span>
        <span className={styles.Line}>
          {"    "}<span className={styles.At}>auth</span>:
        </span>
        <span className={styles.Line}>
          {"      "}<span className={styles.At}>jwt</span>:
        </span>
        <span className={styles.Line}>
          {"        "}
          <span className={styles.At}>algorithm</span>:{" "}
          <span className={styles.St}>HMAC512</span>
        </span>
        <span className={styles.Line}>
          {"        "}
          <span className={styles.At}>signing-key</span>:{" "}
          <span className={styles.St}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
        </span>
        <span className={styles.Line}>
          {"    "}<span className={styles.At}>discovery</span>:
        </span>
        <span className={styles.Line}>
          {"      "}
          <span className={styles.At}>instance-name</span>:{" "}
          <span className={styles.St}>my-app</span>
        </span>
        <span className={styles.Line}>
          {"      "}
          <span className={styles.At}>instance-url</span>:{" "}
          <span className={styles.St}>https://my-app.com/actuator</span>
        </span>
        <span className={styles.Line}>
          {"      "}
          <span className={styles.At}>master-url</span>:{" "}
          <span className={styles.St}>https://axelix-master.com/api/internal/service/register</span>
        </span>
      </code>
    </pre>
  );
}

function PropertiesSnippet({ refEl }: SnippetProps) {
  return (
    <pre
      className={`${styles.Snippet} ${styles.Active}`}
      ref={(el) => {
        refEl.current = el;
      }}
    >
      <code>
        <span className={styles.Line}>
          <span className={styles.At}>axelix.master.url</span>=
          <span className={styles.St}>http://localhost:8080</span>
        </span>
        <span className={styles.Line}>
          <span className={styles.At}>axelix.auth.jwt.algorithm</span>=
          <span className={styles.St}>HMAC256</span>
        </span>
        <span className={styles.Line}>
          <span className={styles.At}>axelix.auth.jwt.signing-key</span>=
          <span className={styles.Kw}>{"${AXELIX_TOKEN}"}</span>
        </span>
      </code>
    </pre>
  );
}
