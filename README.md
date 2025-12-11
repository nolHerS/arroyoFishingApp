# 🎣 CharcaFishing App

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> API REST para gestionar capturas de pesca, permitiendo a los usuarios registrar, visualizar y compartir sus mejores capturas con la comunidad.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Uso](#-uso)
- [API Endpoints](#-api-endpoints)
- [Testing](#-testing)
- [Despliegue](#-despliegue)
- [Arquitectura](#-arquitectura)
- [Contribuir](#-contribuir)
- [Licencia](#-licencia)

## ✨ Características

- 🔐 **Autenticación y Autorización**: Sistema completo con JWT (Access + Refresh tokens)
- 👤 **Gestión de Usuarios**: Registro, Login, Perfil y Gestión de sesiones
- 🐟 **Capturas de Pesca**: CRUD completo para registrar capturas (tipo de pez, peso, ubicación, fecha)
- 📸 **Gestión de Imágenes**: 
  - Subida de imágenes a Cloudinary
  - Validación y optimización automática
  - Generación de thumbnails
  - Límite de 5 imágenes por captura
- 🔍 **Búsqueda y Filtrado**: Búsqueda de capturas por usuario
- 📊 **API RESTful**: Documentación con OpenAPI/Swagger
- 🛡️ **Seguridad**: 
  - Encriptación de contraseñas con BCrypt
  - Protección CORS configurada
  - Tokens de verificación de email
- ☁️ **Almacenamiento en la Nube**: Integración con Cloudinary para gestión de imágenes

## 🚀 Tecnologías

### Backend
- **Java 17**
- **Spring Boot 3.3.4**
  - Spring Security
  - Spring Data JPA - Hibernate
  - Spring Web
- **PostgreSQL** - Base de datos principal
- **H2** - Base de datos en memoria para testing

### Seguridad y Autenticación
- **JWT (JSON Web Tokens)** - Autenticación stateless
- **BCrypt** - Encriptación de contraseñas

### Almacenamiento
- **Cloudinary** - CDN para imágenes
- **PostgreSQL/Neon** - Base de datos en producción

### Testing
- **JUnit 5**
- **Mockito**
- **Hamcrest**
- **MockMvc**

### Documentación
- **SpringDoc OpenAPI** - Documentación automática de API
- **Swagger UI** - Interfaz interactiva para probar la API

### Build y CI/CD
- **Maven** - Gestión de dependencias
- **Docker** - Contenedorización
- **GitHub Actions** - CI/CD pipeline
- **JaCoCo** - Cobertura de código
- **SonarCloud** - Análisis de calidad de código

## 📦 Requisitos Previos

- **Java 17** o superior
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Docker**
- **Cuenta en Cloudinary**
- **Cuenta en Neon**

## 🔧 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/charcaFishing-app.git
cd charcaFishing-app
```

### 2. Configurar PostgreSQL

**Opción A: PostgreSQL Local**

```sql
CREATE DATABASE arroyofishing;
CREATE USER tu_usuario WITH PASSWORD 'tu_password';
GRANT ALL PRIVILEGES ON DATABASE arroyofishing TO tu_usuario;
```

**Opción B: Neon (Recomendado para desarrollo)**

1. Crea una cuenta en [Neon](https://neon.tech)
2. Crea un nuevo proyecto
3. Copia la connection string

### 3. Configurar variables de entorno

Crea un archivo `src/main/resources/application-local.properties`:

```properties
# Base de datos LOCAL - PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/arroyofishing
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password

# JWT LOCAL
jwt.secret=tu-clave-jwt-segura-de-al-menos-256-bits

# Cloudinary
cloudinary.cloud-name=tu_cloud_name
cloudinary.api-key=tu_api_key
cloudinary.api-secret=tu_api_secret

# Configuración de imágenes
app.image.max-file-size=10485760
app.image.max-images-per-capture=5
app.image.allowed-types=image/jpeg,image/jpg,image/png

# Frontend LOCAL
app.frontend.url=http://localhost:3000

# CORS LOCAL
app.cors.allowed-origins=http://localhost:3000,http://localhost:4200
```

### 4. Instalar dependencias

```bash
./mvnw clean install
```

### 5. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## ⚙️ Configuración

### Perfiles de Spring

- **local**: Desarrollo local
- **prod**: Producción (usa variables de entorno)
- **test**: Testing (usa H2 en memoria)

### Variables de Entorno (Producción)

```bash
# Base de datos
DATABASE_URL=jdbc:postgresql://host:5432/database?user=user&password=pass&sslmode=require

# JWT
JWT_SECRET=tu-clave-jwt-segura

# Cloudinary
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret

# Frontend
FRONTEND_URL=https://tu-frontend.com
CORS_ALLOWED_ORIGINS=https://tu-frontend.com
```

## 📖 Uso

### Acceder a Swagger UI

Una vez la aplicación esté corriendo, accede a:

```
http://localhost:8080/swagger-ui.html
```

### Ejemplo de uso con cURL

**Registrar un usuario:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "pescador123",
    "fullName": "Juan Pescador",
    "email": "juan@ejemplo.com",
    "password": "Password123"
  }'
```

**Login:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "juan@ejemplo.com",
    "password": "Password123"
  }'
```

**Crear una captura:**

```bash
curl -X POST http://localhost:8080/api/fish-captures \
  -H "Authorization: Bearer TU_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fishType": "Trucha",
    "weight": 2.5,
    "captureDate": "2025-12-11",
    "location": "Río Tajo"
  }'
```

## 🔌 API Endpoints

### Autenticación (`/api/auth`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/register` | Registrar nuevo usuario | No |
| POST | `/login` | Iniciar sesión | No |
| POST | `/refresh-token` | Renovar access token | No |
| POST | `/logout` | Cerrar sesión | No |
| POST | `/logout-all` | Cerrar todas las sesiones | No |

### Usuarios (`/api/users`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/` | Listar todos los usuarios | No |
| GET | `/username/{username}` | Obtener usuario por username | No |
| GET | `/id/{id}` | Obtener usuario por ID | Sí |
| PUT | `/` | Actualizar usuario | Sí |
| DELETE | `/{username}` | Eliminar usuario | Admin |

### Capturas (`/api/fish-captures`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/` | Listar todas las capturas | No |
| GET | `/{id}` | Obtener captura por ID | Sí |
| GET | `/user/{username}` | Capturas de un usuario | No |
| POST | `/` | Crear nueva captura | Sí |
| PUT | `/{id}` | Actualizar captura | Sí |
| DELETE | `/{id}` | Eliminar captura | Sí |

### Imágenes (`/api/captures`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/{captureId}/images` | Subir imagen | Sí |
| POST | `/{captureId}/images/multiple` | Subir múltiples imágenes | Sí |
| GET | `/{captureId}/images` | Obtener imágenes de captura | No |
| GET | `/images/{imageId}` | Obtener imagen por ID | No |
| GET | `/{captureId}/images/count` | Contar imágenes | No |
| DELETE | `/images/{imageId}` | Eliminar imagen | Sí |
| DELETE | `/{captureId}/images` | Eliminar todas las imágenes | Sí |

## 🧪 Testing

### Ejecutar todos los tests

```bash
./mvnw test
```

### Ejecutar tests con cobertura

```bash
./mvnw clean verify
```

### Ver reporte de cobertura

El reporte JaCoCo se genera en:
```
target/site/jacoco/index.html
```

### Ver cobertura en sonar

La cobertura en Sonar se puede observar en:
```
https://sonarcloud.io/
```

### Tipos de tests

- **Tests Unitarios**: Servicios y controladores
- **Tests de Integración**: Repositorios y flujos completos
- **Tests de API**: Endpoints REST con MockMvc

### Cobertura de código

- **Objetivo mínimo**: 85%
- **Herramienta**: JaCoCo
- **CI/CD**: El pipeline falla si la cobertura es < 85%

## 🚢 Despliegue

### Docker

**Construir imagen:**

```bash
docker build -t charcafishing-app .
```

**Ejecutar contenedor:**

```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://... \
  -e JWT_SECRET=... \
  -e CLOUDINARY_CLOUD_NAME=... \
  -e CLOUDINARY_API_KEY=... \
  -e CLOUDINARY_API_SECRET=... \
  charcafishing-app
```

### Render (Recomendado)

1. Conecta tu repositorio de GitHub
2. Configura las variables de entorno en el dashboard
3. Selecciona el perfil `prod`
4. Despliega automáticamente con cada push a `main`

### Variables de entorno requeridas en producción

```bash
DATABASE_URL
JWT_SECRET
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
FRONTEND_URL
CORS_ALLOWED_ORIGINS
```

## 🏗️ Arquitectura

```
src/
├── main/
│   ├── java/com/example/fishingapp/
│   │   ├── config/           # Configuración (Security, Swagger, Cloudinary)
│   │   ├── controller/       # Controladores REST
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── exception/        # Manejo de excepciones
│   │   ├── mapper/           # Mappers entidad <-> DTO
│   │   ├── model/            # Entidades JPA
│   │   ├── repository/       # Repositorios JPA
│   │   ├── security/         # Seguridad (JWT, filtros)
│   │   └── service/          # Lógica de negocio
│   └── resources/
│       ├── application.properties          # Común
│       ├── application-local.properties    # Local
│       └── application-prod.properties     # Producción
└── test/                     # Tests unitarios e integración
```

### Patrones de diseño

- **DTO Pattern**: Separación entre modelo de dominio y API
- **Repository Pattern**: Abstracción de acceso a datos
- **Service Layer**: Lógica de negocio centralizada
- **JWT Stateless Authentication**: Autenticación sin sesiones

## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Guía de estilo

- Sigue las convenciones de código de Java
- Escribe tests para nuevas funcionalidades
- Mantén la cobertura de código > 85%
- Documenta los endpoints en Swagger

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

## 👨‍💻 Autor

**Imanol Hernández**
- GitHub: [@nolHerS](https://github.com/nolHerS)

## 🙏 Agradecimientos

- Spring Boot team por el excelente framework
- Cloudinary por el servicio de almacenamiento de imágenes
- Neon por la base de datos PostgreSQL serverless
- Render por el hosting gratuito

---

⭐️ Si este proyecto te ha sido útil, considera darle una estrella en GitHub