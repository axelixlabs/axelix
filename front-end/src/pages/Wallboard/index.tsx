import { useEffect, useState } from 'react';

import { EmptyHandler, Loader, PageSearch } from 'components';
import { useAppDispatch, useAppSelector } from 'hooks';
import { getWallboardDataThunk } from 'store/thunks';
import { WallboardCard } from './WallboardCard';
import type { IInstanceCard } from 'models';
import { filterInstances } from 'helpers';

import styles from './styles.module.css'

export const Wallboard = () => {
    const dispatch = useAppDispatch()
    const { instances, loading, error } = useAppSelector(state => state.wallboard)

    const [isSearched, setIsSearched] = useState<boolean>(false)
    const [filteredInstances, setFilteredInstances] = useState<IInstanceCard[]>([])

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

    const serviceCardsList = isSearched ? filteredInstances : instances;
    const noSearchResults = isSearched && !filteredInstances.length;
    const addonAfter = `${isSearched ? filteredInstances.length : instances.length} / ${instances.length}`;

    const handleSearchChange = (search: string): void => {
        const isSearching = Boolean(search);
        setIsSearched(isSearching);

        if (!isSearching) {
            setFilteredInstances([]);
            return;
        }

        setFilteredInstances(filterInstances(instances, search));
    };

    return (
        <>
            <PageSearch addonAfter={addonAfter} onChange={handleSearchChange} />

            <EmptyHandler isEmpty={noSearchResults}>
                <div className={styles.CardsResponsiveWrapper}>
                    {serviceCardsList.map(data => <WallboardCard data={data} key={data.instanceId} />)}
                </div>
            </EmptyHandler>
        </>
    )
};

export default Wallboard;