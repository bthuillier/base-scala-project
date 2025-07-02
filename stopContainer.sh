#!/bin/bash

# Stop the two containers
echo "Stopping containers..."

# Stop the otel-lgtm container if it exists
if docker ps -a | grep -q otel-lgtm; then
  echo "Stopping and removing otel-lgtm container..."
  docker stop otel-lgtm
  docker rm otel-lgtm
fi

# Stop the PostgreSQL container if it exists
if docker ps -a | grep -q postgres; then
  echo "Stopping and removing postgres container..."
  docker stop postgres
  docker rm postgres
fi

echo "Containers stopped and removed successfully."