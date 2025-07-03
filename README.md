# Base Scala Project

## Purpose

This project is a base template for Scala 3 backend applications, designed to be built and run with [Scala CLI](https://scala-cli.virtuslab.org/). It provides a ready-to-use structure with example REST endpoints for managing workspaces and tasks, demonstrating authentication and authorization using Biscuit tokens, observability with OpenTelemetry, and PostgreSQL persistence. The workspace and task features are included as examples to help you get started quickly with your own business logic.

## Main Features

- **Example Workspace and Task Management**: Sample endpoints to illustrate API structure and database integration.
- **Authentication & Authorization**: Uses Biscuit tokens for fine-grained access control.
- **Observability**: Integrated with OpenTelemetry for distributed tracing and metrics.
- **Health Endpoints**: Exposes endpoints for health checks.
- **Configuration**: Uses PureConfig for type-safe configuration loading.

## Main Libraries Used

- **Scala 3.7.1**
- **Cats Effect** (`org.typelevel::cats-effect`)
- **Doobie** (`org.tpolecat::doobie-core`, `doobie-hikari`, `doobie-postgres`)
- **PureConfig** (`com.github.pureconfig::pureconfig-core`, `pureconfig-cats-effect`)
- **Tapir** (`com.softwaremill.sttp.tapir` and related modules for endpoints, OpenAPI, and server)
- **Circe** (`io.circe::circe-core` for JSON)
- **Log4cats** and **Logback** for logging
- **OpenTelemetry** (`org.typelevel::otel4s-oteljava`, `io.opentelemetry` modules)
- **Biscuit** (`org.biscuitsec:biscuit` for authorization tokens)

## Project Structure

- `App.scala`: Main application entry point and server setup.
- `Config.scala`: Configuration loading and case classes.
- `Tasks.scala`, `Workspaces.scala`: Example business logic for tasks and workspaces.
- `Idp.scala`: Biscuit token helpers for authorization.
- `HealthEndpoints.scala`: Health check endpoints.
- `resources/`: Contains `application.conf` and `logback.xml` for configuration and logging.

## How to Run

### Prerequisites

- Java 17 or higher
- PostgreSQL running and accessible (configure credentials in `resources/application.conf`)
- [Scala CLI](https://scala-cli.virtuslab.org/) (recommended)

### Configuration

Copy `.env.dist` to `.env` and adjust the values as needed for your environment:

| Variable              | Description                                 | Example Value                      |
|-----------------------|---------------------------------------------|------------------------------------|
| POSTGRES_USER         | Database user                               | postgres                           |
| POSTGRES_PASSWORD     | Database password                           | postgres                           |
| POSTGRES_DB           | Database name                               | mydb                               |
| HTTP_HOST             | Application host                            | localhost                          |
| HTTP_PORT             | Application port                            | 8080                               |
| BISCUIT_PRIVATE_KEY   | Biscuit private key (replace for prod)      | your_biscuit_private_key_here      |
| BISCUIT_TOKEN         | Example Biscuit token (optional, for tests) | your_biscuit_token_here            |

Alternatively, you can edit `resources/application.conf` for additional configuration options.

### Start the Application

Using Scala CLI:

```sh
scala-cli run .
```

### Running with Dockerized Services

You can use the provided scripts to start/stop all required services. By default, these scripts will start:

- **PostgreSQL** (database, port 5432)
- **Grafana OTEL-LGTM**: an all-in-one observability stack that bundles several open-source components:
  - **Loki** for logs
  - **Grafana** for visualization (UI on port 3000)
  - **Tempo** for traces
  - **Mimir** for metrics
  - **Pyroscope** for continuous profiling
  - **OpenTelemetry Collector** for ingesting telemetry data (ports 4317 for gRPC, 4318 for HTTP)

This setup provides a quick, local environment for collecting, storing, and visualizing logs, metrics, traces, and profiles using the OpenTelemetry standard.

To start the containers:

```sh
./startContainers.sh
# ... run the app ...
./stopContainer.sh
```

- Grafana will be available at [http://localhost:3000](http://localhost:3000)
- OTEL endpoints will be available at ports 4317 (gRPC) and 4318 (HTTP)

### API Documentation

Once running, the API will be available at the configured host/port (see `application.conf`). OpenAPI docs may be available at `/docs` if enabled.
