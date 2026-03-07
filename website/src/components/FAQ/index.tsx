import { IFAQItem } from "@/models";
import { FAQAccordions } from "./FAQAccordions";
import { FAQStructuredData } from "./FAQStructuredData";

const faqItems: IFAQItem[] = [
    {
        question: "Why Axelix?",
        structuredDataAnswer: "Axelix was created to assist engineering teams to debug, test and monitor their Spring Boot applications. It is especially useful when it comes to Spring Boot microservice ecosystems, where engineering teams face similar challenges during development",
        answer: (
            <>
                <p>A lot of problems and pitfalls that you face during Spring Boot development and testing are actually quite common. Developers often:</p>

                <ul>
                    <li>Doubt the correct <code>@Value</code> property value were injected</li>
                    <li>Or the correct <code>@Bean</code> was created</li>
                    <li>Or the expected Spring Boot <code>@AutoConfiguration</code> beans are up/down</li>
                    <li>Or they merely want to temporarily change the <code>@Scheduled</code> interval/cron in order to test it and so on</li>
                </ul>

                <p>
                    These and many other issues are versatile. And the funny thing is that with the proper knowledge of the Spring internals
                    all of these problems can be significantly mitigated. So, Axelix is a tool that helps engineering teams to tame these problems
                </p>
            </>
        )
    },
    {
        question: "How does Axelix work?",
        structuredDataAnswer: "Axelix design consists of two main components - Master and managed Spring Boot microservices. Master is the webserver core that hosts the UI and backend aggregation logic. Spring Boot starter is included in Java applications that want to be connected to Axelix.",
        answer: (
            <>
                <p>Axelix design revolves around two key components:</p>
                
                <ul>
                    <li>
                        <i>Axelix Master</i> - the webserver &quot;heart&quot; that hosts the UI along with the backend for this UI.
                    </li>
                    <li>
                        <i>Axelix Spring Boot Starter</i> - the Spring Boot Starter that is included in every
                        Java application that wants to be available in Axelix.
                    </li>
                </ul>
                
                <p>
                    The heavy lifting is done by the starter. Axelix Master is generally just the UI along with some additional
                    aggregation and discovery logic. The actual &quot;job&quot; is done by the Axelix Spring Boot Starter.
                </p>
            </>
        )
    },
    {
        question: "How is Axelix different from Spring Boot Admin?",
        structuredDataAnswer: "Axelix was designed to solve complex engineering challenges and debugging cases that traditional Spring Boot Admin may not cover. Because of its architecture, Spring Boot Admin have to solely rely on native Spring Boot Actuator endpoints, which are limited in their abilities. Also, installation of Spring Boot Admin is complicated by lack of official distributions (i.e. container images or Helm charts)",
        answer: (
            <>
                <p>Experienced Software Engineers may ask a perfectly valid question:</p>
                <p>
                    <i> How Axelix differ from Spring Boot Admin? Why creating Axelix when Spring Boot Admin is there?</i>
                </p>
                <p>
                    You see, the core engineering team behind Axelix have written Spring Boot microservices for many years. Of course, during those years
                    we have had used Spring Boot Admin (SBA) extensively. However, after all these years, we also discovered that:
                </p>

                <ul>
                    <li>
                        Although SBA is great, it still misses out on quite a lot of cases. For instance,
                        SBA can tell that the <code>@Bean</code> is in the context. But the very next question is -
                        <i>who the hell creates it? why it even exists?</i>
                        This is just one of the many examples.
                    </li>
                    <li>
                        SBA is not very easy to install. We have to create our own Java application from SBA,
                        package that up into Docker image, and then create a Helm Chart from it and finally deploy
                        the app into K8S. That is quite a lot of work. There are some community charts, that is true,
                        but they are also barely maintained and often does not meet the corporate standards.
                    </li>
                </ul>

                <p>
                    We want to clarify - this is not really the pitfall of SBA. The SBA is just designed around native actuator endpoints, which are quite limited in their abilities. Axelix is designed differently.
                </p>
                <p>
                    <b>So, The goal of the Axelix Project is to provide an easy to install solution for common problems and fill all those gaps left by SBA.</b>
                </p>
                <p>
                    To see the detailed comparison of Axelix vs SBA, please, examine <a href="https://axelix.io/docs/introduction" target="_blank" rel="noopener noreferrer">this docs page</a>
                </p>
            </>
        )
    },
    {
        question: "Is Axelix Open Source?",
        structuredDataAnswer: "Yes. The source code of the Axelix Project is open source and available under the LGPL v3.0",
        answer: (
            <>
                <p>
                    <b>Short: Yes, it is.</b>
                </p>

                <p>
                    The source code of the Axelix Project is Open Source and available under{' '}
                    <a href="https://www.gnu.org/licenses/lgpl-3.0.en.html" target="_blank" rel="noopener noreferrer">LGPL V3.0</a>.
                    You can find it over <a href="https://github.com/axelixlabs/axelix" target="_blank" rel="noopener noreferrer">on GitHub</a>.
                </p>
            </>
        ),
    },
    {
        question: "How can I install Axelix?",
        structuredDataAnswer: "Installation of Axelix requires two steps - installing the Axelix Master distribution, and then adding Axelix Spring Boot starter to all required services. Navigate to https://axelix.io to get a complete installation instructions",
        answer: (
            <>
                <p>
                    <b>Short: just follow <a href="#installation">these instructions</a>.</b>
                </p>

                <p>In general, regardless of the installation method, you need to do two things:</p>

                <ul>
                    <li>You first deploy the Axelix Master (the &quot;heart&quot; of Axelix that hosts the UI and accepts the requests from the end-users).</li>
                    <li>Then you add the Axelix starter for the Spring Boot into your build system.</li>
                </ul>

                <p>Right now, the Axelix distribution is available either as a Docker image, or as a K8S Helm Chart.</p>

                <p>
                    You typically will need very few configuration to get started.For the short installation instructions visit
                    <a href="#installation"> the dedicated section</a> on the page. To
                    read the complete guide, please, consult <a href="#TODO"> the reference doc</a >.
                </p>
            </>
        )
    },
    {
        question: "Does Axelix only support Spring Boot?",
        structuredDataAnswer: "Yes, as of now, Axelix primarily supports Spring Boot applications. The core Axelix team considered the support for additional frameworks in the future.",
        answer: (
            <>
                <p>
                    <b>Short: Yes, at least as of now, only Spring Boot.</b >
                </p>
                <p>
                    We might consider adding other frameworks in the future, but that is going to be clear later on.
                </p>
            </>
        ),
    },
];

export const FAQ = () => {
    return (
        <>
            <FAQStructuredData faqItems={faqItems} />

            <FAQAccordions faqItems={faqItems} />
        </>
    );
}