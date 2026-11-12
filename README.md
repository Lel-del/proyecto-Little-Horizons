# ERP Colegio Internacional Little Horizons

Sistema de Planificación de Recursos Empresariales (ERP) educativo desarrollado en Java como aplicación de consola, con arquitectura MVC y base de datos MySQL 8.0+. Centraliza la gestión académica, administrativa, financiera y de recursos del Colegio Internacional "Little Horizons".

---

## 2. Descripción

### Propósito del sistema

Little Horizons atiende a más de 500 estudiantes desde Preescolar hasta grado 11°, con 40 profesores y una oferta académica que incluye múltiples materias y actividades extracurriculares. Este ERP reemplaza los registros en papel y las hojas de cálculo dispersas por un sistema único, consistente y auditable.

### Problemas que resuelve

- **Pérdida de información académica:** notas centralizadas con validación de rango (0.0 – 5.0) e historial completo por estudiante.
- **Desorganización extracurricular:** catálogo de clubes con control de cupo, membresías y registro de participación en eventos y competencias.
- **Caos en la facturación:** registro de pagos, tarifas personalizadas (descuentos por hermanos, becas) y generación automática de factura electrónica simulada en formato TXT con CUFE y QR simulados.
- **Desperdicio de recursos:** inventario de biblioteca y laboratorios con préstamos separados para estudiantes y profesores, devoluciones y alertas de vencimiento.
- **Comunicación ineficiente:** reportes de asistencia, bajo rendimiento, morosidad y estadísticas consolidadas por módulo.

### Funcionalidades principales

1. **Gestión de Estudiantes:** registro completo, acudientes (relación muchos a muchos, hermanos), matrícula anual por grado e inscripción de materias por período.
2. **Gestión Académica:** catálogo de materias, registro de calificaciones, cálculo automático de promedios (materia, período y curso), historial académico y control de asistencia con reportes.
3. **Gestión de Profesores:** directorio, asignación de materias con horario y detección de conflictos de franja horaria.
4. **Actividades Extracurriculares:** clubes con cupo máximo, membresías, eventos por tipo, participantes y posiciones en competencia.
5. **Gestión Financiera:** conceptos de pago, tarifas personalizadas, pagos con recibo, historial por estudiante/acudiente, reporte de morosos y factura electrónica TXT.
6. **Gestión de Recursos:** inventario por categoría y estado, préstamos a estudiantes y profesores, devoluciones y reporte de préstamos vencidos.
7. **Reportes y Estadísticas:** estudiantes por estado y grado, ingresos por concepto, promedios por materia, participación en clubes, recursos por estado y exportación consolidada a TXT.

---

## 3. Autores

| Nombre completo | Correo electrónico | Contacto |
|---|---|---|
| Santiago Castro Peñaranda | c3strosantiago@gmail.com | [Repositorio en GitHub](https://github.com/Lel-del/proyecto-Little-Horizons) |

---

## 4. Tecnologías Utilizadas

| Elemento | Tecnología / Versión |
|---|---|
| Lenguaje | Java 21 (compatible con Java 17+) |
| IDE recomendado | IntelliJ IDEA Community / Ultimate |
| Base de datos | MySQL 8.4 (requiere MySQL 8.0 o superior) |
| Driver JDBC | MySQL Connector/J 8.4.0 (`lib/mysql-connector-j-8.4.0.jar`) |
| Librerías externas | Solo el driver JDBC de MySQL |
| Interfaz | Consola (menús, validaciones y mensajes formateados) |
| Arquitectura | MVC + DAO + Service |
| Control de versiones | Git / GitHub |

**No se utiliza Maven ni Gradle:** el proyecto es una aplicación Java estándar compilada con `javac`, tal como lo permite la guía del proyecto.

---

## 5. Requerimientos Previos

- **JDK 17 o superior** (probado con JDK 21). Verifique con:
  ```bash
  java -version
  javac -version
  ```
- **MySQL Server 8.0 o superior** (obligatorio por el uso de `CHECK` constraints).
  ```bash
  mysql --version
  ```
- **MySQL Connector/J 8.4.0** incluido en `lib/`.
- Sistema operativo: Windows, Linux o macOS.
- Terminal o consola con soporte UTF-8 para visualizar correctamente tildes y símbolos.

> **Importante:** MySQL 8.0+ es un requisito estricto. Versiones anteriores no aplican correctamente las restricciones `CHECK` del esquema.

---

## 6. Configuración de la Base de Datos

### 6.1. Creación de la base de datos

El script completo (DDL + DML) está en [`docs/database.sql`](docs/database.sql). Incluye la creación de la base `erp_little_horizons`, todas las tablas, restricciones y datos de prueba coherentes (mínimo 30 registros por tabla principal).

```bash
mysql -u root -p < docs/database.sql
```

O desde el cliente MySQL:

```sql
SOURCE C:/ruta/al/proyecto/docs/database.sql;
```

### 6.2. Configuración de credenciales

Edite [`config/db.properties`](config/db.properties):

```properties
db.url=jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
db.user=root
db.password=SU_CONTRASENA
```

También puede usar variables de entorno (tienen prioridad sobre el archivo):

| Variable | Descripción |
|---|---|
| `LH_DB_URL` | URL JDBC completa |
| `LH_DB_USER` | Usuario de MySQL |
| `LH_DB_PASSWORD` | Contraseña de MySQL |

Si `config/db.properties` no existe, la aplicación usa `config/db.properties.example` y luego valores por defecto (`root` sin contraseña).

> `config/db.properties` está excluido en `.gitignore` para no versionar credenciales reales.

### 6.3. Instalación del driver JDBC

El archivo `lib/mysql-connector-j-8.4.0.jar` ya está incluido en el repositorio. Si necesita reinstalarlo:

```bash
# Descarga directa desde Maven Central
curl -L -o lib/mysql-connector-j-8.4.0.jar \
  https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar
```

En IntelliJ IDEA: **File → Project Structure → Libraries → + → Java** y seleccione el JAR. Alternativamente, marque `lib/` como biblioteca del módulo.

---

## 7. Instalación y Ejecución

### 7.1. Clonar el repositorio

```bash
git clone https://github.com/Lel-del/proyecto-Little-Horizons.git
cd proyecto-Little-Horizons
```

### 7.2. Abrir el proyecto en el IDE

1. Abra IntelliJ IDEA.
2. **File → Open** y seleccione la carpeta del proyecto.
3. Marque `src` como *Sources Root* si el IDE no lo detecta.
4. Agregue `lib/mysql-connector-j-8.4.0.jar` como dependencia.
5. Configure el SDK del proyecto en **File → Project Structure → Project → SDK** (JDK 17+).

### 7.3. Compilar

**Desde IntelliJ:** `Build → Build Project` (Ctrl+F9).

**Desde terminal (Windows PowerShell):**

```powershell
.\build.ps1
```

**Manualmente con `javac`:**

```powershell
New-Item -ItemType Directory -Force -Path bin | Out-Null
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d bin -cp "lib\mysql-connector-j-8.4.0.jar" $files
```

**En Linux/macOS:**

```bash
mkdir -p bin
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin -cp "lib/mysql-connector-j-8.4.0.jar" @sources.txt
```

### 7.4. Ejecutar

**Desde IntelliJ:** ejecute la clase `com.littlehorizons.erp.Main`.

**Desde terminal:**

```powershell
.\run.ps1
```

o manualmente:

```powershell
java -Dfile.encoding=UTF-8 -cp "bin;lib\mysql-connector-j-8.4.0.jar" com.littlehorizons.erp.Main
```

En Linux/macOS el separador de classpath es `:` en lugar de `;`.

Al iniciar, la aplicación verifica la conexión a MySQL y muestra el menú principal. Si la conexión falla, revise `config/db.properties`, que el servicio MySQL esté activo y que la base `erp_little_horizons` exista.

---

## 8. Estructura del Proyecto

```
proyecto-littleHorizons/
├── src/
│   └── com/littlehorizons/erp/
│       ├── Main.java                 # Punto de entrada
│       ├── config/                   # Conexión JDBC (Singleton)
│       │   └── DatabaseConnection.java
│       ├── model/                    # Entidades del dominio (18 clases)
│       ├── dao/                      # Acceso a datos con PreparedStatement
│       ├── controller/               # Lógica de negocio y validaciones
│       ├── service/                  # Facturación TXT y exportación de reportes
│       ├── view/                     # Interfaz de consola por módulo
│       └── util/                     # Logger, validación de entradas, formato
├── docs/
│   └── database.sql                  # Sección DDL + sección DML
├── config/
│   ├── db.properties                 # Credenciales locales (no versionado)
│   └── db.properties.example         # Plantilla de configuración
├── lib/
│   └── mysql-connector-j-8.4.0.jar
├── build.ps1                         # Script de compilación (Windows)
├── run.ps1                           # Script de ejecución (Windows)
└── README.md
```

### Arquitectura MVC

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **Modelo** | `model` | Entidades del dominio (`Student`, `Teacher`, `Payment`, `Resource`, etc.) con sus atributos y reglas de representación. |
| **Vista** | `view` | Menús de consola, captura de datos, mensajes de éxito/error y navegación. No contiene SQL. |
| **Controlador** | `controller` | Validaciones de negocio, reglas (cupos, conflictos de horario, mora, duplicados) y coordinación entre vistas y DAO. |
| **DAO** | `dao` | Persistencia con `PreparedStatement`, mapeo `ResultSet → Modelo` y `try-with-resources`. |
| **Servicio** | `service` | Generación de facturas TXT (CUFE/QR simulados) y exportación de reportes. |
| **Utilidades** | `util` | `AppLogger` (Singleton), `InputValidator`, `ConsolePrinter`, `CufeGenerator`. |
| **Configuración** | `config` | `DatabaseConnection` (Singleton JDBC). |

---

## 9. Principios y Patrones de Diseño Aplicados

### 9.1. Principios SOLID

- **S — Responsabilidad Única:** cada clase tiene una única razón para cambiar. `StudentDAO` solo persiste estudiantes; `InvoiceService` solo genera facturas; `InputValidator` solo valida entradas.
- **O — Abierto/Cerrado:** nuevas vistas o DAOs se agregan sin modificar los existentes. `ReportController` compone reportes reutilizando `ReportDAO` y `AcademicRecordDAO`.
- **L — Sustitución de Liskov:** los modelos exponen contratos consistentes y los DAO devuelven `Optional` cuando un registro puede no existir, evitando retornos `null` ambiguos.
- **I — Segregación de Interfaces:** no se imponen interfaces gigantes; cada DAO expone solo las operaciones que su módulo necesita (por ejemplo, `GradeDAO` solo consulta grados).
- **D — Inversión de Dependencias:** los controladores dependen de abstracciones de acceso a datos bien delimitadas (DAO) y no construyen conexiones SQL directamente; la conexión se obtiene mediante `DatabaseConnection`.

### 9.2. Patrones de Diseño

| Patrón | Implementación | Justificación |
|---|---|---|
| **Singleton** | `DatabaseConnection`, `AppLogger` | Evita múltiples configuraciones de conexión y garantiza un único punto de escritura de logs. |
| **DAO (Data Access Object)** | Paquete `dao` | Aísla el SQL del dominio y de la vista, facilitando mantenimiento y pruebas. |
| **MVC** | `model`, `view`, `controller` | Separa presentación, lógica y datos según exige la guía. |
| **Service Layer** | `InvoiceService`, `ReportExportService` | Encapsula procesos de negocio complejos (facturación y exportación) reutilizables por varios controladores. |
| **Builder parcial / JavaBeans** | Modelos con setters | Construcción legible de entidades desde las vistas y mapeo uniforme desde JDBC. |
| **Template de reportes** | `ReportExportService` | Formato homogéneo de todos los TXT generados. |

### 9.3. Uso de Streams y Lambdas

- Filtrado y agrupación en `AcademicController.calculateStudentAverages`.
- Búsqueda de préstamo activo por ID en `ResourceView.registerReturn`.
- Recorrido de mapas de estadísticas con `forEach` en `ReportController`.
- Validación de categoría existente con `stream().anyMatch(...)` en `ResourceController`.

---

## 10. Funcionalidades Principales

### 10.1. Módulo de Estudiantes
- Registro con documento, nombres, fecha de nacimiento, género, dirección, contactos y datos médicos.
- Acudientes con relación muchos a muchos: un acudiente puede tener varios estudiantes (hermanos) y un estudiante varios acudientes.
- Validación de que todo estudiante conserve al menos un acudiente.
- Matrícula anual por grado con estado (`Activa`, `Completada`, `Retirada`) y control de duplicados por año.
- Inscripción de materias por año y período.

### 10.2. Módulo Académico
- Catálogo de materias por grado con horas semanales y estado.
- Registro de calificaciones (0.0 – 5.0, un decimal) validando inscripción y asignación del profesor.
- Cálculo automático de promedio por materia, promedio general del período y promedio del curso.
- Historial académico completo desde el ingreso.
- Asistencia diaria (`Presente`, `Ausente`, `Tardanza`, `Excusa Médica`).
- Reporte de inasistencias reiteradas y de bajo rendimiento.

### 10.3. Módulo de Profesores
- Directorio con documento, especialización y contacto.
- Asignación de materias con año, sección, día y franja horaria.
- Detección automática de conflictos de horario para el mismo profesor.

### 10.4. Módulo Extracurricular
- Clubes con cupo máximo, profesor encargado y horario.
- Inscripción con validación de cupo y membresía activa única.
- Retiro con fecha y motivo.
- Eventos por tipo (`Académico`, `Cultural`, `Deportivo`, `Social`, `Científico`).
- Participantes con rol y posición (`1er Lugar`, `2do Lugar`, `3er Lugar`, `Participante`).

### 10.5. Módulo Financiero
- Conceptos de pago (matrícula, pensión, uniforme, transporte, etc.).
- Tarifas personalizadas por estudiante con porcentaje de descuento y motivo.
- Registro de pagos con método, recibo, año y período.
- Historial por estudiante y por acudiente; total pagado.
- Reporte de morosos (pensión sin pago por más de un mes).
- **Factura electrónica TXT:** razón social, NIT, resolución simulada, datos del cliente, detalle, subtotal, IVA 19%, total, CUFE simulado (SHA-256) y bloque QR simulado. Se guarda en `invoices/factura_NNNNNN.txt`.

### 10.6. Módulo de Recursos
- Inventario por código único, categoría, ubicación y estado.
- Préstamos separados a estudiantes (`prestamos_estudiante`) y profesores (`prestamos_profesor`).
- Validación de disponibilidad y devolución que libera el recurso.
- Reporte de préstamos vencidos con actualización de estado.

### 10.7. Reportes y Estadísticas
- Estudiantes por estado y por grado.
- Ingresos por concepto y total recaudado.
- Promedios por materia y período.
- Participación por club.
- Recursos por estado.
- Exportación consolidada a `reports/reporte_consolidado_YYYYMMDD_HHMMSS.txt`.

---

## 11. Manejo de Errores y Logs

### Sistema de logging

`AppLogger` es un Singleton que escribe en **`error_log.txt`** en la raíz del proyecto. Cada entrada incluye fecha y hora, nivel (`INFO`, `WARN`, `ERROR`), contexto y mensaje. Para excepciones, se adjunta el `stack trace` completo.

Ejemplo:

```
[2026-11-12 19:42:10] [ERROR] [DatabaseConnection.testConnection] Failed to connect to database URL: jdbc:mysql://localhost:3306/erp_little_horizons
...
```

### Estrategia de manejo de excepciones

1. **Vistas:** capturan `IllegalArgumentException` e `IllegalStateException` para mostrar mensajes claros al usuario sin cerrar la aplicación, y registran fallos inesperados con `AppLogger.logError`.
2. **DAO:** envuelven `SQLException` en `DatabaseException` (unchecked) para no filtrar detalles técnicos a la vista.
3. **`Main`:** captura cualquier error crítico, informa al usuario y garantiza el cierre ordenado registrando el evento.
4. **Recursos:** todas las conexiones, `PreparedStatement` y `ResultSet` se gestionan con `try-with-resources`.
5. **Consultas:** el 100 % de las sentencias usan `PreparedStatement` para prevenir inyección SQL.

---

## 12. Capturas de Pantalla

> Espacio reservado para evidencias visuales del sistema en ejecución. Puede agregar imágenes en `docs/screenshots/` y referenciarlas aquí.

| Menú principal | Factura electrónica generada |
|---|---|
| *(pendiente)* | *(pendiente)* |

---

## 13. Problemas Conocidos y Limitaciones

- La factura electrónica es una **simulación académica**: el CUFE y el QR no tienen validez fiscal ante la DIAN.
- El IVA se aplica con una tasa fija del 19 % sobre el valor del pago; el esquema no distingue conceptos exentos.
- La interfaz es de consola, por lo que la experiencia depende de una terminal con soporte UTF-8.
- No se implementa autenticación ni control de roles: cualquier usuario con acceso al ejecutable puede operar el sistema.
- El consecutivo de factura se calcula con `COUNT(*) + 1`; en un escenario concurrente real requeriría una secuencia transaccional.
- La detección de morosos se basa en pagos registrados del concepto seleccionado y no en un estado de cartera formal.

---

## 14. Futuras Mejoras

- Migrar la interfaz a JavaFX o aplicación web (Spring Boot + React).
- Implementar autenticación con roles (administrador, profesor, acudiente, estudiante).
- Generar PDF real con código QR verificable y firma digital.
- Añadir tests automatizados con JUnit 5 y pruebas de integración con Testcontainers.
- Gestión completa de horarios con validación de aulas y disponibilidad de estudiantes.
- Notificaciones por correo a acudientes (pagos, inasistencias, boletines).
- Panel de indicadores (BI) con gráficos de rendimiento y cartera.
- API REST para integración con otros sistemas del colegio.

---

## 15. Licencia

Proyecto académico desarrollado para el Colegio Internacional Little Horizons. Su uso está restringido a fines educativos y de evaluación. Todos los datos personales incluidos en `docs/database.sql` son ficticios.

---

## 16. Contacto

- **Autor:** Santiago Castro Peñaranda
- **Correo:** c3strosantiago@gmail.com
- **Repositorio:** [https://github.com/Lel-del/proyecto-Little-Horizons](https://github.com/Lel-del/proyecto-Little-Horizons)

Para reportar errores o sugerencias, abra un *issue* en el repositorio indicando el módulo afectado, los pasos para reproducir el problema y el contenido relevante de `error_log.txt`.
