package tasks.miniaturesUpdate

import web.url.Url

interface MiniatureUrlResolver {
    fun supports(url: String): Boolean
    fun getMiniatureUrl(url: Url): String
}
