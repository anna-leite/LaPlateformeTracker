#!/usr/bin/env bash
#
# backup.sh
# Creates a timestamped pg_dump of the laplat_tracker_db database,
# compresses it, and keeps the last N backups.
#
# Usage: ./backup.sh [output-directory]
# If no output directory is given, defaults to ./backups

set -euo pipefail

# 1. CONFIGURATION
# ──────────────────────────────────────────────────────────────────────────────

# Database connection settings
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGUSER="${PGUSER:-laplat_tracker_user}"
PGDATABASE="${PGDATABASE:-laplat_tracker_db}"
# Uncomment if prefer to pass password via PGPASSWORD env var:
# export PGPASSWORD="ChangeMe123!"

# Number of backups to keep (oldest beyond this number will be pruned)
MAX_BACKUPS=3

# Destination folder (first script argument, or default)
DEST_DIR="${1:-$(dirname "$0")/backups}"

# 2. PREPARE DESTINATION
# ──────────────────────────────────────────────────────────────────────────────

mkdir -p "$DEST_DIR"
cd "$DEST_DIR"

# 3. RUN pg_dump AND COMPRESS
# ──────────────────────────────────────────────────────────────────────────────

# Timestamp format: YYYYMMDD_HHMMSS
TIMESTAMP="$(date +'%Y%m%d_%H%M%S')"
FILENAME="${PGDATABASE}_${TIMESTAMP}.sql.gz"

echo "[$(date +'%F %T')] Starting backup of '$PGDATABASE' to '$DEST_DIR/$FILENAME'..."

pg_dump \
  --host="$PGHOST" \
  --port="$PGPORT" \
  --username="$PGUSER" \
  --format=plain \
  --no-owner \
  --no-privileges \
  "$PGDATABASE" \
| gzip > "$FILENAME"

echo "[$(date +'%F %T')] Backup completed successfully."

# 4. PRUNE OLD BACKUPS
# ──────────────────────────────────────────────────────────────────────────────

echo "[$(date +'%F %T')] Pruning backups, keeping the latest $MAX_BACKUPS files..."
ls -1tr ${PGDATABASE}_*.sql.gz \
  | head -n -"$MAX_BACKUPS" \
  | xargs -r rm --verbose

echo "[$(date +'%F %T')] Done."
