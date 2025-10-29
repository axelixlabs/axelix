package com.nucleonforge.axile.common.api.details;

/**
 * DTO that contains details about this application.
 *
 * @param serviceName   The name of the service providing information.
 * @param axileDetails  The DTO containing details of the application.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
public record TransitAxileDetails(String serviceName, AxileDetails axileDetails) {}
