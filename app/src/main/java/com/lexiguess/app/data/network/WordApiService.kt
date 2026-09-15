package com.lexiguess.app.data.network

import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Retrofit interface for fetching optional dictionary word lists.
 */
interface WordApiService {

    /**
     * Fetches a plain-text word list from [url].
     * The @Url annotation makes the endpoint fully configurable at call-site
     * so it can be overridden in tests.
     */
    @GET
    suspend fun fetchWordList(@Url url: String = WORD_LIST_URL): String

    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/"
        const val WORD_LIST_URL =
            "https://raw.githubusercontent.com/raun/Scrabble/master/words.txt"
    }
}
