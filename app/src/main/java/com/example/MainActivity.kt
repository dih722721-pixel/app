package com.example

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

private const val TARGET_URL = "https://sites.google.com/view/schoolblackmarket"

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        SchoolMarketScreen()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SchoolMarketScreen() {
  val context = LocalContext.current
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var progress by remember { mutableFloatStateOf(0f) }
  var canGoBack by remember { mutableStateOf(false) }
  var hasError by remember { mutableStateOf(false) }
  var pageTitle by remember { mutableStateOf("School Black Market") }
  var currentUrl by remember { mutableStateOf(TARGET_URL) }

  // Handle hardware / gesture back button
  BackHandler(enabled = canGoBack) {
    webViewInstance?.let { webView ->
      if (webView.canGoBack()) {
        webView.goBack()
      }
    }
  }

  // Cleanup WebView on disposal
  DisposableEffect(Unit) {
    onDispose {
      webViewInstance?.apply {
        stopLoading()
        onPause()
        destroy()
      }
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      Column(modifier = Modifier.fillMaxWidth()) {
        CenterAlignedTopAppBar(
          title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = pageTitle.ifBlank { stringResource(R.string.app_name) },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = "Secure Connection",
                  modifier = Modifier.size(12.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "sites.google.com",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1
                )
              }
            }
          },
          navigationIcon = {
            if (canGoBack) {
              IconButton(
                onClick = { webViewInstance?.goBack() },
                modifier = Modifier.testTag("back_button")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back"
                )
              }
            }
          },
          actions = {
            IconButton(
              onClick = {
                hasError = false
                webViewInstance?.reload()
              },
              modifier = Modifier.testTag("refresh_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.web_refresh)
              )
            }
            IconButton(
              onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl.ifEmpty { TARGET_URL }))
                context.startActivity(intent)
              },
              modifier = Modifier.testTag("open_in_browser_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.web_open_in_browser)
              )
            }
          },
          colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        )

        AnimatedVisibility(
          visible = isLoading && progress < 1.0f,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(3.dp)
              .testTag("loading_progress_bar"),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("webview_container")
    ) {
      AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
          WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              databaseEnabled = true
              cacheMode = WebSettings.LOAD_DEFAULT
              useWideViewPort = true
              loadWithOverviewMode = true
              setSupportZoom(true)
              builtInZoomControls = true
              displayZoomControls = false
              allowFileAccess = false
              allowContentAccess = false
              mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }

            webChromeClient = object : WebChromeClient() {
              override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress = newProgress / 100f
                if (newProgress >= 100) {
                  isLoading = false
                }
              }

              override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!title.isNullOrBlank() && !title.startsWith("http")) {
                  pageTitle = title
                }
              }
            }

            webViewClient = object : WebViewClient() {
              override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isLoading = true
                hasError = false
                if (url != null) {
                  currentUrl = url
                }
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoading = false
                canGoBack = view?.canGoBack() == true
                if (url != null) {
                  currentUrl = url
                }
              }

              override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
              ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                  hasError = true
                  isLoading = false
                }
              }

              override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
              ): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme ?: ""
                return if (scheme == "http" || scheme == "https") {
                  false
                } else {
                  try {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                  } catch (_: Exception) {
                    // Ignore unsupported intent schemes
                  }
                  true
                }
              }
            }

            loadUrl(TARGET_URL)
            webViewInstance = this
          }
        },
        update = { webView ->
          webViewInstance = webView
        }
      )

      if (hasError) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
              text = stringResource(R.string.web_error_title),
              style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = stringResource(R.string.web_error_desc),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
              onClick = {
                hasError = false
                isLoading = true
                webViewInstance?.reload() ?: webViewInstance?.loadUrl(TARGET_URL)
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("retry_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = stringResource(R.string.web_retry))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
              onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TARGET_URL))
                context.startActivity(intent)
              },
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = stringResource(R.string.web_open_in_browser))
            }
          }
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun SchoolMarketPreview() {
  MyApplicationTheme {
    SchoolMarketScreen()
  }
}

