# Event Viewer 📱

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-26%2B-green?style=for-the-badge&logo=android)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

**Una herramienta para capturar y entender qué pasa en tu Android cuando todo se congela** 🔍

[¿Qué es?](#-qué-es-event-viewer) • [Características](#-características) • [Instalación](#-instalación) • [Uso](#-uso) • [Tecnología](#-tecnología) • [Próximas ideas](#-próximas-ideas)

</div>

---

## 🎯 ¿Qué es Event Viewer?

**Event Viewer** es una aplicación Android experimental diseñada para registrar y analizar eventos del sistema en tiempo real. Nació de una pregunta simple: **¿por qué se congela el teléfono?**

Cuando tu Android "se pone lento" o se congela, algo está pasando bajo el capó. Esta app captura ese "algo" — eventos del sistema, crasheos, comportamientos extraños — para que puedas entender realmente qué está sucediendo.

> **Nota importante**: Esta aplicación fue **desarrollada y optimizada específicamente para Moto G60s (Lisbon)**, pero puede ser adaptada y utilizada como base conceptual para otros dispositivos y enfoques de monitoreo.

---

## ✨ Características Actuales

- 📊 **Captura de eventos** - Registra eventos del sistema en tiempo real
- 🔍 **Análisis local** - Visualiza el historial completo de eventos capturados
- 💾 **Persistencia** - Los datos se almacenan localmente con Room Database
- 🔄 **Background tracking** - WorkManager procesa eventos incluso cuando la app está cerrada
- 📱 **Diseño minimalista** - Interfaz simple y directa (sin distracciones)
- ⚡ **Bajo overhead** - Optimizado para no impactar el rendimiento del dispositivo

---

## 🚀 ¿Por qué nació este proyecto?

Cuando un Moto G60s se congela, no hay forma sencilla de saber **por qué**. ¿Es un app específica? ¿Falta de memoria? ¿Un servicio del sistema? 

Event Viewer intenta responder esas preguntas capturando un "registro de vuelo" (black box) de lo que estaba pasando en el momento del congelamiento.

---

## 💡 Casos de Uso

```
✅ Identificar qué causa congelamientos
✅ Detectar picos de consumo de recursos
✅ Entender comportamientos anómalos del sistema
✅ Generar reports para debugging
✅ Base para herramientas más complejas de monitoreo
```

---

## 🛠️ Tecnología

| Componente | Versión | Razón |
|-----------|---------|-------|
| **Kotlin** | 1.9.22 | Lenguaje moderno y seguro para Android |
| **Room** | 2.6.1 | Persistencia de datos robusta y eficiente |
| **WorkManager** | 2.9.0 | Ejecutar tareas en background sin drenar batería |
| **LiveData** | 2.7.0 | Reactividad y observadores reactivos |
| **ViewModel** | 2.7.0 | Gestión inteligente del ciclo de vida |
| **Android SDK** | 26+ (Moto G60s ready) | Compatible con dispositivos mid-range |
| **Material Design 3** | 1.11.0 | UI moderna y accesible |

---

## 📦 Instalación

### Requisitos

```
✓ Android Studio (Flamingo o superior)
✓ JDK 17+
✓ Gradle 8.3.0
✓ Un Moto G60s o dispositivo similar (Android 26+)
```

### Pasos

1. **Clona el repositorio**
```bash
git clone https://github.com/joak-pixel/event-viewer.git
cd event-viewer
```

2. **Abre en Android Studio**
```
File → Open → Selecciona la carpeta del proyecto
```

3. **Sincroniza Gradle**
```
Android Studio lo hará automáticamente
(si no: File → Sync Now)
```

4. **Conecta tu dispositivo (o usa emulador)**
```bash
adb devices  # Verifica que esté conectado
```

5. **Ejecuta la app**
```bash
./gradlew installDebug
# O desde Android Studio: Run (Shift + F10)
```

---

## 🎮 Uso

### Primeros pasos

```
1. Abre Event Viewer en tu Moto G60s
2. Realiza acciones normales (abre apps, navega, etc)
3. Cuando algo se congele → abre Event Viewer
4. Revisa los eventos que ocurrieron antes del congelamiento
5. Busca patrones o anomalías
```

### Pantalla Principal

```
┌─────────────────────────────┐
│ Event Viewer                │
├─────────────────────────────┤
│ 📊 Eventos registrados: XXX │
│                             │
│ [Lista de eventos]          │
│ • 14:32:15 - Event A        │
│ • 14:32:14 - Event B        │
│ • 14:32:13 - Event C        │
│                             │
│ 🔄 Monitoreo: ACTIVO        │
└─────────────────────────────┘
```

---

## 🔧 Desarrollo

### Compilar

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Tests
./gradlew test
```

### Estructura del Proyecto

```
event-viewer/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── com/lopz/eventviewer/
│   │   │   │       ├── MainActivity.kt
│   │   │   │       ├── data/         (Room entities, DAOs)
│   │   │   │       ├── ui/           (Activities, Fragments)
│   │   │   │       ├── viewmodel/    (ViewModels)
│   │   │   │       └── workers/      (Background tasks)
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   └── drawable/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/        (Unit tests)
│   │   └── androidTest/ (Instrumented tests)
│   └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🎯 Especificaciones del Dispositivo Objetivo

**Event Viewer fue optimizado para:**

```
📱 Dispositivo: Moto G60s (Lisbon)
RAM: 4GB
Procesador: Snapdragon 778 5G+
Android: 12/13
Pantalla: 6.78" IPS LCD
Enfoque: Dispositivos mid-range con recursos limitados
```

Aunque fue diseñado para este modelo específico, funciona en cualquier Android 26+ con arquitectura ARM64. Eventualmente puede adaptarse como base para herramientas de monitoreo más especializadas.

---

## 💭 Próximas Ideas

Este proyecto puede evolucionar hacia:

```
🔸 Exportar logs en formato JSON/CSV
🔸 Gráficos de consumo de recursos en tiempo real
🔸 Alertas configurables (RAM > 80%, CPU > 90%, etc)
🔸 Integración con firebase para análisis remoto
🔸 Análisis automático y predicción de crasheos
🔸 Perfilador de apps individuales
🔸 Dashboard web para revisar datos capturados
🔸 API para que otras apps puedan hacer queries
🔸 Compatibilidad con otros dispositivos/marcas
```

---

## 🤝 Contribuir

¿Quieres mejorar Event Viewer?

1. **Fork** el repositorio
2. **Crea una rama** (`git checkout -b feature/tu-idea`)
3. **Commit** tus cambios (`git commit -m 'Add: descripción clara'`)
4. **Push** (`git push origin feature/tu-idea`)
5. **Abre un Pull Request**

### Antes de contribuir

- Prueba en un Moto G60s o dispositivo similar si es posible
- Mantén el código limpio (Kotlin idiomático)
- Añade tests para nuevas features
- Actualiza esta documentación si es necesario

---

## 📝 Notas Técnicas

```
🔹 Kotlin 1.9.22 - Code style: official
🔹 Java 17 compilation target
🔹 View Binding habilitado (mejor que findViewById)
🔹 Kapt enabled (procesamiento de anotaciones Room)
🔹 ProGuard/R8 rules aplicadas en release builds
🔹 Target SDK 34 (últimas APIs de Android)
🔹 Min SDK 26 (Android 8.0+)
```

---

## 🐛 Debugging

Si encuentras problemas:

```bash
# Ver logs en tiempo real
adb logcat | grep "EventViewer"

# Limpiar datos
adb shell pm clear com.lopz.eventviewer

# Desinstalar
adb uninstall com.lopz.eventviewer
```

---

## 📞 Soporte & Feedback

- 🐛 Abre un [Issue](https://github.com/joak-pixel/event-viewer/issues)
- 💬 Inicia una [Discusión](https://github.com/joak-pixel/event-viewer/discussions)
- 📧 Contacta al autor

---

## 📄 Licencia

MIT License - Libre para usar, modificar y distribuir.

```
Copyright (c) 2026 joak-pixel

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software...
```

Ver [LICENSE](LICENSE) para detalles completos.

---

## 🎬 Créditos

Desarrollado por [joak-pixel](https://github.com/joak-pixel)

Inspirado en la necesidad real de entender por qué los dispositivos mid-range se congelen.

---

<div align="center">

**[⬆ Volver arriba](#event-viewer-)**

Hecho con ❤️ y muchos congelamientos de prueba

`¿Te sirvió? Dale una ⭐ si te gustó el proyecto`

</div>
