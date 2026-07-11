#!/bin/bash
sed -i '/val context = LocalContext.current/a\
    val autoCleanupDays by viewModel.autoCleanupDays.collectAsState()' app/src/main/java/com/example/ui/screens/MainScreen.kt

sed -i '/\/\/ 4. Help & Feedback Panel/i\
        // 4. Auto Cleanup Panel\
        Card(\
            shape = RoundedCornerShape(16.dp),\
            colors = CardDefaults.cardColors(\
                containerColor = if (isSystemInDarkTheme() || themePreference == "dark") DarkCardBg else MaterialTheme.colorScheme.surface\
            ),\
            modifier = Modifier\
                .fillMaxWidth()\
                .padding(bottom = 12.dp)\
                .shadow(2.dp, RoundedCornerShape(16.dp))\
        ) {\
            Column(\
                modifier = Modifier.padding(16.dp)\
            ) {\
                Row(\
                    verticalAlignment = Alignment.CenterVertically\
                ) {\
                    Icon(\
                        imageVector = Icons.Default.Delete,\
                        contentDescription = null,\
                        tint = MaterialTheme.colorScheme.error,\
                        modifier = Modifier.size(24.dp)\
                    )\
                    Spacer(modifier = Modifier.width(12.dp))\
                    Column {\
                        Text(\
                            text = "Auto Cleanup Saved Statuses",\
                            style = MaterialTheme.typography.titleMedium,\
                            fontWeight = FontWeight.Bold\
                        )\
                        Text(\
                            text = "Automatically delete old saved statuses to free up storage.",\
                            style = MaterialTheme.typography.bodySmall,\
                            color = MaterialTheme.colorScheme.onSurfaceVariant\
                        )\
                    }\
                }\
                Spacer(modifier = Modifier.height(12.dp))\
                Row(\
                    modifier = Modifier.fillMaxWidth(),\
                    horizontalArrangement = Arrangement.SpaceBetween\
                ) {\
                    val durations = listOf(0 to "Off", 7 to "7 Days", 30 to "1 Month", 90 to "3 Months")\
                    durations.forEach { (days, title) ->\
                        val isSelected = autoCleanupDays == days\
                        OutlinedButton(\
                            onClick = { viewModel.setAutoCleanupDays(days) },\
                            colors = ButtonDefaults.outlinedButtonColors(\
                                containerColor = if (isSelected) MaterialTheme.colorScheme.errorContainer else Color.Transparent,\
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant\
                            ),\
                            shape = RoundedCornerShape(10.dp),\
                            modifier = Modifier\
                                .weight(1f)\
                                .padding(horizontal = 2.dp)\
                            ,\
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)\
                        ) {\
                            Text(text = title, style = MaterialTheme.typography.labelSmall, maxLines = 1)\
                        }\
                    }\
                }\
            }\
        }\
' app/src/main/java/com/example/ui/screens/MainScreen.kt
