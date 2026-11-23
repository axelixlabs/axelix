/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

export type TListenerInvocation = {
    method: string;
    mode: string;
    startedAfterMs: number;
    durationMs?: number;
    error?: { type: string; message: string; stack: string };
};

export type TPublication = {
    key: string;
    type: string;
    instance: string;
    publishedAt: number;
    callSite?: string;
    listeners: TListenerInvocation[];
};

const inventory: TListenerInvocation = {
    method: "InventoryListener.onOrderCompleted",
    mode: "Synchronous",
    startedAfterMs: 0,
    durationMs: 4,
};
const billing: TListenerInvocation = {
    method: "BillingListener.onOrderCompleted",
    mode: "After commit",
    startedAfterMs: 6,
    durationMs: 18,
};
const receipt: TListenerInvocation = {
    method: "ReceiptEmailListener.onOrderCompleted",
    mode: "Async · after commit",
    startedAfterMs: 32,
    durationMs: 480,
    error: {
        type: "SocketTimeoutException",
        message: "Timed out waiting for mail gateway response.",
        stack: "java.net.SocketTimeoutException: Timed out waiting for mail gateway response\n    at checkout.notifications.ReceiptEmailListener.onOrderCompleted(ReceiptEmailListener.java:47)",
    },
};

const samples: Omit<TPublication, "key" | "publishedAt">[] = [
    {
        type: "OrderCompleted",
        instance: "checkout-7c9d",
        callSite: "CheckoutService.complete:87",
        listeners: [inventory, billing, receipt],
    },
    {
        type: "PaymentAuthorized",
        instance: "checkout-2f6a",
        callSite: "PaymentService.authorize:64",
        listeners: [
            {
                method: "PaymentLedgerListener.onPaymentAuthorized",
                mode: "Synchronous",
                startedAfterMs: 0,
                durationMs: 5,
            },
        ],
    },
    {
        type: "OrderCompleted",
        instance: "checkout-2f6a",
        listeners: [inventory, billing, { ...receipt, durationMs: 86, error: undefined }],
    },
    {
        type: "CartExpired",
        instance: "checkout-7c9d",
        listeners: [],
    },
    {
        type: "OrderCompleted",
        instance: "checkout-7c9d",
        listeners: [
            receipt,
            {
                ...billing,
                mode: "Async · after commit",
                error: {
                    type: "BillingUnavailableException",
                    message: "Billing service unavailable.",
                    stack: "checkout.billing.BillingUnavailableException: Billing service unavailable.\n    at checkout.billing.BillingListener.onOrderCompleted(BillingListener.java:54)",
                },
            },
        ],
    },
    {
        type: "ShipmentRequested",
        instance: "checkout-b184",
        listeners: [
            {
                method: "ShipmentAuditListener.onShipmentRequested",
                mode: "Synchronous",
                startedAfterMs: 0,
                durationMs: 3,
            },
            {
                method: "CarrierBookingListener.onShipmentRequested",
                mode: "Async",
                startedAfterMs: 18,
            },
        ],
    },
];

export const mockPublications: TPublication[] = Array.from({ length: 15 }, (_, index) => ({
    ...samples[index % samples.length]!,
    key: String(index),
    publishedAt: Date.UTC(2026, 10, 21, 14, 32, 4, 251) - index * 19_347,
}));
