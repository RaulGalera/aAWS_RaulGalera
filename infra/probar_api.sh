#!/bin/bash
# probar_api.sh - Batería de comprobaciones de la API de la biblioteca.
# Uso:   bash probar_api.sh http://127.0.0.1:8080        (necesita curl y jq: sudo apt install -y jq)
# Crea sus propios datos de prueba (autor, libros y socios con nombres únicos), así que
# se puede ejecutar varias veces y con cualquier estado de la base de datos.
# Las comprobaciones marcadas [VARIANTE 3] son las propias: límite de 5 préstamos,
# fecha límite = hoy + 30 e informe GET /api/libros/{id}/historial.

BASE="${1:-http://127.0.0.1:8080}"
OK=0; KO=0
CUERPO=$(mktemp); trap 'rm -f "$CUERPO"' EXIT
SUFIJO="$(date +%s)$RANDOM"
HOY=$(date +%F)

# llamar METODO RUTA [JSON]  -> deja el código en $CODIGO y el cuerpo en $CUERPO
llamar() {
  local datos=()
  [ -n "${3:-}" ] && datos=(-H 'Content-Type: application/json' --data "$3")
  CODIGO=$(curl -s -o "$CUERPO" -w '%{http_code}' -X "$1" "${datos[@]}" "$BASE$2")
}
# comprobar "descripción" CODIGO_ESPERADO [expresión jq que debe dar true]
comprobar() {
  local bien=1
  [ "$CODIGO" = "$2" ] || bien=0
  if [ $bien = 1 ] && [ -n "${3:-}" ]; then
    [ "$(jq -r "$3" "$CUERPO" 2>/dev/null)" = "true" ] || bien=0
  fi
  if [ $bien = 1 ]; then OK=$((OK+1)); printf '  OK     %-62s %s\n' "$1" "$CODIGO"
  else KO=$((KO+1)); printf '  FALLO  %-62s esperado %s, obtenido %s\n' "$1" "$2" "$CODIGO"; fi
}
campo() { jq -r "$1" "$CUERPO"; }

echo "Probando $BASE"
echo "--- Estado"
llamar GET /api/estado;                       comprobar "GET /api/estado" 200 '.versionMysql != null'
CAB=$(curl -s -D - -o /dev/null "$BASE/api/autores/999999" | tr -d '\r' | grep -i '^X-Alumno:')
CODIGO=$([ -n "$CAB" ] && echo 404 || echo 000); comprobar "Cabecera X-Alumno también en los errores" 404

echo "--- Autores"
llamar GET /api/autores;                      comprobar "GET /api/autores" 200 'length >= 1'
llamar POST /api/autores '{"nombre":""}';     comprobar "POST autor sin nombre -> 400 con detalles" 400 '.detalles | length >= 1'
llamar POST /api/autores "{\"nombre\":\"Autor prueba $SUFIJO\",\"nacionalidad\":\"Española\",\"anioNacimiento\":1970}"
comprobar "POST autor" 201;                   AUTOR=$(campo .id)
llamar GET "/api/autores/$AUTOR";             comprobar "GET /api/autores/{id}" 200
llamar GET /api/autores/999999;               comprobar "GET autor inexistente" 404 '.estado == 404'
llamar PUT "/api/autores/$AUTOR" "{\"nombre\":\"Autor prueba $SUFIJO bis\"}"; comprobar "PUT autor" 200

echo "--- Libros"
crear_libro() { # crear_libro TITULO EJEMPLARES -> id en $LIBRO
  local isbn; isbn=$(printf '97%011d' $(( (SUFIJO + RANDOM * 7919 + $2) % 100000000000 )))
  llamar POST /api/libros "{\"isbn\":\"$isbn\",\"titulo\":\"$1 $SUFIJO\",\"genero\":\"Prueba\",\"ejemplares\":$2,\"autorId\":$AUTOR}"
  LIBRO=$(campo .id); ISBN=$isbn
}
crear_libro "Libro A" 1;                      comprobar "POST libro" 201 '.disponibles == 1';  LIBRO_A=$LIBRO; ISBN_A=$ISBN
llamar POST /api/libros "{\"isbn\":\"123\",\"titulo\":\"X\",\"genero\":\"X\",\"ejemplares\":1,\"autorId\":$AUTOR}"
comprobar "POST libro con ISBN de menos de 13 dígitos" 400
llamar POST /api/libros "{\"isbn\":\"$ISBN_A\",\"titulo\":\"X\",\"genero\":\"X\",\"ejemplares\":1,\"autorId\":$AUTOR}"
comprobar "POST libro con ISBN repetido" 409
llamar POST /api/libros '{"isbn":"9780000000000","titulo":"X","genero":"X","ejemplares":1,"autorId":999999}'
comprobar "POST libro con autor inexistente" 400
llamar GET "/api/libros?titulo=libro%20a%20$SUFIJO"; comprobar "GET /api/libros?titulo= (parcial, sin mayúsculas)" 200 'length == 1'
llamar GET "/api/libros?autorId=$AUTOR&genero=prueba"; comprobar "GET /api/libros?autorId=&genero= combinados" 200 'length == 1'
llamar GET /api/libros/999999;                comprobar "GET libro inexistente" 404
llamar GET "/api/autores/$AUTOR/libros";      comprobar "GET /api/autores/{id}/libros" 200 'length == 1'
llamar DELETE "/api/autores/$AUTOR";          comprobar "DELETE autor con libros" 409

echo "--- Socios"
crear_socio() { llamar POST /api/socios "{\"nombre\":\"Socio $1 $SUFIJO\",\"email\":\"socio.$1.$SUFIJO@example.com\"}"; SOCIO=$(campo .id); }
crear_socio uno;                              comprobar "POST socio (fecha de alta = hoy)" 201 ".fechaAlta == \"$HOY\""; SOCIO1=$SOCIO
llamar POST /api/socios "{\"nombre\":\"X\",\"email\":\"socio.uno.$SUFIJO@example.com\"}"; comprobar "POST socio con email repetido" 409
llamar POST /api/socios '{"nombre":"X","email":"no-es-un-email"}'; comprobar "POST socio con email mal formado" 400
llamar GET /api/socios;                       comprobar "GET /api/socios" 200 'length >= 1'
crear_socio dos;                              SOCIO2=$SOCIO

echo "--- Préstamos"
llamar POST /api/prestamos "{\"libroId\":999999,\"socioId\":$SOCIO1}"; comprobar "POST préstamo con libro inexistente" 400
llamar POST /api/prestamos "{\"libroId\":$LIBRO_A,\"socioId\":999999}"; comprobar "POST préstamo con socio inexistente" 400
llamar POST /api/prestamos "{\"libroId\":$LIBRO_A,\"socioId\":$SOCIO1}"; comprobar "POST préstamo" 201 '.estado == "ACTIVO"'; P1=$(campo .id)
llamar POST /api/prestamos "{\"libroId\":$LIBRO_A,\"socioId\":$SOCIO1}"; comprobar "POST préstamo del mismo libro ya abierto" 409
llamar POST /api/prestamos "{\"libroId\":$LIBRO_A,\"socioId\":$SOCIO2}"; comprobar "POST préstamo sin ejemplares disponibles" 409
llamar PUT "/api/libros/$LIBRO_A" "{\"isbn\":\"$ISBN_A\",\"titulo\":\"X\",\"genero\":\"Prueba\",\"ejemplares\":0,\"autorId\":$AUTOR}"
comprobar "PUT libro con menos ejemplares que préstamos abiertos" 409
llamar DELETE "/api/socios/$SOCIO1";          comprobar "DELETE socio con préstamos" 409
llamar DELETE "/api/libros/$LIBRO_A";         comprobar "DELETE libro que se ha prestado" 409
llamar GET "/api/prestamos/$P1";              comprobar "GET /api/prestamos/{id}" 200
llamar GET "/api/prestamos?estado=ACTIVO";    comprobar "GET /api/prestamos?estado=ACTIVO" 200 'all(.[]; .estado == "ACTIVO")'
llamar GET "/api/prestamos?estado=RARO";      comprobar "GET /api/prestamos?estado= no válido" 400
llamar GET "/api/socios/$SOCIO1/prestamos";   comprobar "GET /api/socios/{id}/prestamos" 200 'length == 1'
llamar PUT "/api/prestamos/$P1/devolucion";   comprobar "PUT devolución" 200 '.estado == "DEVUELTO"'
llamar PUT "/api/prestamos/$P1/devolucion";   comprobar "PUT devolución de un préstamo ya devuelto" 409
llamar PUT /api/prestamos/999999/devolucion;  comprobar "PUT devolución de un préstamo inexistente" 404

echo "--- Comprobaciones propias [VARIANTE 3]"
# 1. Fecha límite = hoy + 30 días
crear_libro "Libro B" 1; LIBRO_B=$LIBRO
llamar POST /api/prestamos "{\"libroId\":$LIBRO_B,\"socioId\":$SOCIO2}"
comprobar "[V3] fecha límite de un préstamo nuevo = hoy + 30" 201 ".fechaLimite == \"$(date -d '+30 days' +%F)\""
# 2. Límite de 5 préstamos abiertos: socio sin préstamos, 5 libros distintos -> 201, el 6.º -> 409
crear_socio limite; SOCIO3=$SOCIO; BIEN=0
for n in 1 2 3 4 5; do
  crear_libro "Libro limite $n" 1
  llamar POST /api/prestamos "{\"libroId\":$LIBRO,\"socioId\":$SOCIO3}"; [ "$CODIGO" = 201 ] && BIEN=$((BIEN+1))
done
CODIGO=$([ $BIEN = 5 ] && echo 201 || echo "$BIEN de 5"); comprobar "[V3] 5 préstamos abiertos del mismo socio" 201
crear_libro "Libro limite 6" 1
llamar POST /api/prestamos "{\"libroId\":$LIBRO,\"socioId\":$SOCIO3}"; comprobar "[V3] el 6.º préstamo abierto se rechaza" 409
# 3. Informe historial: del más reciente al más antiguo, con estado
llamar GET "/api/libros/$LIBRO_A/historial"
comprobar "[V3] GET /api/libros/{id}/historial" 200 'length == 1 and .[0].estado == "DEVUELTO"'
llamar GET /api/libros/1/historial
comprobar "[V3] historial ordenado por fecha descendente" 200 '[.[].fechaPrestamo] == ([.[].fechaPrestamo] | sort | reverse)'
# 4. Historial de un libro inexistente -> 404 (y de uno nunca prestado -> lista vacía)
llamar GET /api/libros/999999/historial;      comprobar "[V3] historial de un libro inexistente" 404
llamar GET "/api/libros/$LIBRO/historial";    comprobar "[V3] historial de un libro nunca prestado = []" 200 'length == 0'

echo
echo "Resultado: $OK correctas, $KO fallidas"
[ "$KO" = 0 ]
