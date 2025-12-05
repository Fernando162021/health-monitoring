# Health Monitoring API

Real-time health monitoring system for IoT medical devices with automatic alerts and reports.

---

## Project Description

REST API developed with Spring Boot to monitor patients' vital signs in real time. The system receives data from IoT devices, triggers automatic alerts when values exceed configurable thresholds, and provides aggregated reports.

**Main Features:**
- Registration and management of medical devices
- Real-time reception of vital signs (HR, SpO₂, temperature, steps)
- Automatic anomaly detection with alert system
- Configurable thresholds per metric
- Aggregated reports with time windows (5, 15, 60 min)
- Data export in CSV format
- GraphQL subscriptions for real-time updates
- JWT-based authentication

---

## Used Technologies

### Backend
- **Java 21**
- **Spring Boot 3.2.0**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring GraphQL
- **PostgreSQL** - Relational Database
- **Lombok** - Less boilerplate code
- **MapStruct** - Mapping DTOs and entities
- **JWT (jjwt)** - Authentication
### Testing & Tools
- Maven
- SLF4J - structured logging
- Postman - API testing

---

## Database Schema

### Tables

#### 1. **users**
System users for authentication.
```sql
- id (BIGINT, PK)
- username (VARCHAR, UNIQUE)
- password (VARCHAR) - Hashed with BCrypt
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### 2. **device**
Medical IoT devices registered in the system.
```sql
- id (BIGINT, PK)
- device_id (VARCHAR, UNIQUE)
- is_active (BOOLEAN) - Active/inactive status
- last_vital_id (BIGINT, FK)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### 3. **vital**
Vital signs data received from devices.
```sql
- id (BIGINT, PK)
- device_id (VARCHAR, FK)
- device (FK → device.id)
- heart_rate (DOUBLE) - Heart rate (bpm)
- oxygen_level (DOUBLE) - Oxygen saturation (%)
- body_temperature (DOUBLE) - Body temperature (°C)
- steps (INTEGER) - Step counter
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### 4. **alert**
Alerts generated when vitals exceed thresholds.
```sql
- id (BIGINT, PK)
- device_id (VARCHAR)
- device (FK → device.id)
- vital (FK → vital.id)
- metric (VARCHAR) - HEART_RATE, OXYGEN_LEVEL, BODY_TEMPERATURE
- value (DOUBLE) - Value that triggered the alert
- threshold (VARCHAR) - "ABOVE X" or "BELOW Y"
- triggered_at (TIMESTAMP)
- acknowledged (BOOLEAN) - Acknowledged or not
- acknowledged_at (TIMESTAMP)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### 5. **threshold**
Thresholds for each vital metric.
```sql
- id (BIGINT, PK)
- metric (VARCHAR, UNIQUE) - Metric name
- min_value (DOUBLE) - Minimum allowed value
- max_value (DOUBLE) - Maximum allowed value
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

## Getting Started

### Pre-requisites
- Java 21+
- Maven 3.8+
- PostgreSQL 15+

### 1. Clone the repository
```bash
git clone <repository-url>
cd health-monitoring-api
```

### 2. Configure database connection

**Option B: Modify `application-dev.yml`**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/healthmonitoring_db
    username: postgres
    password: your_password
```

### 3. Execute application
```bash
mvn clean install
mvn spring-boot:run
```

The application will be running at `http://localhost:8080`

---

## Main API Endpoints

### Authentication
```
POST   /api/auth/login          - Login and get JWT token
POST   /api/auth/refresh        - Refresh token
POST   /api/auth/register       - Register new user (admin only)
```

### Devices
```
GET    /api/devices                      - All devices with the latest vital
GET    /api/devices/status               - Devices status (OK/ALERT)
GET    /api/devices/{id}/history         - Historical vitals for device
POST   /api/devices                      - Register device
POST   /api/devices/data                 - Send vitals from device
PATCH  /api/devices/{deviceId}/ack       - Acknowledge alerts for device
DELETE /api/devices/{deviceId}           - Delete device (soft delete)
```

### Alerts
```
GET    /api/alerts/active                - Active alerts
GET    /api/alerts/history?hours=24      - Historical alerts
GET    /api/alerts/device/{deviceId}     - Alerts for device
```

### Reports
```
GET    /api/reports?deviceId=X&window=Y          - JSON report
GET    /api/reports/export?deviceId=X&window=Y   - Export CSV
```

**CSV Export Details:**
- **Endpoint:** `GET /api/reports/export`
- **Query Parameters:**
  - `deviceId` (required) - Device identifier
  - `window` (optional, default: 5) - Time window in minutes (5, 15, or 60)
  
**Request Headers:**
```
Authorization: Bearer {your_jwt_token}
```

**Response Headers:**
```
Content-Type: text/csv
Content-Disposition: attachment; filename="report-{deviceId}.csv"
```

### Thresholds
```
GET    /api/thresholds                   - Get current thresholds
PUT    /api/thresholds                   - Update threshold values
```

### Health Check
```
GET    /api/health                       - System health status
```

**GraphiQL UI:** `http://localhost:8080/graphiql`

---

## Authentication

All endpoints (except `/api/auth/**`) require JWT authentication.

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "..."
}
```

### Use Token
Include the token in the `Authorization` header for subsequent requests:
```
Authorization: Bearer {token}
```

---

## Example of full workflow with cURL

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin","password":"admin123"}'
```

### 2. Register Device
```bash
curl -X POST http://localhost:8080/api/devices \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"P-102"}'
```

### 3. Send Vital Signs
```bash
curl -X POST http://localhost:8080/api/devices/vitals \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "P-102",
    "heartRate": 150.0,
    "oxygenLevel": 98.0,
    "bodyTemperature": 36.8,
    "steps": 1500
  }'
```

**Result**: If any vital sign exceeds the thresholds, an alert will be generated.

### 4. Check Active Alerts
```bash
curl http://localhost:8080/api/alerts/active \
  -H "Authorization: Bearer {token}"
```

### 5. Acknowledge Alerts
```bash
curl -X PATCH http://localhost:8080/api/devices/P-102/ack \
  -H "Authorization: Bearer {token}"
```

### 6. Get Report
```bash
curl "http://localhost:8080/api/reports?deviceId=P-102&window=60" \
  -H "Authorization: Bearer {token}"
```

### 7. Export CSV
```bash
curl "http://localhost:8080/api/reports/export?deviceId=P-102&window=60" \
  -H "Authorization: Bearer {token}" \
  -o report.csv
```

**Response Headers:**
```
Content-Type: text/csv
Content-Disposition: attachment; filename="report-P-102.csv"
```

**Expected Response:** CSV file with vital signs data

**CSV Format:**
```csv
Device ID,Timestamp,Heart Rate,Oxygen Level,Body Temperature,Steps
P-102,2025-11-27 14:30:00,75.0,98.5,36.8,1234
P-102,2025-11-27 14:29:00,72.0,97.2,36.7,1200
P-102,2025-11-27 14:28:00,78.0,98.0,36.9,1250
```

**Note:** The `-o report.csv` flag saves the response to a file named `report.csv`. The server automatically sets the `Content-Disposition` header to trigger file download in browsers.

---

## GraphQL Subscriptions

### Vitals in real time
```graphql
subscription {
  liveVitals {
    id
    heartRate
    oxygenLevel
    bodyTemperature
    steps
    createdAt
  }
}
```

### Alerts in real time
```graphql
subscription {
  liveAlerts {
    id
    deviceId
    metric
    value
    threshold
    triggeredAt
    acknowledged
  }
}
```

**WebSocket URL:** `ws://localhost:8080/graphql`

## Node-RED Integration

This API is designed to integrate with Node-RED Dashboard:

1. **Use GraphQL Subscriptions** for real-time vitals and alerts, and devices query.
2. **Consume REST endpoints** for:
   - KPI cards (device count, active alerts, avg HR)
   - Patient table with status
   - Vital history (drill-down)
   - Reports with time windows
   - CSV export

**WebSocket:** `ws://localhost:8080/graphql`

## Troubleshooting

### Error: "Connection refused"
- Verify PostgreSQL is running
- Check credentials in `application-dev.yml`

### Error: "401 Unauthorized"
- Verify the JWT token is valid
- Token expires in 24 hours and refresh token expires in 7 days, login again

### Alerts not being created
- Verify thresholds exist: `GET /api/thresholds`
- Verify device is active
- Send out-of-range values to trigger alerts

## License

This project was developed as part of an academic project.

## Author

Fernando Martin Alcocer Quintero
