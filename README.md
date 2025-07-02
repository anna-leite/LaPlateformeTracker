# LaPlateformeTracker

A JavaFX application to manage students (CRUD, sorting, search, stats, import/export, backups) backed by PostgreSQL.

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

#### 3.1. Start PostgreSQL

    **On macOS (Homebrew):**

```bash
brew services start postgresql
```

**On Linux (systemd):**

``` bash
sudo systemctl start postgresql
```

**On Windows:**

Start “PostgreSQL” service, or launch pgAdmin / SQL Shell (psql).

#### 3.2. Create a Database User & Database

1. Switch to the `postgres` system user:

    ``` bash
    sudo -i -u postgres
    ```

2. At the `psql` prompt, run (replace names/passwords as desired):

    ``` sql
    -- 1) Create a dedicated application user
    CREATE ROLE laplat_tracker_user
        WITH LOGIN
            PASSWORD 'ChangeMe123'
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOINHERIT;

    -- 2) Create the application database owned by that user
    CREATE DATABASE laplat_tracker_db
        WITH OWNER = laplat_tracker_user
            ENCODING = 'UTF8'
            LC_COLLATE = 'en_US.UTF-8'
            LC_CTYPE = 'en_US.UTF-8'
            TEMPLATE = template0;

    -- 3) Grant privileges (optional—owner already has full rights)
    GRANT ALL PRIVILEGES
        ON DATABASE laplat_tracker_db
        TO laplat_tracker_user;
    ```

3. Exit the postgres shell:

    ``` bash
    \q
    exit
    ```

#### 3.3 Initialize Schema & (Optional) Seed Data

Now that your database and user exist, run:

``` bash
# Apply DDL, triggers, etc.
psql "postgresql://laplat_tracker_user:ChangeMe123@localhost:5432/laplat_tracker_db" \
-f sql/init-schema.sql

# (Optional) Insert sample students & admin user
psql "postgresql://laplat_tracker_user:ChangeMe123@localhost:5432/laplat_tracker_db" \
-f sql/seed-data.sql
```

### 4. Configure the Application

1. Copy the example properties file:

    ```bash
    cp src/main/resources/application.properties.example \
    src/main/resources/application.properties
    ```

Open `src/main/resources/application.properties` and set:

``` properties
# JDBC settings (point at the DB you just created)
db.url=jdbc:postgresql://localhost:5432/laplat_tracker_db
db.user=laplat_tracker_user
db.password=ChangeMe123

# App settings
app.pageSize=20
backup.dir=backups
backup.cron=@daily
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
    ./scripts/restore.sh backups/2023-07-15_153000_laplat_tracker_db.sql
    ```
-------

🎉 You’re all set! Open the LaPlateforme Tracker app, log in with the seeded admin (if you ran seed-data.sql), and start managing students. 🎉