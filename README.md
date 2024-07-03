# CodeMetricsBackend

Codemetrics es una aplicación que analiza automáticamente código fuente escrito en Python para recopilar estadísticas y métricas clave. Utiliza ANTLR para obtener las métricas y se conecta con la API de Gemini para generar análisis y recomendaciones basados en esas métricas y una descripción del código fuente.

## Características

- Recopilación automática de estadísticas de código, incluyendo número y tamaño de clases y funciones, variables globales, comentarios, declaraciones condicionales y bucles
- Identificación de dependencias.
- Análisis de la complejidad de las funciones.
- Generación de análisis y recomendaciones utilizando la API de Gemini.

## Requisitos previos

- Java 17
- Maven 3.8.5

## Instalación

1. Clona el repositorio:
2. Configuración de la API de Gemini
Crea un archivo secrets.properties en src/main/resources y coloca la clave:
gemini.gemini-api-key=tu-clave-api
3. Compila usando maven: mvn clean package -DskipTests

Ya puedes ejecutar el backend, recuerda que la clase principal se encuentra en:
src/main/java/com/codemetrics/codemetrics_generator/CodemetricsGeneratorApplication.java
