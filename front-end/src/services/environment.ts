import apiFetch from "api/apiFetch";

export const getEnvironmentData = (id: string) => {
  return apiFetch.get(`env/feed/${id}`);
};
