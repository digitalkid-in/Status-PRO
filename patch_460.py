with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    lines = f.readlines()

# line 460 is index 459
# 455:                            Icon(
# 456:                                imageVector = Icons.Default.Loop,
# 457:                                contentDescription = "Toggle Theme ($themePreference)",
# 458:                                tint = MaterialTheme.colorScheme.onBackground,
# 459:                                modifier = Modifier
# 460:                                    .size(18.dp)
# 461:                }

# Let's just fix it by adding )
lines.insert(460, "                            )\n")
lines.insert(461, "                        }\n")
lines.insert(462, "                    }\n")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.writelines(lines)
