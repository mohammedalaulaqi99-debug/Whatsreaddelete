package com.example.readdelete

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.readdelete.data.ReadDeleteDatabase
import com.example.readdelete.data.TrackedMessage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val dao by lazy { ReadDeleteDatabase.get(this).trackedMessageDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val messages by dao.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
                ReadDeleteScreen(
                    messages = messages,
                    onRegister = { recipient, body, delay, group, source ->
                        lifecycleScope.launch {
                            dao.insert(
                                TrackedMessage(
                                    recipient = recipient.trim(),
                                    messageText = body,
                                    sentAtMillis = System.currentTimeMillis(),
                                    delaySeconds = delay,
                                    isGroup = group,
                                    source = source
                                )
                            )
                        }
                    },
                    onDeleteRecord = { id -> lifecycleScope.launch { dao.delete(id) } },
                    onOpenWhatsApp = { body -> openWhatsApp(body) },
                    onAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }
        }
    }

    private fun openWhatsApp(body: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
            setPackage("com.whatsapp")
        }
        try {
            startActivity(send)
        } catch (_: Exception) {
            val browser = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/"))
            startActivity(browser)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadDeleteScreen(
    messages: List<TrackedMessage>,
    onRegister: (String, String, Int, Boolean, String) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onOpenWhatsApp: (String) -> Unit,
    onAccessibilitySettings: () -> Unit
) {
    var recipient by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var delayText by remember { mutableStateOf("5") }
    var isGroup by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf("compose_send") }
    var info by remember { mutableStateOf("Read detection is in feasibility-test mode. No messages are automatically deleted.") }

    val delay = delayText.toIntOrNull()?.coerceIn(1, 86400)

    Scaffold(
        topBar = { TopAppBar(title = { Text("ReadDelete · v0.1") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Register a message", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = recipient, onValueChange = { recipient = it },
                    label = { Text("Recipient or group name") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body, onValueChange = { body = it },
                    label = { Text("Exact message text") }, modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = delayText, onValueChange = { delayText = it.filter(Char::isDigit) },
                    label = { Text("Delay after confirmed read (seconds)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = isGroup, onCheckedChange = { isGroup = it })
                    Text("This is a group conversation")
                }
                Text("Registration method", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = source == "compose_send", onClick = { source = "compose_send" })
                    Text("Send through ReadDelete")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = source == "existing_manual", onClick = { source = "existing_manual" })
                    Text("Register an already-sent message")
                }
                Button(
                    onClick = {
                        if (recipient.isNotBlank() && body.isNotBlank() && delay != null) {
                            onRegister(recipient, body, delay, isGroup, source)
                            if (source == "compose_send") onOpenWhatsApp(body)
                            info = if (source == "compose_send")
                                "Registered locally. WhatsApp opened with your message; select the chat and send it. Registration does not prove it was sent."
                            else
                                "Existing message registered manually. Confirm the recipient and exact text carefully."
                            body = ""
                        } else {
                            info = "Enter a recipient, message and valid delay (1–86400 seconds)."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Register message") }
                OutlinedButton(onClick = onAccessibilitySettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Accessibility settings")
                }
                Text(info, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Text("Tracked messages (${messages.size})", style = MaterialTheme.typography.titleLarge)
            }
            items(messages, key = { it.id }) { message ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(message.recipient, style = MaterialTheme.typography.titleMedium)
                        Text(message.messageText)
                        Text(
                            "${if (message.isGroup) "Group" else "Individual"} · ${message.source} · ${message.delaySeconds}s · ${message.status}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text("Registered: ${java.text.DateFormat.getDateTimeInstance().format(message.sentAtMillis)}",
                            style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { onDeleteRecord(message.id) }) { Text("Remove tracking record") }
                    }
                }
            }
            item {
                Text(
                    "Safety note: v0.1 stores tracking records and opens WhatsApp. It does not yet detect read receipts or execute Delete for everyone.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
