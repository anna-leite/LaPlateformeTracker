#!/bin/bash

# Script de restauration de la base de données PostgreSQL
# Usage: ./restore.sh [backup_file] [database_name] [user]

# Configuration par défaut
DEFAULT_DB="laplateforme_tracker"
DEFAULT_USER="postgres"

# Paramètres
BACKUP_FILE=$1
DB_NAME=${2:-$DEFAULT_DB}
DB_USER=${3:-$DEFAULT_USER}

# Vérifier que le fichier de sauvegarde est fourni
if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: $0 <backup_file> [database_name] [user]"
    echo "Exemple: $0 ./backups/laplateforme_tracker_backup_20231215_120000.sql.gz"
    exit 1
fi

# Vérifier que le fichier existe
if [ ! -f "$BACKUP_FILE" ]; then
    echo "Erreur: Le fichier de sauvegarde '$BACKUP_FILE' n'existe pas!"
    exit 1
fi

echo "Début de la restauration..."
echo "Fichier de sauvegarde: $BACKUP_FILE"
echo "Base de données: $DB_NAME"
echo "Utilisateur: $DB_USER"

# Demander confirmation
read -p "Êtes-vous sûr de vouloir restaurer la base de données '$DB_NAME'? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Restauration annulée."
    exit 0
fi

# Arrêter toutes les connexions actives à la base de données
echo "Fermeture des connexions actives..."
psql -h localhost -U "$DB_USER" -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DB_NAME' AND pid <> pg_backend_pid();"

# Supprimer la base de données existante
echo "Suppression de la base de données existante..."
dropdb -h localhost -U "$DB_USER" "$DB_NAME" 2>/dev/null

# Recréer la base de données
echo "Création de la nouvelle base de données..."
createdb -h localhost -U "$DB_USER" "$DB_NAME"

if [ $? -ne 0 ]; then
    echo "Erreur lors de la création de la base de données!"
    exit 1
fi

# Restaurer les données
echo "Restauration des données..."

# Vérifier si le fichier est compressé
if [[ "$BACKUP_FILE" == *.gz ]]; then
    gunzip -c "$BACKUP_FILE" | psql -h localhost -U "$DB_USER" -d "$DB_NAME"
else
    psql -h localhost -U "$DB_USER" -d "$DB_NAME" < "$BACKUP_FILE"
fi

# Vérifier le succès de la restauration
if [ $? -eq 0 ]; then
    echo "Restauration réussie!"
    echo "La base de données '$DB_NAME' a été restaurée à partir de '$BACKUP_FILE'"
else
    echo "Erreur lors de la restauration!"
    exit 1
fi
