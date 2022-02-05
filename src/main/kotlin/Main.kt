// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/*
{
  "Results": [
 */
@Serializable
class ffitemdata(
    var Name: String,
    var Icon: String
)

@Serializable
class searchoutput(
    var Results: List<ffitemdata>
)

val http = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("") }
//Item Output from Search Bar
    val items by produceState<List<ffitemdata>>(emptyList()) {
        val response = http.get("https://xivapi.com/item").body<searchoutput>()
        value = response.Results
    }

    MaterialTheme {
        Column {
            TextField(text, {
                text = it
            })
            val stateVertical = rememberScrollState(0)
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(stateVertical)
                ) {
                    Column {
                        items.forEach { item ->
                            Text(item.Name)
                            if (item.Icon.isEmpty()) {
                                UrlImage(url = "https://cdn.discordapp.com/attachments/194678146925199360/939367290196070430/744599448998641675.webp")
                            } else {
                                UrlImage(url = "https://xivapi.com${item.Icon}")
                            }
                        }
                    }
                }
                VerticalScrollbar(
                    rememberScrollbarAdapter(stateVertical),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
fun UrlImage(url: String) {
    val imageData by produceState<ByteArray?>(null, url) {
        delay(100)
        value = http.get(url).body<ByteArray>()
    }
    if (imageData != null) {
        Image(
            org.jetbrains.skia.Image.makeFromEncoded(imageData).toComposeImageBitmap(),
            contentDescription = null,
        )
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
