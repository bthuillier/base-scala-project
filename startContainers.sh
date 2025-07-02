#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "Starting containers..."

# Start PostgreSQL container
echo "Starting PostgreSQL..."
docker run -d \
  --name postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=mydb \
  -p 5432:5432 \
  postgres:latest

# Start Grafana OTEL-LGTM container
echo "Starting Grafana OTEL-LGTM..."
docker run -d \
  --name otel-lgtm \
  -p 3000:3000 \
  -p 4317:4317 \
  -p 4318:4318 \
  grafana/otel-lgtm:latest

echo "Containers started successfully!"
echo "PostgreSQL is running on port 5432"
echo "Grafana is available at http://localhost:3000"
echo "OTEL endpoints are available at port 4317 (gRPC) and 4318 (HTTP)"