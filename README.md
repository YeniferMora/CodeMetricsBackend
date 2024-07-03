# CodeMetricsBackend

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

Ya puedes ejecutar el backend, recuerda que la clase principal se encuentra en:
src/main/java/com/codemetrics/codemetrics_generator/CodemetricsGeneratorApplication.java

## Resumen de los Endpoints.
POST: Calcular métricas con ANTLR. Recibe código fuente Python en formato texto. https://codemetricsbackend.onrender.com/api/v1/codemetrics/analyze

POST: Generar observaciones de malos olores en el código y recomendaciones. Recibe objeto JSON con las métricas resultantes de analyze en formato texto. https://codemetricsbackend.onrender.com/api/v1/codemetrics/smellCodeAnalysis

POST: Generar descripción de qué hace el código. Recibe código fuente Python en formato texto. https://codemetricsbackend.onrender.com/api/v1/codemetrics/codeDescription