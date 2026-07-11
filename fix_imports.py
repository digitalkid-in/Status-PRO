with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
imports = set()

for line in lines:
    if line.startswith("import "):
        if line not in imports:
            imports.add(line)
            new_lines.append(line)
    else:
        new_lines.append(line)

# Add missing Animatable
new_lines.insert(2, "import androidx.compose.animation.core.Animatable\n")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.writelines(new_lines)
