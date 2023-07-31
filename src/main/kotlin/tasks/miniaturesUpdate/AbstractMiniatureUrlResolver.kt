package tasks.miniaturesUpdate

import web.client.CookieEagerHttpClient
import web.client.FastHttpClient
import web.client.GentleHttpClient
import web.client.HttpClientInterface
import web.url.Url

abstract class AbstractMiniatureUrlResolver(
    httpClient: HttpClientInterface?,
    pattern: String,
) : MiniatureUrlResolver {
    protected val httpClient = httpClient ?: CookieEagerHttpClient(GentleHttpClient(FastHttpClient()))
    protected val regex: Regex = Regex(pattern)

    override fun supports(url: String) = regex.containsMatchIn(url)

    protected fun getPictureId(photoUrl: Url): String
    {
        return regex.find(photoUrl.getUrl())?.groups?.get("pictureId")?.value // TODO
            ?: throw RuntimeException("FailedMatching")
    }
}
