import { useEffect } from 'react';

import { filterServiceCards, getWallboardDataThunk } from 'store/slices';
import { EmptyHandler, Loader, PageSearch } from 'components';
import { useAppDispatch, useAppSelector } from 'hooks';
import { WallboardCard } from './WallboardCard';

import styles from './styles.module.css'

export const Wallboard = () => {
    const dispatch = useAppDispatch()
    const { instances, filteredInstances, instancesSearchText, loading, error } = useAppSelector(state => state.wallboard)

    useEffect(() => {
        dispatch(getWallboardDataThunk())
    }, [])

    if (loading) {
        return <Loader />
    }

    // todo fix this in future
    if (error) {
        return error
    }

    const serviceCardsList = filteredInstances.length ? filteredInstances : instances;
    const noDataAfterSearch = !!instancesSearchText && !filteredInstances.length;
    const addonAfter = `${instancesSearchText ? filteredInstances.length : instances.length} / ${instances.length}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} onChange={(value) => dispatch(filterServiceCards(value))} />

            <EmptyHandler isEmpty={noDataAfterSearch}>
                <div className={styles.CardsResponsiveWrapper}>
                    {serviceCardsList.map(data => <WallboardCard data={data} key={data.instanceId} />)}
                </div>
            </EmptyHandler>
        </>
    )
};

export default Wallboard;