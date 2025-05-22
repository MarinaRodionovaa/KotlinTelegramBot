package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TG_URL = "https://api.telegram.org/bot"

fun main(args: Array<String>) {

    val botToken = args[0]
    val urlGetMe = "$TG_URL$botToken/getMe"
    val urlGetUpdates = "$TG_URL$botToken/getUpdates"

    val client = HttpClient.newBuilder().build()

    val requestGetMe = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()
    val requestGetUpdates = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

    val responseGetMe = client.send(requestGetMe, HttpResponse.BodyHandlers.ofString())
    val responseGetUpdates = client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())

    println(responseGetMe.body())
    println(responseGetUpdates.body())
}