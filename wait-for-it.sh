#!/usr/bin/env bash
# wait-for-it.sh host:port [--command args...]

set -e

hostport="$1"; shift
cmd=( "$@" )

host="${hostport%%:*}"
port="${hostport##*:}"

echo "Waiting for $host:$port..."

until (echo > /dev/tcp/"$host"/"$port") &>/dev/null; do
  printf '.'
  sleep 1
done

echo "Connected to $host:$port"

exec "${cmd[@]}"

