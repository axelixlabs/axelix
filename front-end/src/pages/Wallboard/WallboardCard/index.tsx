import type { IInstanceCard  } from 'models';

import styles from './styles.module.css'

interface IProps {
  /**
   * Wallboard instance card data
   */
  data: IInstanceCard
}

export const WallboardCard = ({ data }: IProps) => {
  
  return (
    <div className={`${styles.Card} ${styles[`Card${data.status}`]}`}>
      <div className={`${styles.CardHeader} ${styles[`CardHeader${data.status}`]}`}>
        {data.name}
      </div>
      <div className={styles.CardBody}>
        <div>Version: {data.serviceVersion}</div>
        <div>Spring Boot: {data.springBootVersion}</div>
        <div>Java: {data.javaVersion}</div>
        <div className={styles.HashAndTimeWrapper}>
          <span>{data.commitShaShort}</span>
          <span>{data.deployedFor}</span>
        </div>
      </div>
    </div>
  )
};