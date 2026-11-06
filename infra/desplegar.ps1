# desplegar.ps1 - Compila la API en el puesto, copia el jar a la instancia y reinicia el servicio.
# Uso (desde la raíz del repositorio):  .\infra\desplegar.ps1
# Requisitos: el servicio ya instalado (fase 6) y la regla del puerto 22 con tu IP actual.
param(
    [string]$Ip    = '34.199.245.196',          # IP elástica de ad-RaulGalera
    [string]$Clave = 'C:\aws\labsuser.pem'
)
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot

# 1. Compilar (sin tests: el test de contexto necesita la BD)
Push-Location "$raiz\biblioteca-api"
try { .\mvnw.cmd -q -B clean package -DskipTests; if ($LASTEXITCODE) { throw "Fallo al compilar" } }
finally { Pop-Location }

# 2. Copiar el jar a /tmp de la instancia (el usuario ubuntu no puede escribir en /opt/biblioteca)
scp -i $Clave "$raiz\biblioteca-api\target\biblioteca.jar" "ubuntu@${Ip}:/tmp/biblioteca.jar"
if ($LASTEXITCODE) { throw "Fallo al copiar el jar (¿ha cambiado tu IP? revisa el security group)" }

# 3. Instalarlo con el propietario correcto y reiniciar el servicio
ssh -i $Clave "ubuntu@$Ip" 'sudo install -o biblioteca -g biblioteca -m 644 /tmp/biblioteca.jar /opt/biblioteca/biblioteca.jar && rm /tmp/biblioteca.jar && sudo systemctl restart biblioteca'
if ($LASTEXITCODE) { throw "Fallo al instalar o reiniciar" }

# 4. Esperar a que responda /api/estado
for ($i = 1; $i -le 30; $i++) {
    Start-Sleep 1
    $codigo = curl.exe -s -o NUL -w '%{http_code}' --max-time 3 "http://${Ip}:8080/api/estado"
    if ($codigo -eq '200') { "Desplegado: /api/estado responde 200 tras $i s"; exit 0 }
}
throw "La API no responde; mira: ssh -i $Clave ubuntu@$Ip 'journalctl -u biblioteca -n 50'"
