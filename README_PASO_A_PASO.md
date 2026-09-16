# SkillBridge AI - Colaborador V3

Esta versión incorpora las correcciones visuales y funcionales solicitadas para el módulo **Colaborador**, manteniendo Spring MVC + Thymeleaf + Spring Data JPA, de acuerdo con la metodología trabajada en clase.

## 1. Abrir en IntelliJ
1. Descomprime el ZIP.
2. IntelliJ IDEA -> File -> Open.
3. Abre la carpeta `skillbridge-ai-colaborador-corregido-v3`.
4. Espera que Maven cargue `pom.xml`.
5. Usa JDK 17.

## 2. Configurar MySQL
Edita:
`src/main/resources/application.properties`

y coloca tu usuario/contraseña de MySQL.

## 3. Base de datos
### Si crearás la BD desde cero
Ejecuta en este orden:
1. `src/main/resources/database/skillbridge_ai_18_tablas_corregida.sql`
2. `src/main/resources/database/datos_demo.sql`

### Si YA tienes skillbridge_ai creada con la versión anterior
Ejecuta:
1. `src/main/resources/database/actualizacion_colaborador_v3.sql`
2. Luego, solo si deseas restaurar todos los datos de presentación, ejecuta `datos_demo.sql`.

**Importante:** `datos_demo.sql` elimina los registros demo y los vuelve a insertar. No elimina las tablas. Ya incluye la corrección de `SQL_SAFE_UPDATES` para evitar el error 1175.

El modelo visual actualizado de MySQL Workbench está en:
`src/main/resources/database/tabla_refinada_gtics_v3.mwb`

## 4. Cambios principales del Colaborador
- Inicio más práctico: se retiraron accesos repetidos, actividad de foro y notificaciones del dashboard.
- Se muestra proyecto principal, carga actual y disponibilidad estimada.
- El logo lateral conserva la sesión y regresa al inicio del rol.
- Perfil completamente en español: `Nivel profesional`, `Junior`, `Semi senior`, `Senior`, `Líder`.
- Validaciones de perfil: nombres/apellidos/cargo/área solo letras y hasta 20 caracteres; teléfono exactamente 9 dígitos; disponibilidad 0-100.
- Certificaciones: nombre y entidad máximo 40 caracteres y formulario visualmente uniforme.
- Habilidades: se retiró la tarjeta Catálogo; agregar solo habilidades aún no registradas; nivel en formato legible; experiencia 0-50 años.
- Asignaciones: filtros uniformes y fechas `dd-MM-yyyy`.
- `Knowledge Hub` fue reemplazado por `Foro` / `Foro de conocimiento`.
- Foro con paginación para que muchos temas no alarguen la vista indefinidamente.
- Chat con fechas legibles y controles uniformes.
- Notificaciones con tipos en español, tipo específico `CHAT`, resumen útil y paginación de 5 elementos.

## 5. Credencial demo principal
- Rol: Colaborador
- Correo: `maria.gonzalez@skillbridge.com`
- Contraseña: `demo`

## 6. Ejecutar
Ejecuta `SkillbridgeAiApplication.java` y abre:
`http://localhost:8080`

## Ajuste V4 — Foro y Chat

- Los filtros del Foro vuelven a estar alineados horizontalmente en escritorio.
- El Chat valida la pertenencia al proyecto, persiste el mensaje con `saveAndFlush()` y recarga la conversación en la misma vista.
- Esta versión no requiere cambios adicionales en la base de datos respecto de V3.
