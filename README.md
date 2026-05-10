# Statements Service

A Spring Boot service for generating, storing and downloading account statements as PDF files.

The service supports:

- Generating account statements for a given account and period
- Batch generation of monthly statements
- PDF statement storage
- Time-limited JWT-protected statement download links
- PostgreSQL persistence

## Run with Docker Compose

The easiest way to run the full application stack is with Docker Compose.

This starts:

- PostgreSQL
- Statements Spring Boot application

Run:
bash docker compose up --build

The API will be available at:  http://localhost:8080

PostgreSQL will be exposed locally at: localhost:5432

Docker Compose uses the following database configuration:

| Property | Value |
|---|---|
| Database | `statements_db` |
| Username | `statements_user` |
| Password | `statements_password` |
| Host inside Docker | `postgres` |
| Port | `5432` |

## Docker Volumes

The Docker Compose setup creates two named volumes:

| Volume | Purpose |
|---|---|
| `statements-postgres-data` | Stores PostgreSQL data |
| `statements-generated-files` | Stores generated PDF statement files |

This allows data and generated statements to survive container restarts.

## API Endpoints

Base URL:
### Generate a Statement

Generates a statement for a specific account and period.
POST /api/statements/generate

Example:
bash curl -X POST [http://localhost:8080/api/statements/generate](http://localhost:8080/api/statements/generate)
-H "Content-Type: application/json"
-d '{ "accountNumber": 100001, "startDate": "2026-04-01", "endDate": "2026-04-30" }'


### Generate Monthly Statements in Batch

Generates statements for all available transaction records.
POST /api/statements/batch/generate

Example:
bash curl -X POST [http://localhost:8080/api/statements/batch/generate](http://localhost:8080/api/statements/batch/generate)


### Get a Download Link

Returns a time-limited JWT-protected download URL.
GET /api/statements/download

Query parameters:

| Parameter | Description |
|---|---|
| `accountNumber` | Account number |
| `periodStart` | Statement period start date in `yyyy-MM-dd` format |
| `periodEnd` | Statement period end date in `yyyy-MM-dd` format |

Example:
bash curl [http://localhost:8080/api/statements/download?accountNumber=100001&periodStart=2026-04-01&periodEnd=2026-04-30](http://localhost:8080/api/statements/download?accountNumber=100001&periodStart=2026-04-01&periodEnd=2026-04-30)


### Download Statement PDF

Downloads the generated PDF using the JWT-protected link.
GET /api/statements/{statementId}/file?token={jwt}

Example:
bash curl -L "[http://localhost:8080/api/statements/7ef6b731-596b-44a2-8df3-2f80f73525e3/file?token=eyJhbGciOiJIUzI1NiJ9](http://localhost:8080/api/statements/7ef6b731-596b-44a2-8df3-2f80f73525e3/file?token=eyJhbGciOiJIUzI1NiJ9)..."
--output statement.pdf

## Available Test Data

The application includes sample transaction data that can be used to generate and test statements.

Use the following account numbers and statement periods when calling the generate or download endpoints.

| Account Number | Customer ID | Period Start | Period End | Currency | Opening Balance | Closing Balance |
|---:|---|---|---|---|---:|---:|
| `100001` | `customer-001` | `2026-03-01` | `2026-03-31` | `ZAR` | `1500.00` | `1720.00` |
| `100001` | `customer-001` | `2026-04-01` | `2026-04-30` | `ZAR` | `1720.00` | `2100.00` |
| `100002` | `customer-002` | `2026-02-01` | `2026-02-28` | `ZAR` | `1800.00` | `2100.00` |
| `100003` | `customer-003` | `2026-01-01` | `2026-01-31` | `ZAR` | `500.00` | `950.00` |
| `100004` | `customer-004` | `2026-01-01` | `2026-01-31` | `ZAR` | `3000.00` | `2550.00` |
| `100007` | `customer-001` | `2026-04-01` | `2026-04-30` | `ZAR` | `1720.00` | `2100.00` |

---