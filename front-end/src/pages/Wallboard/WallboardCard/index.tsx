import type { IServiceCard } from 'models';

import styles from './styles.module.css'

interface IProps {
  data: IServiceCard
}

export const WallboardCard = ({ data }: IProps) => {
  return (
    <div className={`${styles.Card} ${styles[`Card${data.status}`]}`}>
      <div className={`${styles.CardHeader} ${styles[`CardHeader${data.status}`]}`}>
        {data.serviceName}
      </div>
      <div className={styles.CardBody}>
        <div>Version: {data.serviceVersion}</div>
        <div>Spring Boot: {data.springBootVersion}</div>
        <div>Java: {data.javaVersion}</div>
        <div className={styles.HashAndTimeWrapper}>
          <span>{data.commitHash}</span>
          {/* todo use this in future */}
          {/* <span>wdasda</span> */}
        </div>
      </div>
    </div>
  )
};