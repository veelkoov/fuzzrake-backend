package e2e

import data.UrlType
import database.entities.Creator
import database.entities.CreatorUrl
import database.helpers.getMiniatureUrls
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.cookies.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.utils.io.*
import org.jetbrains.exposed.dao.entityCache
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.jupiter.api.Test
import tasks.MiniaturesUpdate
import testUtils.disposableDatabase
import testUtils.getNullConfiguration
import web.client.FastHttpClient
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class MiniaturesUpdateTest {
    data class ExpectedHttpCall(
        val url: String,
        val requestJsonContent: String?,
        val requestHeaders: Map<String, String>,
        val responseContent: String,
        val responseHeaders: Map<String, String>,
    )

    @Test
    fun execute() = disposableDatabase { database, transaction ->
        val creatorId = setupEntities()

        transaction.commit()
        transaction.entityCache.flush()

        val httpCalls = getExpectedHttpCalls()
        val httpClient = getHttpClient(httpCalls)

        // Execution

        val subject = MiniaturesUpdate(getNullConfiguration(), database, httpClient)
        subject.execute()

        transaction.commit()
        transaction.entityCache.flush()

        // Verification

        val creator = Creator.findById(creatorId)
        assertNotNull(creator)

        val expected = setOf(
            "https://orca.furtrack.com/gallery/thumb/49767-b29a0ffc76f98b18ebb5a0a7e394bbab.jpg",
            "https://storage.scritch.es/scritch/45fbfc5483674d20dfd4cf6a342ea6653bd70440/thumbnail_9989c527-725a-4e98-b916-004c7ed91716.jpeg",
            "https://orca.furtrack.com/gallery/thumb/41933-68dcba69d82cdafe787b42f2a52b49b6.jpg",
            "https://storage.scritch.es/scritch/2a8ff452966723efe44ac65db076778e299e6824/thumbnail_77263eca-0ac2-4446-b86d-1f1fe21569a6.jpeg",
        )

        val actual = creator.getMiniatureUrls().map { it.url }.toSet()

        assertEquals(expected, actual)
    }

    private fun getHttpClient(httpCalls: MutableList<ExpectedHttpCall>): FastHttpClient {
        val mockEngine = MockEngine {
            val httpCall = httpCalls.removeFirst()

            assertEquals(httpCall.url, it.url.toString(), "Wrong call order")

            if (httpCall.requestJsonContent != null) {
                // Not pretty sure if testing the "application/json" header properly here
                assertIs<TextContent>(it.body)
                assertEquals(
                    httpCall.requestJsonContent, (it.body as TextContent).text,
                    "Wrong request payload"
                )
                assertEquals(
                    "application/json", it.body.contentType.toString(),
                    "Wrong request payload type"
                )
            }

            httpCall.requestHeaders.forEach { (header, value) ->
                assertEquals(
                    it.headers.getAll(header), listOf(value),
                    "Wrong request headers for '${httpCall.url}' call"
                )
            }

            respond(
                content = ByteReadChannel(httpCall.responseContent),
                headers = HeadersImpl(httpCall.responseHeaders.map { (header, value) ->
                    header to listOf(value)
                }.toMap()),
            )
        }

        return FastHttpClient(HttpClient(mockEngine) { install(HttpCookies) })
    }

    private fun setupEntities(): EntityID<Int> {
        val creator = Creator.new { creatorId = "CREATOR" }
        val creatorId = creator.id

        CreatorUrl.new {
            this.creator = creator
            url = "https://www.furtrack.com/p/49767"
            type = UrlType.URL_PHOTOS.name
        }
        CreatorUrl.new {
            this.creator = creator
            url = "https://scritch.es/pictures/847486df-64fc-45a2-b74b-11fd87fe43ca"
            type = UrlType.URL_PHOTOS.name
        }
        CreatorUrl.new {
            this.creator = creator
            url = "https://www.furtrack.com/p/41933"
            type = UrlType.URL_PHOTOS.name
        }
        CreatorUrl.new {
            this.creator = creator
            url = "https://scritch.es/pictures/b4a47593-f0e2-43b4-bc74-df6b9c3f555f"
            type = UrlType.URL_PHOTOS.name
        }

        return creatorId
    }

    private fun getExpectedHttpCalls() = mutableListOf(
        ExpectedHttpCall(
            "https://solar.furtrack.com/view/post/49767",
            null,
            mapOf(),
            "{\"post\": {\"postStub\": \"49767-b29a0ffc76f98b18ebb5a0a7e394bbab\", \"metaFiletype\": \"jpg\"}}",
            mapOf(),
        ),
        ExpectedHttpCall(
            "https://scritch.es/",
            null,
            mapOf(),
            "",
            mapOf("Set-Cookie" to "csrf-token=%21%40%23%24%25%5E%26*%28%29; path=/; SameSite=Strict"),
        ),
        ExpectedHttpCall(
            "https://scritch.es/graphql",
            "{\"operationName\": \"Medium\", \"variables\": {\"id\": \"847486df-64fc-45a2-b74b-11fd87fe43ca\"}, \"query\": \"query Medium(\$id: ID!, \$tagging: Boolean) { medium(id: \$id, tagging: \$tagging) { thumbnail } }\"}",
            mapOf(
                "authorization" to "Scritcher !@#\$%^&*()",
                "X-CSRF-Token" to "!@#\$%^&*()",
            ),
            "{\"data\": {\"medium\": {\"thumbnail\": \"https://storage.scritch.es/scritch/45fbfc5483674d20dfd4cf6a342ea6653bd70440/thumbnail_9989c527-725a-4e98-b916-004c7ed91716.jpeg\"}}}",
            mapOf(),
        ),
        ExpectedHttpCall(
            "https://solar.furtrack.com/view/post/41933",
            null,
            mapOf(),
            "{\"post\": {\"postStub\": \"41933-68dcba69d82cdafe787b42f2a52b49b6\", \"metaFiletype\": \"jpg\"}}",
            mapOf(),
        ),
        ExpectedHttpCall(
            "https://scritch.es/graphql",
            "{\"operationName\": \"Medium\", \"variables\": {\"id\": \"b4a47593-f0e2-43b4-bc74-df6b9c3f555f\"}, \"query\": \"query Medium(\$id: ID!, \$tagging: Boolean) { medium(id: \$id, tagging: \$tagging) { thumbnail } }\"}",
            mapOf(
                "authorization" to "Scritcher !@#\$%^&*()",
                "X-CSRF-Token" to "!@#\$%^&*()",
            ),
            "{\"data\": {\"medium\": {\"thumbnail\": \"https://storage.scritch.es/scritch/2a8ff452966723efe44ac65db076778e299e6824/thumbnail_77263eca-0ac2-4446-b86d-1f1fe21569a6.jpeg\"}}}",
            mapOf(),
        ),
    )
}