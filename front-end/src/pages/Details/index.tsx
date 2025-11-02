import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { Loader } from "components";
import { fetchData } from "helpers";
import { type IDetailsResponseBody, StatefulRequest } from "models";
import { getDetailsData } from "services";

import { DetailsCard } from "./DetailsCard";
import { DetailsHeader } from "./DetailsFirstSection";
import styles from "./styles.module.css";

const Details = () => {
    const { instanceId } = useParams();

    const [dataState, setDataState] = useState(StatefulRequest.loading<IDetailsResponseBody>());

    useEffect(() => {
        fetchData(setDataState, () => getDetailsData(instanceId!));
    }, []);

    if (dataState.loading) {
        return <Loader />;
    }

    if (dataState.error) {
        // todo change error handling in future
        return dataState.error;
    }

    const detailsFeed = dataState.response!;
    const { serviceName, git, build, spring, runtime, os } = detailsFeed;

    // Here, the order is very important. It must be exactly like this:
    // git, build, spring, runtime, os
    const detailsCardsData: Omit<IDetailsResponseBody, "serviceName"> = {
        git: git,
        build: build,
        spring: spring,
        runtime: runtime,
        os: os,
    };

    const getCardsData = Object.entries(detailsCardsData).map(([title, content]) => ({
        title: title,
        content: Object.entries(content),
    }));

    return (
        <>
            <DetailsHeader instanceName={serviceName} />

            <div className={styles.InnerWrapper}>
                <div className={styles.ColumnWrapper}>
                    {getCardsData.slice(0, 2).map(({ title, content }) => (
                        <DetailsCard title={title} content={content} key={title} />
                    ))}
                </div>

                <div className={styles.ColumnWrapper}>
                    {getCardsData.slice(2, 5).map(({ title, content }) => (
                        <DetailsCard title={title} content={content} key={title} />
                    ))}
                </div>
            </div>
        </>
    );
};

export default Details;
