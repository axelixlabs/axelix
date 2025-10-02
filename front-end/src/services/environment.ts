import apiFetch from "../api/apiFetch";

export const getEnvironmentsData = (id: string) => {
  return apiFetch.get(`env/feed/${id}`);
};
