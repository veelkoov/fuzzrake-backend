package tasks.miniaturesUpdate

import data.JsonNavigator
import io.ktor.http.*
import web.client.HttpClientInterface
import web.url.FreeUrl
import web.url.Url

class ScritchMiniatureUrlResolver(
    httpClient: HttpClientInterface? = null,
) : AbstractMiniatureUrlResolver(
    httpClient,
    "^https://scritch\\.es/pictures/(?<pictureId>[-a-f0-9]{36})\$",
) {
    private val graphQlUrl = FreeUrl("https://scritch.es/graphql")

    override fun getMiniatureUrl(url: Url): String {
        val csrfToken = getCsrfToken()
        val pictureId = getPictureId(url)
        val jsonPayload = getGraphQlJsonPayload(pictureId)

        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-CSRF-Token" to csrfToken,
            "authorization" to "Scritcher $csrfToken",
        )

        val response = httpClient.fetch(graphQlUrl, HttpMethod.Post, headers, jsonPayload)

        if (response.metadata.httpCode != 200) {
            throw RuntimeException("Http code ${response.metadata.httpCode}")
        }

        return JsonNavigator(response.contents)
            .getString("data/medium/thumbnail") // TODO: Expect non-empty?
    }

    private fun getGraphQlJsonPayload(pictureId: String): String
    {
        return "{\"operationName\": \"Medium\", \"variables\": {\"id\": \"$pictureId\"}, \"query\": \"query " +
                "Medium(\$id: ID!, \$tagging: Boolean) { medium(id: \$id, tagging: \$tagging) { thumbnail } }\"}"
    }

    private fun getCsrfToken(): String = getOptionalCsrfToken() ?: getFirstRequiredCsrfToken()

    private fun getOptionalCsrfToken(): String? {
        return httpClient.getSingleCookieValue("https://scritch.es/", "csrf-token")
    }

    private fun getFirstRequiredCsrfToken(): String {
        httpClient.fetch(FreeUrl("https://scritch.es/"))

        return getOptionalCsrfToken() ?: throw RuntimeException("Missing csrf-token cookie")
    }
}
