package com.aibuilt.porn.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoItem(
    val id: String,
    val title: String,
    val duration: Int,
    val source: String,
    val embed: String,
    val category: String,
    val age: Int? = null,
    val views: Long = 0L,
    val date: Long = 0L,
    val thumb: String? = null
) {
    val thumbnailUrl: String
        get() = thumb?.takeIf { it.isNotBlank() }
            ?: derivedThumb()
            ?: "https://s0.wp.com/mshots/v1/${encode(watchUrl)}?w=640&h=360"

    val watchUrl: String
        get() = buildWatchUrl(embed)

    private fun derivedThumb(): String? = when {
        embed.contains("youtube.com/embed/") ->
            "https://img.youtube.com/vi/${embed.substringAfterLast("/")}/hqdefault.jpg"
        embed.contains("dailymotion.com/embed/video/") ->
            "https://www.dailymotion.com/thumbnail/video/${embed.substringAfterLast("/")}"
        embed.contains("archive.org/embed/") ->
            "https://archive.org/services/img/${embed.substringAfterLast("/")}"
        else -> null
    }

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8")

    companion object {
        fun buildWatchUrl(embed: String): String = when {
            embed.contains("pornhub.com/embed/") ->
                "https://www.pornhub.com/view_video.php?viewkey=" + embed.substringAfterLast("/embed/")
            embed.contains("xembed.php?video=") ->
                "https://xhamster.com/videos/" + embed.substringAfter("video=")
            embed.contains("eporner.com/embed/") ->
                "https://www.eporner.com/video-" + embed.substringAfterLast("/embed/").trimEnd('/') + "/"
            embed.contains("spankbang.com/") ->
                "https://spankbang.com/" + embed.substringAfter("spankbang.com/").substringBefore("/") + "/video/"
            embed.contains("xnxx.com/embedframe/") ->
                "https://www.xnxx.com/video-" + embed.substringAfterLast("/") + "/"
            embed.contains("txxx.com/embed/") ->
                "https://txxx.com/videos/" + embed.substringAfterLast("/") + "/"
            embed.contains("hqporner.com/embed/") ->
                "https://hqporner.com/hdporn/" + embed.substringAfterLast("/")
            embed.contains("youporn.com/embed/") ->
                "https://www.youporn.com/watch/" + embed.substringAfterLast("/embed/").trimEnd('/') + "/"
            embed.contains("youtube.com/embed/") ->
                "https://www.youtube.com/watch?v=" + embed.substringAfterLast("/")
            embed.contains("dailymotion.com/embed/video/") ->
                "https://www.dailymotion.com/video/" + embed.substringAfterLast("/")
            embed.contains("ok.ru/videoembed/") ->
                "https://ok.ru/video/" + embed.substringAfterLast("/")
            embed.contains("archive.org/embed/") ->
                "https://archive.org/details/" + embed.substringAfterLast("/")
            else -> embed
        }
    }
}

@Serializable
data class TorLink(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val group: String
)

data class AppCategory(val name: String, val slug: String)

object Categories {
    val ALL = listOf(
        AppCategory("Lesbian", "lesbian"),
        AppCategory("Threesome", "threesome"),
        AppCategory("Anal", "anal"),
        AppCategory("MILF", "milf"),
        AppCategory("Teen", "teen"),
        AppCategory("Big Tits", "big-tits"),
        AppCategory("Amateur", "amateur"),
        AppCategory("Creampie", "creampie"),
        AppCategory("POV", "pov"),
        AppCategory("Squirt", "squirt"),
        AppCategory("Gangbang", "gangbang"),
        AppCategory("Interracial", "interracial"),
        AppCategory("Ebony", "ebony"),
        AppCategory("Asian", "asian"),
        AppCategory("Latina", "latina"),
        AppCategory("Solo", "solo"),
        AppCategory("Blowjob", "blowjob"),
        AppCategory("Hardcore", "hardcore"),
        AppCategory("Russian", "russian"),
        AppCategory("Compilation", "compilation")
    )

    fun bySlug(slug: String?): AppCategory? = ALL.firstOrNull { it.slug == slug }
}

enum class SortOrder(val label: String) {
    NEWEST("Newest"),
    LONGEST("Longest"),
    MOST_VIEWED("Most viewed")
}

enum class VideoAction { OPEN_BROWSER, COPY, HIDE }
