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
export const PROMPT = "Why does the external payment provider not accept our request?";

const WARNING = "#e8c46a";
const SUCCESS = "#8fbd4b";
const ERROR = "#f28b7d";

export const STEPS = [
    {
        note: "",
        tool: "axelix.get_logs",
        args: "logger=com.acme.payments.client",
        result: "0 entries in the last 15 min (level: WARN)",
        color: WARNING,
    },
    {
        note: "No logs at the current level. Raising it without a restart.",
        tool: "axelix.set_log_level",
        args: "com.acme.payments.client → DEBUG",
        result: "level changed live · audited",
        color: SUCCESS,
    },
    {
        note: "",
        tool: "axelix.get_logs",
        args: "logger=com.acme.payments.client",
        result: "POST /v2/charges → 401 Unauthorized · Authorization header missing",
        color: ERROR,
    },
    {
        note: "The request carries no token. Checking which client bean is wired.",
        tool: "axelix.get_beans",
        args: "type=PaymentProviderClient",
        result: "paymentProviderClient → NoOpTokenPaymentClient",
        color: ERROR,
    },
    {
        note: "The no-op implementation is active. Checking why the real one was skipped.",
        tool: "axelix.get_conditions",
        args: "OAuthPaymentClientConfig",
        result: "@ConditionalOnProperty(payments.auth.mode=oauth) did not match",
        color: ERROR,
    },
    {
        note: "",
        tool: "axelix.get_properties",
        args: "payments.auth.mode",
        result: '"oath" · source: application-prod.yml',
        color: ERROR,
    },
];
