import {
    ELanguage,
    IInstallationInstructions,
    IWhatCanDoCardData,
    IZigzagSectionData
} from "@/models";

import MockImage from "@/assets/images/mockImage.png"

// TODO: revisit the links here
export const whatCanDoCardsData: IWhatCanDoCardData[] = [
    {
        image: MockImage.src,
        title: "Explore Application Context Beans",
        description: "Gain deep insights into Application Context beans, including their origins and details on why certain beans were excluded.",
        category: "@Bean",
        documentationLink: "https://spring.io",
    },
    {
        image: MockImage.src,
        title: "Reveal <code>@Transactional</code> bottlenecks",
        description: "Gain visibility into <code>@Transactional</code> execution timelines and identify potential performance pitfalls before they escalate",
        category: "@Transactional",
        documentationLink: "https://spring.io",
    },
    {
        image: MockImage.src,
        title: "Uncover Property Origins and Sources",
        description: "Trace Spring Boot property values to their origins and identify which configuration source or override is currently in effect",
        category: "@Value",
        documentationLink: "https://spring.io",
    },
    {
        image: MockImage.src,
        title: "Orchestrate <code>@Scheduled</code> Tasks",
        description: "Take command of background jobs by toggling tasks, redefining cron expressions, and triggering manual executions with a click",
        category: "@Scheduled",
        documentationLink: "https://spring.io",
    },
    {
        image: MockImage.src,
        title: "Control Runtime Caching Behavior",
        description: "Manage your data layer by monitoring real-time cache efficiency and hit/miss ratios while clearing or toggling caches instantly",
        category: "@Cacheable",
        documentationLink: "https://spring.io",
    },
    {
        image: MockImage.src,
        title: "Discover the <code>@Conditional</code> Outcomes",
        description: "Analyze the results of <code>@Conditional</code> evaluations and explore the specific reasons behind each outcome",
        category: "@Conditional",
        documentationLink: "https://spring.io",
    },
]

export const whatCanDoContainerVariants = {
    visible: {
        transition: {
            staggerChildren: 0.3,
        },
    },
};

export const whatCanDoCardVariants = {
    hidden: {
        opacity: 0,
        y: 20
    },
    visible: {
        opacity: 1,
        y: 0,
        transition: {
            duration: 1
        }
    },
};

export const enSequence = [
    "AI friendly!",
    1000,
    "easy!",
    1000,
    "enjoyable!",
    1000,
    "debuggable!",
    1000,
    "testable!",
    1000,
    "efficient!",
    1000
]

export const zigzagSectionsData: IZigzagSectionData[] = [
    {
        title: "Enhance your Spring Boot deployments with the power of AI",
        description: "Axelix is a product that is natively designed to be used by both humans and AI models. " +
            "For humans, Axelix provides the intuitive user-friendly UI along with documentation. For AI, Axelix " +
            "contains a built-in MCP server that delivers the accurate and secured state of the running Spring Boot " +
            "application to AI.",
        href: "",
        image: MockImage
    },
    {
        title: "Debug Spring Boot Applications with Ease",
        description: [
            "Gain valuable insights into your Spring Boot applications by unraveling the beans in use and " +
            "their origins, discovering available properties and their actual values, determining " +
            "long-executing @Transactional methods, @Cacheable caches usage statistics and much more"
        ].join(),
        href: "",
        image: MockImage
    },
    {
        title: "Streamline Your Spring Boot Testing Process",
        description: [
            "Make testing your Spring Boot Microservices enjoyable and efficient by modifying, " +
            "triggering, or disabling @Scheduled jobs on the fly and changing logging levels as " +
            "needed at runtime. Furthermore, you can clear or disable Spring caches instantly " +
            "and modify @ConfigurationProperties without the need for a re-deploy."
        ].join(),
        href: "",
        image: MockImage
    },
]

// TODO: review the code samples here
const axelixVersion = "1.0.0"

function addStarterStep() {
    return {
        name: "Add Starter",
        codeSampleGroups: [
            {
                group: "Spring Boot 2",
                codeSamples: [
                    {
                        codeSampleTitle: "Gradle Kotlin DSL",
                        codeSample: `implementation("com.axelixlabs:axelix-spring-boot-2-starter:${axelixVersion}")`,
                        language: ELanguage.KOTLIN
                    },
                    {
                        codeSampleTitle: "Gradle Groovy DSL",
                        codeSample: `implementation("com.axelixlabs:axelix-spring-boot-2-starter:${axelixVersion}")`,
                        language: ELanguage.GROOVY
                    },
                    {
                        codeSampleTitle: "Maven",
                        codeSample: `       
                                    <dependency>
                                        <groupId>com.axelixlabs</groupId>
                                        <artifactId>axelix-spring-boot-2-starter</artifactId>
                                        <version>${axelixVersion}</version>
                                    </dependency>
                                `,
                        language: ELanguage.XML
                    }
                ]
            },
            {
                group: "Spring Boot 3",
                codeSamples: [
                    {
                        codeSampleTitle: "Gradle Kotlin DSL",
                        codeSample: `implementation("com.axelixlabs:axelix-spring-boot-3-starter:${axelixVersion}")`,
                        language: ELanguage.KOTLIN
                    },
                    {
                        codeSampleTitle: "Gradle Groovy DSL",
                        codeSample: `implementation("com.axelixlabs:axelix-spring-boot-3-starter:${axelixVersion}")`,
                        language: ELanguage.GROOVY
                    },
                    {
                        codeSampleTitle: "Maven",
                        codeSample: `       
                                    <dependency>
                                        <groupId>com.axelixlabs</groupId>
                                        <artifactId>axelix-spring-boot-3-starter</artifactId>
                                        <version>${axelixVersion}</version>
                                    </dependency>
                                `,
                        language: ELanguage.XML
                    }
                ]
            }
        ]
    };
}

function configureStarterStep() {
    return {
        name: "Configure Starter",
        codeSampleGroups: [
            {
                group: "Yaml",
                codeSamples: [
                    {
                        codeSampleTitle: "Yaml",
                        codeSample: ``,
                        language: ELanguage.YAML
                    }
                ]
            },
            {
                group: "Java Properties",
                codeSamples: [
                    {
                        codeSampleTitle: "Yaml",
                        codeSample: ``,
                        language: ELanguage.YAML
                    }
                ]
            },
        ]
    };
}

export const installationOptions: IInstallationInstructions[] = [
    {
        option: "Docker",
        description: "The docker installation involves pulling an image, running it, and then launching your Spring Boot microservices with configured Axelix Spring Boot starter",
        steps: [
            {
                name: "Run Axelix Image",
                codeSampleGroups: [
                    {
                        group: "default",
                        codeSamples: [
                            {
                                codeSampleTitle: "Shell",
                                language: ELanguage.SHELL,
                                // TODO: Add ENV variables required for self-registration
                                codeSample: `
                                    # Run the docker image (optionally pulls an image)
                                    docker run \\ 
                                        --publish 8080:8080 \\
                                        -e AXELIX_MASTER_DISCOVERY_AUTO=false \\ 
                                        --name axelix \\ 
                                        --detach \\
                                        ghcr.io/axelixlabs/axelix:${axelixVersion}`
                            }
                        ]
                    }
                ]
            },
            addStarterStep(),
            configureStarterStep()
        ]
    },
    {
        option: "Docker Compose",
        description: "The docker compose installation involves adding and configuring Axelix Spring Boot starter, and then launching your microservices along with Axelix UI via docker compose",
        steps: [
            addStarterStep(),
            configureStarterStep(),
            {
                name: "Define Services",
                codeSampleGroups: [
                    {
                        group: "default",
                        codeSamples: [
                            {
                                codeSampleTitle: "Docker Compose",
                                language: ELanguage.YAML,
                                codeSample: `
                                services:
                                  axelix:
                                    image: 
                                    container_name: axelix
                                    environment:
                                      AXELIX_MASTER_DISCOVERY_AUTO: false
                                    ports:
                                      - "9444:8080"
                                  your-spring-boot-app:
                                    build:
                                      dockerfile: /path/to/Dockerfile
                                      context: .
                                    environment:
                                      AXELIX_STARTER_DISCOVERY_SELF_REGISTER: true
                                      AXELIX_STARTER_DISCOVERY_MASTER_URL: http://localhost:9444
                                `
                            }
                        ]
                    }
                ]
            },
            {
                name: "Compose Up",
                codeSampleGroups: [
                    {
                        group: "default",
                        codeSamples: [
                            {
                                codeSampleTitle: "Shell",
                                language: ELanguage.SHELL,
                                codeSample: "docker compose up --detach"
                            }
                        ]
                    }
                ]
            }
        ]
    },
    {
        option: "Kubernetes",
        description: "The K8S installation involves pulling and then installing a helm chart into the K8S cluster, and then adding Axelix Spring Boot starter to your microservices and configuring it",
        steps: [
            {
                name: "Pull Chart",
                codeSampleGroups: [
                    {
                        group: "default",
                        codeSamples: [
                            {
                                codeSampleTitle: "Shell",
                                language: ELanguage.SHELL,
                                codeSample: `
                                helm repo add axelixlabs https://axelixlabs.github.io/helm-charts
                                helm repo update
                                `
                            }
                        ]
                    }
                ]
            },
            {
                name: "Install Chart",
                codeSampleGroups: [
                    {
                        group: "default",
                        codeSamples: [
                            {
                                codeSampleTitle: "Shell",
                                language: ELanguage.SHELL,
                                codeSample: "helm install axelix-release axelixlabs/axelix"
                            }
                        ]
                    }
                ]
            },
            addStarterStep(),
            configureStarterStep()
        ]
    }
]
