import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# 1. Update State Variables
content = re.sub(
    r'var activeTab by remember \{ mutableIntStateOf\(0\) \} // 0: Images, 1: Videos, 2: Saved, 3: Settings/About',
    'var bottomNavTab by remember { mutableIntStateOf(0) }\n    var homeSubTab by remember { mutableIntStateOf(0) }',
    content
)

# 2. Update TopBar
topbar_replacement = """                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            withStyle(androidx.compose.ui.text.SpanStyle(color = if (isSystemInDarkTheme() || themePreference == "dark") Color.White else Color.Black)) {
                                append("Status")
                            }
                            withStyle(androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append("PRO")
                            }
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    var expanded by remember { mutableStateOf(false) }
                    
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .clickable { expanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isBusinessMode) Icons.Default.Business else Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBusinessMode) "Business" else "Personal",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Personal Mode", fontWeight = FontWeight.Medium) },
                                onClick = {
                                    viewModel.setBusinessMode(false)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = if (!isBusinessMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Business Mode", fontWeight = FontWeight.Medium) },
                                onClick = {
                                    viewModel.setBusinessMode(true)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = if (isBusinessMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            )
                        }
                    }
                }
"""

content = re.sub(
    r'                // Header row\n                Row\(\n                    modifier = Modifier\.fillMaxWidth\(\),\n                    horizontalArrangement = Arrangement\.SpaceBetween,\n                    verticalAlignment = Alignment\.CenterVertically\n                \) \{.*?Theme switch button.*?\}\n                    \}\n                \}',
    topbar_replacement,
    content,
    flags=re.DOTALL
)

# Need to add import for withStyle
content = content.replace('import androidx.compose.ui.text.style.TextOverflow', 'import androidx.compose.ui.text.style.TextOverflow\nimport androidx.compose.ui.text.withStyle')


# 3. BottomNav update
bottomnav_replacement = """                // Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val items = listOf(
                        Triple("Home", Icons.Default.Home, 0),
                        Triple("Downloads", Icons.Default.Download, 1),
                        Triple("Settings", Icons.Default.Settings, 2)
                    )

                    items.forEach { (label, icon, index) ->
                        NavigationBarItem(
                            selected = bottomNavTab == index,
                            onClick = { bottomNavTab = index },
                            icon = { 
                                if (index == 1 && savedStatuses.isNotEmpty()) {
                                    androidx.compose.material3.BadgedBox(
                                        badge = {
                                            androidx.compose.material3.Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White
                                            ) {
                                                Text(savedStatuses.size.toString())
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = icon, contentDescription = label)
                                    }
                                } else {
                                    Icon(imageVector = icon, contentDescription = label)
                                }
                            },
                            label = { Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }"""

content = re.sub(
    r'                // Navigation Bar.*?\}\n                \}\n            \}',
    bottomnav_replacement + '\n            }',
    content,
    flags=re.DOTALL
)

# Ensure icons imported
content = content.replace('import androidx.compose.material.icons.filled.Help', 'import androidx.compose.material.icons.filled.Help\nimport androidx.compose.material.icons.filled.Home\nimport androidx.compose.material.icons.filled.Download\nimport androidx.compose.material.icons.filled.VideoLibrary')

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
