#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <target-host>"
    exit 1
fi

if echo "$1" | grep -q -E '^https?://'; then
    echo "The target host must not include a URL scheme"
    exit 1
fi

target_host=$1

if http_status_code=$(curl --fail --silent --show-error --connect-timeout 10 -w "%{http_code}\n" -o /dev/null "http://${target_host}"); then
    echo "Status code for unencrypted HTTP connection attempt: $http_status_code"
    if [[ $http_status_code -ge 200 && $http_status_code -le 299 ]]; then
        echo "Unencrypted HTTP connection should not succeed"
        exit 1
    fi
fi

if ! curl --silent --show-error --connect-timeout 10 -o /dev/null "https://${target_host}"; then
    echo "Host unreachable"
    exit 1
fi

if ! openssl s_client -connect "${target_host}:443" -servername "$target_host" -tls1_3 </dev/null >/dev/null 2>&1; then
    echo "TLS 1.3 handshake should succeed"
    exit 1
fi

if ! openssl s_client -connect "${target_host}:443" -servername "$target_host" -tls1_2 </dev/null >/dev/null 2>&1; then
    echo "TLS 1.2 handshake should succeed"
    exit 1
fi

if openssl s_client -connect "${target_host}:443" -servername "$target_host" -tls1_1 </dev/null >/dev/null 2>&1; then
    echo "TLS 1.1 handshake should not succeed"
    exit 1
fi

if openssl s_client -connect "${target_host}:443" -servername "$target_host" -tls1 </dev/null >/dev/null 2>&1; then
    echo "TLS 1.0 handshake should not succeed"
    exit 1
fi

echo "TLS test succeeded"
