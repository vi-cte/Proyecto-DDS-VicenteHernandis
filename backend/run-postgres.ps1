$env:DB_URL = "jdbc:postgresql://localhost:5432/votify"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "admin"
$env:DB_DRIVER = "org.postgresql.Driver"

Set-Location "C:\Users\vicen\Desktop\Votify\backend"
mvn spring-boot:run
