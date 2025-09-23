import apiFetch from "../api/apiFetch";

export const getEnvironmantData = (id: string) => {
  return apiFetch.get(`env/feed/${id}`);
};
