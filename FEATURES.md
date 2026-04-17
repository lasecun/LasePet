# PocketPal – Contexto del Proyecto

## Descripción
Aplicación de mascota virtual desarrollada con **Kotlin Multiplatform Mobile (KMM)**.  
Compatible con **Android** e **iOS** compartiendo la lógica de negocio.  
IDE de desarrollo: **Android Studio**.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| UI Android | Jetpack Compose |
| UI iOS | SwiftUI |
| Lógica compartida | Kotlin Multiplatform (ViewModel, UseCases) |
| Base de datos local | SQLDelight |
| Red / sincronización | Ktor Client |
| Animaciones mascota | Lottie (por plataforma) |

---

## Funcionalidades Principales

### 🧬 Core – La mascota
- Creación y personalización del pet: nombre, especie (perro, gato, dragón, etc.), color y accesorios.
- Sistema de estadísticas vitales: hambre, felicidad, energía, higiene y salud.
- Ciclo de vida: la mascota nace, crece (bebé → joven → adulto) y envejece según el cuidado recibido.
- Animaciones y expresiones que reflejan el estado emocional del pet.

### 🍖 Interacción diaria
- Alimentar al pet con distintos tipos de comida (desbloqueable).
- Jugar con minijuegos sencillos para subir la felicidad.
- Limpiar / bañar al pet para mantener su higiene.
- Hacer dormir al pet respetando su ciclo de sueño.

### 🏠 Entorno
- Habitación personalizable con muebles y decoraciones.
- Tienda in-app con moneda virtual ganada jugando (sin pagos reales obligatorios).

### 🔔 Notificaciones & Engagement
- Notificaciones push cuando la mascota tiene hambre, está triste o enferma.
- Racha diaria de cuidado con recompensas.
- Eventos estacionales (Navidad, Halloween, etc.) con items especiales.

### 📊 Progresión
- Sistema de niveles y logros por cuidar bien al pet.
- Diario de la mascota: historial de momentos importantes (primer baño, primer juego, etc.).

### ☁️ Sincronización
- Sincronización en la nube para no perder la mascota al cambiar de dispositivo.
- Compatibilidad Android & iOS con una sola base de código compartida.

---

## Estructura de Módulos KMM sugerida

```
PocketPal/
├── androidApp/          # Módulo Android (Jetpack Compose)
├── iosApp/              # Módulo iOS (SwiftUI)
└── shared/              # Módulo compartido KMM
    ├── commonMain/      # Lógica común (modelos, UseCases, repositorios)
    ├── androidMain/     # Implementaciones específicas Android
    └── iosMain/         # Implementaciones específicas iOS
```

---

## Entidades principales del dominio

- **Pet**: id, nombre, especie, nivel, stats (hambre, felicidad, energía, higiene, salud), fechaCreación.
- **Stats**: valores numéricos (0-100) que decaen con el tiempo y suben con las interacciones.
- **Inventory**: lista de items disponibles (comida, juguetes, decoración).
- **Achievement**: logros desbloqueados por el usuario.
- **DailyStreak**: racha de días consecutivos de cuidado.