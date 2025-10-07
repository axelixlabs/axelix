import { Input } from 'antd';
import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';

import { filterServiceCards, getWallboardDataThunk } from 'store/slices';
import { useAppDispatch, useAppSelector } from 'hooks';
import { EmptyHandler, Loader } from 'components';
import { WallboardCard } from './WallboardCard';

import styles from './styles.module.css'

export const Wallboard = () => {
    const { t } = useTranslation()
    const dispatch = useAppDispatch()
    const { instances, filteredInstances, instancesSearchText, loading, error } = useAppSelector(state => state.wallboard)

    useEffect(() => {
        dispatch(getWallboardDataThunk())
        // The dispatch passed as a dependency to useEffect does not affect its execution, since the dispatch function is never recreated.
        // There are two common approaches: either include dispatch in the dependencies or omit it. 
        // Both approaches are considered correct.
    }, [dispatch])

    if (loading) {
        return <Loader />
    }

    // todo fix this in future
    if (error) {
        return error
    }

    const serviceCardsData = filteredInstances.length ? filteredInstances : instances;
    const noDataAfterSearch = !!instancesSearchText && !filteredInstances.length;

    return (
        <>
            <Input
                placeholder={t("search")}
                onChange={(e) => dispatch(filterServiceCards(e.target.value))}
                className={styles.Search}
            />

            <EmptyHandler isEmpty={noDataAfterSearch}>
                <div className={styles.CardsResponsiveWrapper}>
                    {serviceCardsData.map(data => <WallboardCard data={data} key={data.instanceId} />)}
                </div>
            </EmptyHandler>
        </>
    )
};

export default Wallboard;