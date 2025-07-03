#!/bin/bash

# Script de sauvegarde automatique de la base de données PostgreSQL
# Usage: ./backup.sh [database_name] [output_directory]

# Configuration par défaut
DEFAULT_DB="laplateforme_tracker"
DEFAULT_DIR="./backups"
DEFAULT_USER="postgres"

# Paramètres
DB_NAME=${1:-$DEFAULT_DB}
OUTPUT_DIR=${2:-$DEFAULT_DIR}
DB_USER=${3:-$DEFAULT_USER}

# Créer le répertoire de sauvegarde s'il n'existe pas
mkdir -p "$OUTPUT_DIR"

# Générer un nom de fichier avec timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$OUTPUT_DIR/${DB_NAME}_backup_$TIMESTAMP.sql"

echo "Début de la sauvegarde de la base de données: $DB_NAME"
echo "Fichier de sortie: $BACKUP_FILE"

# Exécuter pg_dump
pg_dump -h localhost -U "$DB_USER" -d "$DB_NAME" --no-password > "$BACKUP_FILE"

# Vérifier le succès de la sauvegarde
if [ $? -eq 0 ]; then
    echo "Sauvegarde réussie!"
    echo "Taille du fichier: $(du -h "$BACKUP_FILE" | cut -f1)"
    
    # Compresser le fichier de sauvegarde
    gzip "$BACKUP_FILE"
    echo "Fichier compressé: ${BACKUP_FILE}.gz"
    
    # Supprimer les anciennes sauvegardes (garder les 7 dernières)
    find "$OUTPUT_DIR" -name "${DB_NAME}_backup_*.sql.gz" -type f -mtime +7 -delete
    echo "Anciennes sauvegardes supprimées (plus de 7 jours)"
    
else
    echo "Erreur lors de la sauvegarde!"
    exit 1
fi
