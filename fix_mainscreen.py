import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = 0

for i, line in enumerate(lines):
    if skip > 0:
        skip -= 1
        continue
    
    if "import androidx.compose.animation.core.Animatable" in line and i < 6:
        continue
    if "import androidx.compose.animation.core.tween" in line and i < 6:
        continue
    if "import androidx.compose.animation.core.FastOutSlowInEasing" in line and i < 6:
        continue
    if "import androidx.compose.runtime.LaunchedEffect" in line and i < 6:
        continue
    if "import androidx.compose.runtime.remember" in line and i < 6:
        continue
    if "import androidx.compose.ui.graphics.graphicsLayer" in line and i < 6:
        continue

    if "val alpha = remember { Animatable(0f) }" in line:
        # Check if we are inside StatusCard
        # Look backwards a bit to see if we are in fun StatusCard
        is_in_status_card = False
        for j in range(max(0, len(new_lines)-20), len(new_lines)):
            if "fun StatusCard(" in new_lines[j]:
                is_in_status_card = True
                break
        if not is_in_status_card:
            # We are not in StatusCard, skip this and the following animation block
            # Skip until the end of the launched effect
            # We know exactly the number of lines added: 
            # val alpha...
            # val offsetY...
            # <empty>
            # LaunchedEffect...
            # ...
            # }
            # LaunchedEffect...
            # ...
            # }
            # Wait, it's easier to just skip these lines explicitly.
            # But we already read `val alpha`. Let's just skip next 15 lines.
            skip = 15
            continue
            
    if ".graphicsLayer {" in line:
        # check if we're in StatusCard
        is_in_status_card = False
        for j in range(max(0, len(new_lines)-40), len(new_lines)):
            if "fun StatusCard(" in new_lines[j]:
                is_in_status_card = True
                break
        if not is_in_status_card:
            skip = 3
            continue

    new_lines.append(line)

# Also add imports to the correct place
import_lines = """
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
"""

# find package
pkg_idx = -1
for i, line in enumerate(new_lines):
    if line.startswith("package com.example"):
        pkg_idx = i
        break

if pkg_idx != -1:
    new_lines.insert(pkg_idx + 1, import_lines)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.writelines(new_lines)

