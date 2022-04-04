package ru.home.swap.network

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import ru.home.swap.App

class MockInterceptor(val context: Context): Interceptor {

    companion object {
        const val API_ACCOUNT = "/api/account"
        const val API_OFFERS = "/api/account/offers"
        const val API_DEMANDS = "/api/account/demands"

        const val API_OFFERS_DELETE = "/api/account/offers?id=0"
        const val API_DEMANDS_DELETE = "/api/account/demands?id=0"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d(App.TAG, "[0] Network interceptor is called")
        var request = chain.request()
        val url = request.url().uri().toString()

        val msg = getMessage(getMockFileName(url, request.method()!!))
        Log.d(App.TAG, "[0] $msg")
        val body = getBody(msg)

        return okhttp3.Response.Builder()
            .request(chain.request())
            .code(200)
            .protocol(Protocol.HTTP_2)
            .message(msg)
            .body(body)
            .addHeader("content-type", "application/json")
            .build()
    }

    private fun getMockFileName(url: String, method: String): String {
        var msg = ""
        val uri = Uri.parse(url)
        if (matchPath(uri.pathSegments, API_ACCOUNT.split("?").first())
            /*&& matchQuery(uri.queryParameterNames, API_ACCOUNT.split("?").last())*/
            && method.equals("POST")) {
            msg = "mock_api_profile_post_response.json"
        } else if (matchPath(uri.pathSegments, API_ACCOUNT.split("?").first())
            /*&& matchQuery(uri.queryParameterNames, API_ACCOUNT.split("?").last())*/
            && method.equals("GET")
        ) {
            msg = "mock_api_profile_get_response.json"
        } else if(matchPath(uri.pathSegments, API_OFFERS.split("?").first())
            && method.equals("POST")
        ) {
            msg = "mock_api_profile_post_offer_response.json"
        } else if (matchPath(uri.pathSegments, API_DEMANDS.split("?").first())
            && method.equals("POST")
        ) {
            msg = "mock_api_profile_post_demand_response.json"
        } else if (matchPath(uri.pathSegments, API_OFFERS_DELETE.split("?").first())
            && matchQuery(uri.queryParameterNames, API_OFFERS_DELETE.split("?").last())
            && method.equals("DELETE")) {
            msg = "mock_api_profile_delete_offer_response.json"
        } else if (matchPath(uri.pathSegments, API_DEMANDS_DELETE.split("?").first())
            && matchQuery(uri.queryParameterNames, API_DEMANDS_DELETE.split("?").last())
            && method.equals("DELETE")) {
            msg = "mock_api_profile_delete_demand_response.json"
        } else {
            throw IllegalArgumentException("Url $url is not supported by interceptor")
        }
        return msg
    }

    private fun getMessage(jsonName: String): String {
        val str = context.assets.open(jsonName).bufferedReader().use { it.readText() }
        return str
    }

    private fun getBody(msg: String): ResponseBody {
        return ResponseBody.create(
            MediaType.parse("application/json"),
            msg.toByteArray()
        )
    }

    private fun matchPath(pathSegments: List<String>, urlPattern: String): Boolean {
        var patternTokens = urlPattern.split("/")
        patternTokens = patternTokens.subList(1, patternTokens.size)
        return pathSegments.size == patternTokens.size
                && pathSegments.intersect(patternTokens.toSet()).size == patternTokens.size
    }

    private fun matchQuery(queryParameterNames: MutableSet<String>, urlPattern: String): Boolean {
        val queryPairs = urlPattern.split("&")
        val patternQueryParameterNames = mutableListOf<String>()
        for (pair in queryPairs) {
            patternQueryParameterNames.add(pair.split("=").first())
        }
        return queryParameterNames.intersect(patternQueryParameterNames.toSet()).size == patternQueryParameterNames.size
    }
}