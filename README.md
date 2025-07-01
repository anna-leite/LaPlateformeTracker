# LaPlateformeTracker

## 🛠 Setup

### 1. Prerequisites

Before you begin, make sure you have the following software installed:

- Java 17 (or later)  
- Apache Maven
- PostgreSQL 12+  
- Git  

### 2. Clone the Repository

```bash
git clone https://github.com/anna-leite/LaPlateformeTracker.git
cd LaPlateformeTracker
```

### 3. Configure the Database

1. Start PostgreSQL

    **On macOS (Homebrew):**

    ```bash
    brew services start postgresql
    ```

    **On Linux (systemd):**

    ``` bash
    sudo systemctl start postgresql
    ```

    **On Windows:**

    Open “pgAdmin” or run “SQL Shell (psql)” from your Start menu.

2. Create a Database User & Database

    Open a psql shell as the postgres superuser:

    ``` bash
    psql -U postgres
    ```

    Inside psql, run:

    ``` sql
    -- Create a dedicated user
    CREATE ROLE appuser WITH LOGIN PASSWORD 'appPassword';

    -- Create the application database
    CREATE DATABASE studentdb OWNER appuser;

    -- Grant all privileges to your user
    GRANT ALL PRIVILEGES ON DATABASE studentdb TO appuser;
    ```
3. Initialize Schema

    Exit the psql shell (\q) and apply the schema SQL script:

    ```bash
    psql -U appuser -d studentdb -f sql/init-schema.sql
    ```

4. (Optional) Seed Sample Data

    If you want initial test data (including an admin user), run:

    ```bash
    psql -U appuser -d studentdb -f sql/seed-data.sql
    ```
### 4. Configure the Application

Copy the example configuration file:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Open src/main/resources/application.properties in your editor and update the database settings if needed:

``` properties
# JDBC settings
db.url=jdbc:postgresql://localhost:5432/studentdb
db.user=appuser
db.password=appPassword

# Application settings
app.pageSize=20
backup.dir=backups
```
### 5. Build and Run

From the project root:

``` bash
# Build the project (compile + package)
mvn clean package

# Run the JavaFX application
mvn javafx:run
```

Alternatively, if you prefer to run the JAR directly:

``` bash
java -jar target/LaPlateformeTracker-1.0.0.jar
```
### 6. Backup & Restore

- Manual Backup

    ``` bash
    ./scripts/backup.sh
    ```

    This creates a timestamped `.sql` dump in the `backups/` folder.

- Restore from Backup

    ```bash
    ./scripts/restore.sh backups/2023-07-15_153000_studentdb.sql
    ```
🎉 You’re now ready to log in and use the LaPlateforme Tracker app! 🎉