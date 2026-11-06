#!/bin/bash
# copia_seguridad.sh - Copia lógica diaria de la base de datos biblioteca.
# Instalación:  sudo install -m 750 ~/infra/copia_seguridad.sh /usr/local/sbin/
# Programación: sudo crontab -e   ->   15 3 * * * /usr/local/sbin/copia_seguridad.sh
# Restauración: gunzip < /var/backups/biblioteca/<fichero>.sql.gz | sudo mysql
#
# Se ejecuta como root: entra en MySQL por auth_socket, sin contraseña en ningún fichero.
set -euo pipefail

DESTINO=/var/backups/biblioteca
DIAS_RETENCION=7
FICHERO="$DESTINO/biblioteca_$(date +%Y%m%d_%H%M%S).sql.gz"

install -d -m 700 "$DESTINO"   # solo root puede leer las copias (contienen datos personales)
umask 077

# --single-transaction: foto coherente de InnoDB sin bloquear a la API
# --databases: incluye CREATE DATABASE y USE, así se restaura con un simple "| mysql"
# El fichero se escribe primero como .tmp y se renombra al final: nunca queda una copia a medias
mysqldump --single-transaction --routines --triggers --events \
          --databases biblioteca | gzip > "$FICHERO.tmp"
mv "$FICHERO.tmp" "$FICHERO"

# Rotación: se borran las copias de más de DIAS_RETENCION días
find "$DESTINO" -name 'biblioteca_*.sql.gz' -mtime +"$DIAS_RETENCION" -delete

logger -t copia_seguridad "Copia creada: $FICHERO ($(du -h "$FICHERO" | cut -f1))"
echo "Copia creada: $FICHERO"
