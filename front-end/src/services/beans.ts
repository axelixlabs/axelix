import apiFetch from "api/apiFetch";

export const getBeansData = (instanceId: string) => {
  return apiFetch.get(`beans/feed/${instanceId}`);
};
