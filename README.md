# EFrame - Marco Digital Inteligente y Modular para Android

**EFrame** es una aplicación diseñada para convertir cualquier dispositivo Android (desde **Android 6.0 Marshmallow - API 23** hasta las versiones más recientes como **Android 14, 15 y 16**) en un marco de fotos digital moderno, eficiente y expandible.

---

## 🌟 Características Principales

1. **Galería de Fotos desde Google Drive**:
   - Conexión directa a través de un enlace público a una carpeta de Google Drive (`https://drive.google.com/drive/folders/...`).
   - Resolución automática de imágenes mediante la red de entrega de contenido (CDN de alta resolución de Google) sin necesidad de configurar OAuth complejo.
   - Soporte opcional para Google Drive API v3 (para cuotas avanzadas o carpetas privadas).
   - Galería de respaldo precargada con fotografías de alta definición para funcionamiento inmediato sin configuración y soporte de caché offline con Coil.

2. **Rendimiento a 60 FPS y Transiciones Suaves (Crossfade)**:
   - Desvanecimiento continuo entre fotografías con doble capa de renderizado acelerado por hardware (`graphicsLayer`).
   - Decodificación optimizada en `RGB_565` (16 bits) para tablets antiguas, reduciendo a la mitad el consumo de memoria GPU y evitando cualquier caída de frames.
   - Precarga automática de la siguiente foto en segundo plano.
   - Intervalo de diapositivas totalmente configurable (5s, 8s, 10s, 15s, 30s, 60s).

3. **Sensor de Proximidad y Control de Reposo (Sleep & Wake)**:
   - **Apagado suave por inactividad**: La pantalla se oscurece y reduce su brillo físico a nivel mínimo (`0.005f`) tras un tiempo sin actividad (10 segundos predeterminado, personalizable o desactivado). Esto evita quemaduras en pantallas OLED/IPS y reduce el consumo energético al mínimo.
   - **Despertar con sensor de proximidad**: Al pasar la mano por encima del sensor frontal del teléfono/tablet, la pantalla recupera gradualmente su brillo y color de manera suave, reiniciando el contador de inactividad.
   - Despertar táctil disponible tocando cualquier parte de la pantalla.
   - El apagado se suspende automáticamente mientras se interactúa con los menús y paneles de información.

4. **Clima en Tiempo Real y Pronóstico a 24 Horas**:
   - **Actualización en vivo**: Sincronización continua en segundo plano cada 2 minutos y actualización inmediata tanto al abrir la app como al cliquear en el módulo.
   - **Próximas 24 Horas estrictas**: Al tocar la tarjeta del clima, se despliega una vista detallada que inicia en el momento actual ("Ahora") y muestra las 24 horas siguientes (temperatura, sensación térmica y probabilidad de lluvia 💧).
   - Botón interactivo de **"Actualizar"** con hora exacta de la última sincronización.
   - **Selector jerárquico para Argentina**: Desplegables de País, Provincia y Barrio/Localidad (Palermo, Belgrano, Recoleta, Caballito, Rosario, Córdoba, etc.).

5. **Transporte Público y Subte de Buenos Aires**:
   - **Estación habitual configurada**: Se elige en los Ajustes (Línea y Estación de espera).
   - **Consulta interactiva (Al hacer clic en el módulo)**:
     - Selector de cualquier línea (A, B, C, D, E, H) iniciando por defecto en la tuya.
     - **Listado de trenes hacia CADA dirección**: Próximos trenes hacia Cabecera A y Cabecera B con minutos de arribo y estado de aproximación.
     - **Estaciones Deshabilitadas**: Alerta clara (⚠️) sobre estaciones clausuradas o en obras de remodelación en la red.
     - Estado de servicio en vivo de todas las líneas ("Normal", "Demoras", etc.).

6. **Escala Visual y Personalización**:
   - Selector de tamaño de widgets (`1.0x Normal`, `1.2x Grande`, `1.4x Extra Grande`, `1.6x Tablet Lejana`).
   - Modo de información `Compacto` vs `Detallado`.

---

## 🏗️ Estructura del Proyecto

```
app/src/main/kotlin/com/antigravity/eframe/
├── MainActivity.kt                      # Actividad principal en modo inmersivo fullscreen (Android 6 a 16)
├── data/
│   ├── AppSettings.kt                   # Persistencia reactiva de configuración (StateFlow + SharedPreferences)
│   ├── ArgentinaLocations.kt            # Base de ubicaciones jerárquicas (País, Provincia, Barrio)
│   ├── DriveRepository.kt               # Extracción y carga de fotos desde enlaces públicos de Google Drive
│   ├── WeatherRepository.kt             # Cliente REST para clima, próximas 24hs y pronóstico extendido
│   └── TransportRepository.kt           # Repositorio de transporte, trenes por dirección y estado
├── modules/
│   ├── FrameModule.kt                   # Interfaz base y posicionamiento de módulos
│   ├── ClockModule.kt                   # Módulo de reloj digital y fecha
│   ├── WeatherModule.kt                 # Módulo de clima en tiempo real
│   ├── TransportModule.kt               # Módulo de transporte público / subte
│   └── ModuleManager.kt                 # Gestor central de módulos
├── sensor/
│   └── ProximitySensorManager.kt        # Detección y manejo del sensor de proximidad
└── ui/
    ├── settings/
    │   └── SettingsDialog.kt            # Diálogo modal de ajustes y personalización
    ├── transport/
    │   └── SubteDetailModal.kt          # Modal informativo de subtes y trenes por dirección
    ├── weather/
    │   └── WeatherDetailModal.kt        # Modal de pronóstico en vivo a 24hs
    └── slideshow/
        └── PhotoFrameScreen.kt          # Pantalla principal del marco fotográfico a 60 FPS
```

---

## 🚀 Compilación e Instalación

Para compilar el APK:

```bash
./gradlew assembleDebug
```

El archivo generado:
```
app/build/outputs/apk/debug/app-debug.apk
```
