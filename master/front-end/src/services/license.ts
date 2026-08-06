import { apiFetch } from "api";

export const checkLicense = (licenseKey: string) => {
    return apiFetch.post("license/check", {
        licenseKey: licenseKey
    });
};

export const sendLicense = (licenseKey: string) => {
    return apiFetch.post("license", {
        licenseKey: licenseKey
    });
};

