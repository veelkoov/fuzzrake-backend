package tasks.miniaturesUpdate

import data.JsonNavigator
import web.client.HttpClientInterface
import web.url.FreeUrl
import web.url.Url

class FurtrackMiniatureUrlResolver(
    httpClient: HttpClientInterface? = null,
) : AbstractMiniatureUrlResolver(
    httpClient,
    "^https://www.furtrack.com/p/(?<pictureId>\\d+)\$",
) {
    override fun getMiniatureUrl(url: Url): String {
        val pictureId = getPictureId(url)
        val response = httpClient.fetch(FreeUrl("https://solar.furtrack.com/view/post/$pictureId"))

        if (response.metadata.httpCode != 200) {
            throw RuntimeException("Http code ${response.metadata.httpCode}")
        }

        val json = JsonNavigator(response.contents)
        val postStub = json.getString("post/postStub") // TODO: non-empty?
        val metaFiletype = json.getString("post/metaFiletype") // TODO: non-empty?

        return "https://orca.furtrack.com/gallery/thumb/$postStub.$metaFiletype";
    }
}
