package com.example.workshipflow.presentation.browser

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.workshipflow.ui.WorshipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: WorshipViewModel,
    initialQuery: String?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    val initialUrl = if (initialQuery.isNullOrBlank()) {
        "https://duckduckgo.com"
    } else {
        "https://duckduckgo.com/?q=$initialQuery+chords"
    }

    var currentUrl by remember { mutableStateOf(initialUrl) }
    var isExtracting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Song Browser", fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { webView?.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Web Back")
                    }
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val url = webView?.url
                    if (url != null) {
                        isExtracting = true
                        webView?.evaluateJavascript(
                            "(function() { return document.documentElement.outerHTML; })();"
                        ) { htmlJson ->
                            val html = htmlJson?.let {
                                if (it.startsWith("\"") && it.endsWith("\"")) {
                                    it.substring(1, it.length - 1)
                                        .replace("\\u003C", "<")
                                        .replace("\\\"", "\"")
                                        .replace("\\n", "\n")
                                        .replace("\\r", "\r")
                                } else it
                            } ?: ""

                            viewModel.parseAndSave(
                                url,
                                html,
                                onSuccess = { id ->
                                    Toast.makeText(context, "Song Saved", Toast.LENGTH_SHORT).show()
                                    isExtracting = false
                                    onSaved(id)
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    isExtracting = false
                                }
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (isExtracting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = "Extract")
                        Spacer(Modifier.width(8.dp))
                        Text("Extract Song")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                currentUrl = url ?: ""
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        loadUrl(currentUrl)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
