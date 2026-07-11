#!/bin/bash
# Insert imports at the top
sed -i '1i import androidx.compose.animation.core.Animatable\nimport androidx.compose.animation.core.tween\nimport androidx.compose.animation.core.FastOutSlowInEasing\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.remember\nimport androidx.compose.ui.graphics.graphicsLayer\n' app/src/main/java/com/example/ui/screens/MainScreen.kt

# Inject animation state inside StatusCard
sed -i '/val context = LocalContext.current/a\
\
    val alpha = remember { Animatable(0f) }\
    val offsetY = remember { Animatable(50f) }\
\
    LaunchedEffect(statusItem.id) {\
        alpha.animateTo(\
            targetValue = 1f,\
            animationSpec = tween(durationMillis = 500)\
        )\
    }\
    LaunchedEffect(statusItem.id) {\
        offsetY.animateTo(\
            targetValue = 0f,\
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)\
        )\
    }' app/src/main/java/com/example/ui/screens/MainScreen.kt

# Modify Card modifier to include graphicsLayer
sed -i '/\.fillMaxWidth()/a\
            .graphicsLayer {\
                this.alpha = alpha.value\
                this.translationY = offsetY.value\
            }' app/src/main/java/com/example/ui/screens/MainScreen.kt

