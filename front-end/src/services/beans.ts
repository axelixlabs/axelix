import apiFetch from "api/apiFetch";

export const getBeansData = (id: string) => {
  return apiFetch.get(`beans/feed/${id}`);
};
