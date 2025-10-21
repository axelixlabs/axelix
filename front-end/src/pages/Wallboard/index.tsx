import {useEffect, useState} from 'react';

import {EmptyHandler, Loader, PageSearch} from 'components';
import {WallboardCard} from './WallboardCard';
import {type IServiceCardsData, StatefulRequest} from 'models';
import {fetchData, filterInstances} from 'helpers';

import styles from './styles.module.css'
import {getWallboardData} from "services";

export const Wallboard = () => {

    const [search, setSearch] = useState<string>("")
    const [wallboard, setWallboard] = useState(StatefulRequest.loading<IServiceCardsData>())

    useEffect(() => {
      fetchData(setWallboard, () => getWallboardData())
    }, [])

    if (wallboard.loading) {
        return <Loader />
    }

    // todo fix this in future
    if (wallboard.error) {
        return wallboard.error
    }

    const instanceCards = wallboard.response!.instances;
    const effectiveInstanceCards = search
        ? filterInstances(instanceCards, search)
        : instanceCards;

    const addonAfter = `${effectiveInstanceCards.length} / ${instanceCards.length}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} search={search} setSearch={setSearch} />

            <EmptyHandler isEmpty={instanceCards.length === 0}>
                <div className={styles.CardsResponsiveWrapper}>
                    {effectiveInstanceCards.map(data => <WallboardCard data={data} key={data.instanceId} />)}
                </div>
            </EmptyHandler>
        </>
    )
};

export default Wallboard;