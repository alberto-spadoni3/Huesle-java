#!/bin/bash

is_service_listening() {
    local service_host="$1"
    local service_ports="$2"
    nc -zw3 "$service_host" "$service_ports"
}

until ( ( is_service_listening "${RABBIT_HOST}" "5672" ) ); do
    echo "Service isn't listening yet. Waiting..."
    sleep 5
done

echo "Service are now listening on the corresponding port!"
echo "Starting the task $SERVICE with gradle"
gradle "$SERVICE"

