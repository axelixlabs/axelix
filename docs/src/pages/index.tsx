import { Redirect } from '@docusaurus/router';
import useBaseUrl from '@docusaurus/useBaseUrl';

export default function Home() {
  // <Redirect> is raw react-router and does not apply baseUrl, so prepend it
  // explicitly to land on /docs/product/introduction (not /product/introduction).
  return <Redirect to={useBaseUrl('/product/introduction')} />;
};
