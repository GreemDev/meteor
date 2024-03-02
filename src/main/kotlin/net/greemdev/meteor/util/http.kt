/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:Suppress("MemberVisibilityCanBePrivate", "unused") // API

package net.greemdev.meteor.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import meteordevelopment.meteorclient.utils.other.JsonDateDeserializer
import net.greemdev.meteor.*
import java.io.InputStream
import java.io.InputStreamReader
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.time.toJavaDuration

private val httpClient by lazy {
    HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(25))
        .build()
}

private val gson = GsonBuilder().apply {
    registerTypeAdapter(Date::class.java, JsonDateDeserializer())
}.create()

@Suppress("FunctionName")
// formatted for interoperability, discouraging usage of java-specific functions from Kotlin because the names are ugly and vice versa for kotlin functions from java
// i.e., prefer `requestLines` from kotlin, and not `java-requestLines`, and from java, prefer `requestLines` and not `ktRequestLines`.
// the functions have identical names because of how they're defined here via the @Jvm annotations,
// and yet they both have method signatures that are different, and best used from the respective language.
// (example, no Unit-returning lambdas for java functions, instead preferring standard java functional interfaces.)

object HTTP {
    @JvmStatic infix fun get(url: String) = GET(url)
    @JvmStatic infix fun post(url: String) = POST(url)
    @JvmStatic infix fun delete(url: String) = DELETE(url)
    @JvmStatic infix fun patch(url: String) = PATCH(url)
    @JvmStatic infix fun put(url: String) = PUT(url)

    @JvmStatic infix fun GET(url: String) = Request.Method.GET(url)
    @JvmStatic infix fun POST(url: String) = Request.Method.POST(url)
    @JvmStatic infix fun DELETE(url: String) = Request.Method.DELETE(url)
    @JvmStatic infix fun PATCH(url: String) = Request.Method.PATCH(url)
    @JvmStatic infix fun PUT(url: String) = Request.Method.PUT(url)


    class Request private constructor(
        private val builder: HttpRequest.Builder,
        private var httpMethod: Method
    ) {
        private var bodyProvided = false
        private var bodyPublisher: HttpRequest.BodyPublisher = HttpRequest.BodyPublishers.noBody()
            set(value) {
                require(!bodyProvided) { "Request already has a body." }

                field = value

                bodyProvided = true
            }

        fun bearer(token: String) =
            setHeaders("Authorization" to "Bearer $token")

        fun headers(vararg headers: Pair<String, String>): Request {
            headers.forEach { (name, value) -> builder.header(name, value) }
            return this
        }

        fun setHeaders(vararg headers: Pair<String, String>): Request {
            headers.forEach { (name, value) -> builder.setHeader(name, value) }
            return this
        }

        fun timeout(duration: kotlin.time.Duration) = timeout(duration.toJavaDuration())
        fun timeout(duration: Duration): Request {
            builder.timeout(duration)
            return this
        }

        fun bodyPlaintext(text: String) =
            bodyInternal("text/plain", HttpRequest.BodyPublishers.ofString(text))

        fun bodyForm(form: String) =
            bodyInternal("application/w-www-form-urlencoded", HttpRequest.BodyPublishers.ofString(form))

        fun bodyJson(body: Any) = bodyJson(gson.toJson(body))
        fun bodyJson(json: String) =
            bodyInternal("application/json", HttpRequest.BodyPublishers.ofString(json))

        fun bodyBytes(mediaType: String, bytes: ByteArray) =
            bodyInternal(mediaType, HttpRequest.BodyPublishers.ofByteArray(bytes))

        fun bodyStream(mediaType: String, inputStream: Getter<InputStream?>) =
            bodyInternal(mediaType, HttpRequest.BodyPublishers.ofInputStream(inputStream))

        fun bodyFromFile(mediaType: String, path: Path) =
            bodyInternal(mediaType, HttpRequest.BodyPublishers.ofFile(path))




        /**
         * Used for requests where the response is pointless, such as DELETE
         */
        @JvmName("ktSend")
        fun send(beforeValidation: Initializer<StatusCodeHandler<*>> = {}) {
            sendInternal("*/*", HttpResponse.BodyHandlers.discarding(), beforeValidation)
        }

        @JvmName("send")
        @JvmOverloads
        fun `java-send`(beforeValidation: Consumer<StatusCodeHandler<*>> = Consumer {}) = send(beforeValidation.kotlin)



        // Blocking requesting
        @JvmName("ktRequestInputStream")
        fun requestInputStream(beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}) =
            sendInternal("*/*", HttpResponse.BodyHandlers.ofInputStream(), beforeValidation)

        @JvmOverloads
        @JvmName("requestInputStream")
        fun `java-requestInputStream`(beforeValidation: Consumer<StatusCodeHandler<InputStream>> = Consumer { }) =
            requestInputStream(beforeValidation.kotlin)

        @JvmName("ktRequestString")
        fun requestString(beforeValidation: Initializer<StatusCodeHandler<String>> = {}) =
            sendInternal("*/*", HttpResponse.BodyHandlers.ofString(), beforeValidation)

        @JvmOverloads
        @JvmName("requestString")
        fun `java-requestString`(beforeValidation: Consumer<StatusCodeHandler<String>> = Consumer { }) =
            requestString(beforeValidation.kotlin)
        @JvmName("ktRequestLines")
        fun requestLines(beforeValidation: Initializer<StatusCodeHandler<Stream<String>>> = {}): List<String>? =
            sendInternal("*/*", HttpResponse.BodyHandlers.ofLines(), beforeValidation)?.toList()
        @JvmOverloads
        @JvmName("requestLines")
        fun `java-requestLines`(beforeValidation: Consumer<StatusCodeHandler<Stream<String>>> = Consumer { }): List<String>? =
            requestLines(beforeValidation.kotlin)
        @JvmOverloads
        @JvmName("requestJson")
        fun <T> `java-requestJson`(cls: Class<T>, beforeValidation: Consumer<StatusCodeHandler<InputStream>> = Consumer { }): T? {
            return gson.fromJson(InputStreamReader(
                sendInternal("application/json", HttpResponse.BodyHandlers.ofInputStream(), beforeValidation.kotlin) ?: return null
            ), cls)
        }

        @JvmName("ktRequestJson")
        fun requestJson(beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}) =
            `java-requestJson`(JsonObject::class.java, beforeValidation)

        @JvmName("requestJson")
        fun `java-requestJson`(beforeValidation: Consumer<StatusCodeHandler<InputStream>> = Consumer {}) =
            requestJson(beforeValidation.kotlin)
        inline fun<reified T> requestJson(noinline beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}): T? =
            `java-requestJson`(T::class.java, beforeValidation)


        // Kotlin requesting via coroutines
        fun sendAsync() { scope.launch { send() } }
        @JvmName("ktSendAsync")
        fun sendAsync(beforeValidation: Initializer<StatusCodeHandler<*>> = {}) = scope.launch { send(beforeValidation) }

        @JvmName("sendAsync")
        fun `java-sendAsync`(beforeValidation: Consumer<StatusCodeHandler<*>>) = sendAsync(beforeValidation.kotlin)

        @JvmName("ktRequestInputStreamAsync")
        fun requestInputStreamAsync(beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}) =
            scope.async { requestInputStream(beforeValidation) }

        @JvmName("ktRequestFutureInputStream")
        fun requestFutureInputStream(beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}) =
            requestInputStreamAsync(beforeValidation).asCompletableFuture()

        @JvmName("ktRequestStringAsync")
        fun requestStringAsync(beforeValidation: Initializer<StatusCodeHandler<String>> = {}) =
            scope.async { requestString(beforeValidation) }

        @JvmName("ktRequestFutureString")
        fun requestFutureString(beforeValidation: Initializer<StatusCodeHandler<String>> = {}) =
            requestStringAsync(beforeValidation).asCompletableFuture()

        @JvmName("ktRequestLinesAsync")
        fun requestLinesAsync(beforeValidation: Initializer<StatusCodeHandler<Stream<String>>> = {}) =
            scope.async { requestLines(beforeValidation) }

        @JvmName("ktRequestFutureLines")
        fun requestFutureLines(beforeValidation: Initializer<StatusCodeHandler<Stream<String>>> = {}) =
            requestLinesAsync(beforeValidation).asCompletableFuture()

        @JvmName("ktRequestJsonAsync")
        fun requestJsonAsync(beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}) =
            scope.async { requestJson(beforeValidation) }

        @JvmName("ktRequestFutureJson")
        fun requestFutureJson(beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}) =
            requestJsonAsync(beforeValidation).asCompletableFuture()


        inline fun<reified T> requestJsonAsync(noinline beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}) =
            scope.async { requestJson<T>(beforeValidation) }

        inline fun<reified T> requestFutureJson(noinline beforeValidation: Initializer<StatusCodeHandler<InputStream>> = {}) =
            requestJsonAsync<T>(beforeValidation).asCompletableFuture()



        // Java-callable coroutine usage with via callbacks
        @JvmOverloads
        @JvmName("requestInputStreamAsync")
        fun `java-requestInputStreamAsync`(callback: Consumer<InputStream?>, beforeValidation: Consumer<StatusCodeHandler<InputStream>> = Consumer {}) {
            scope.launch { requestInputStreamAsync(beforeValidation.kotlin) thenAccept callback.kotlin }
        }

        @JvmOverloads
        @JvmName("requestStringAsync")
        fun `java-requestStringAsync`(callback: Consumer<String?>, beforeValidation: Consumer<StatusCodeHandler<String>> = Consumer {}) {
            scope.launch { requestStringAsync(beforeValidation.kotlin) thenAccept callback.kotlin }
        }

        @JvmOverloads
        @JvmName("requestLinesAsync")
        fun `java-requestLinesAsync`(callback: Consumer<List<String>?>, beforeValidation: Consumer<StatusCodeHandler<Stream<String>>> = Consumer { }) {
            scope.launch { requestLinesAsync(beforeValidation.kotlin) thenAccept callback.kotlin }
        }

        @JvmOverloads
        @JvmName("requestJsonAsync")
        fun `java-requestJsonAsync`(callback: Consumer<JsonObject?>, beforeValidation: Consumer<StatusCodeHandler<InputStream>> = Consumer { }) {
            scope.launch { requestJsonAsync(beforeValidation.kotlin) thenAccept callback.kotlin }
        }

        @JvmOverloads
        @JvmName("requestJsonAsync")
        fun <T> `java-requestJsonAsync`(cls: Class<T>, callback: Consumer<T?>, beforeValidation: Consumer<StatusCodeHandler<InputStream>> = Consumer {  }) {
            scope.launch {
                async {
                    `java-requestJson`(cls, beforeValidation.kotlin)
                } thenAccept callback.kotlin
            }
        }


        /**
         * Internal function for synchronously sending the current [HTTP].[Request]
         * Returns null when some error occurred during sending, or when the response from the server isn't exactly HTTP 200.
         * Non-200 statuses can be handled via the [beforeValidation] callback.
         */
        private fun<T> sendInternal(accept: String, bodyHandler: HttpResponse.BodyHandler<T>, beforeValidation: Initializer<StatusCodeHandler<T>> = {}): T? {
            builder.header("Accept", accept)
            builder.method(httpMethod.name, bodyPublisher)

            return runCatching {
                httpClient.send(builder.build(), bodyHandler)
                    .also {
                        StatusCodeHandler(beforeValidation)
                            .handleResponse(it)
                    }
                    .takeIf {
                        it.statusCode() == 200
                    }
                    ?.body()
            }
                .onFailure {
                    Greteor.logger.error("Error when sending an HTTP request", it)
                }
                .getOrNull()
        }

        private fun bodyInternal(mediaType: String, publisher: HttpRequest.BodyPublisher): Request {
            bodyPublisher = publisher

            setHeaders(
                "Content-Type" to mediaType
            )

            return this
        }

        enum class Method {
            GET,
            POST,
            DELETE,
            PATCH,
            PUT;

            operator fun invoke(url: String) = Request(newRequestBuilder(url), this)

            companion object {
                fun byNameOrNull(httpMethod: String): Method? =
                    Method.entries.firstOrNull { it.name.equals(httpMethod, true) }
            }
        }

        @Suppress("PropertyName", "unused") // HTTP status code DSL
        inner class StatusCodeHandler<T>(
            beforeValidation: Initializer<StatusCodeHandler<T>>,
            private val handlers: MutableMap<Int, Initializer<HttpResponse<T?>>> = mutableMapOf()
        ) {
            init {
                beforeValidation()
            }

            fun handle(code: Int, handler: Initializer<HttpResponse<T?>>) {
                if (code == 200) return //200 is the desired outcome and as such it's not a code you need to handle
                handlers[code] = handler
            }

            operator fun Int.plusAssign(handler: Initializer<HttpResponse<T?>>) = handle(this, handler)

            fun handleResponse(response: HttpResponse<T?>) {
                handlers[response.statusCode()]?.invoke(response)
            }

            // named HTTP status codes

            val Continue = 100
            val SwitchingProtocols = 101
            val Processing = 102
            val EarlyHints = 103

            //OK is not defined as you shouldn't be handling it
            val Created = 201
            val Accepted = 202
            val NonAuthoritativeInformation = 203
            val NoContent = 204
            val ResetContent = 205
            val PartialContent = 206
            val MultiStatus = 207
            val AlreadyReported = 208

            val MultipleChoices = 300
            val MovedPermanently = 301
            val Found = 302
            val SeeOther = 303
            val NotModified = 304
            val UseProxy = 305
            val TemporaryRedirect = 307

            val BadRequest = 400
            val Unauthorized = 401
            val PaymentRequired = 402
            val Forbidden = 403
            val NotFound = 404
            val MethodNotAllowed = 405
            val NotAcceptable = 406
            val ProxyAuthenticationRequired = 407
            val RequestTimeout = 408
            val Conflict = 409
            val Gone = 410
            val LengthRequired = 411
            val PreconditionFailed = 412
            val RequestTooLong = 413
            val RequestUriTooLong = 414
            val UnsupportedMediaType = 415
            val RequestedRangeNotSatisfiable = 416
            val ExpectationFailed = 417
            val ImATeapot = 418
            val InsufficientSpaceOnResource = 419
            val MethodFailure = 420
            val EnhanceYourCalm = 420
            val MisdirectedRequest = 421
            val UnprocessableEntity = 422
            val Locked = 423
            val FailedDependency = 424
            val TooEarly = 425
            val UpgradeRequired = 426
            val PreconditionRequired = 428
            val TooManyRequests = 429
            val RequestHeaderFieldsTooLarge = 431
            val UnavailableForLegalReasons = 451

            val InternalServerError = 500
            val NotImplemented = 501
            val BadGateway = 502
            val ServiceUnavailable = 503
            val GatewayTimeout = 504
            val HttpVersionNotSupported = 505
            val VariantAlsoNegotiates = 506
            val InsufficientStorage = 507
            val LoopDetected = 508
            val NotExtended = 510
            val NetworkAuthenticationRequired = 511
        }
    }
}

private fun newRequestBuilder(url: String): HttpRequest.Builder =
    runCatching(url::asURI)
        .onFailure { Greteor.logger.error("Invalid URI", it) }
        .mapTo {
            HttpRequest.newBuilder(it)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
        }
