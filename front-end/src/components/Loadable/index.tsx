import { Suspense, type ComponentType, type FC, type PropsWithChildren } from 'react';

import { LinearProgress } from '../LinearProgress';

function Loadable<P extends object>(Component: ComponentType<P>): FC<P> {
    return (props: PropsWithChildren<P>) => (
        <Suspense fallback={<LinearProgress />}>
            <Component {...props} />
        </Suspense>
    );
}

export default Loadable;