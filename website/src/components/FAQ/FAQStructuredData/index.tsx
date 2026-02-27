import { IFAQItem } from "@/models";

interface IProps {
    faqItems: IFAQItem[]
}

export const FAQStructuredData = ({ faqItems }: IProps) => {
    const JSON_LD = {
        "@context": "https://schema.org",
        "@type": "FAQPage",
        mainEntity: faqItems.map(({ question, structuredDataAnswer }) => ({
            "@type": "Question",    
            name: question,
            acceptedAnswer: {
                "@type": "Answer",
                text: structuredDataAnswer,
            },
        })),
    };

    return (
        <script
            type="application/ld+json"
            dangerouslySetInnerHTML={{ __html: JSON.stringify(JSON_LD) }}
        />
    )
} 