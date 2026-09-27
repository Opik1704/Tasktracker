#!/bin/bash

CONTAINER_NAME="taskmanager-db"
DB_USER="${POSTGRES_USER:-postgres}"
DB_NAME="${POSTGRES_DB:-tasks}"

BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/db_backup_$DATE.dump"

mkdir -p $BACKUP_DIR

echo "=== Запуск бэкапа базы данных $DB_NAME из контейнера $CONTAINER_NAME ==="

docker exec -t $CONTAINER_NAME pg_dump -U $DB_USER -d $DB_NAME -F c -b > $BACKUP_FILE

if [ $? -eq 0 ]; then
  echo "Бэкап успешно создан: $BACKUP_FILE"
else
  echo "Ошибка при создании бэкапа!"
  exit 1
fi

find $BACKUP_DIR -type f -name "*.dump" -mtime +7 -delete

echo "=== Ротация завершена (удалены файлы старше 7 дней) ==="