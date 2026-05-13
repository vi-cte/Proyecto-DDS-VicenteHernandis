# Votify Backend (Spring Boot)

Backend REST con arquitectura por capas:
- `controller` (API REST JSON)
- `service` (logica de negocio)
- `repository` (acceso a datos JPA)
- `entity` (modelo persistente)
- `dto` (contratos de entrada/salida)

## Endpoints MVP
- `GET /api/participants`
- `POST /api/participants`
- `POST /api/votes`
- `GET /api/results`

## Ejecutar
```powershell
cd backend
mvn spring-boot:run
```

## Configuracion BD (PostgreSQL por defecto)
Variables opcionales:
- `DB_URL` (default `jdbc:postgresql://localhost:5432/votify`)
- `DB_USER` (default `postgres`)
- `DB_PASSWORD` (default `admin`)
- `DB_DRIVER` (default `org.postgresql.Driver`)
- `SERVER_PORT` (default `8080`)

## Ejecutar con PostgreSQL
Ejemplo:
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/votify"
$env:DB_USER="postgres"
$env:DB_PASSWORD="admin"
$env:DB_DRIVER="org.postgresql.Driver"
mvn spring-boot:run
```

## Cambiar a MySQL
Ejemplo:
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/votify?useSSL=false&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASSWORD="root"
$env:DB_DRIVER="com.mysql.cj.jdbc.Driver"
mvn spring-boot:run
```

## Ejemplos de pruebas rapidas
```powershell
curl -X POST http://localhost:8080/api/participants `
  -H "Content-Type: application/json" `
  -d "{\"teamName\":\"Equipo A\",\"email\":\"a@demo.com\",\"address\":\"Calle 1\",\"phone\":\"123\",\"members\":[\"Ana\",\"Luis\"]}"
```

```powershell
curl -X POST http://localhost:8080/api/votes `
  -H "Content-Type: application/json" `
  -d "{\"selections\":[\"Equipo A\"]}"
```

```powershell
curl http://localhost:8080/api/results
```
