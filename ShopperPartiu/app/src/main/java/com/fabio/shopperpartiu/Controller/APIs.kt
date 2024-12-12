package com.fabio.shopperpartiu.Controller

import android.util.Log
import com.fabio.shopperpartiu.Model.Cliente
import com.fabio.shopperpartiu.Model.Motorista
import com.fabio.shopperpartiu.Model.Viagem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class APIs {

    /**
    Criei dois tipos de funções para consumir API:
    1. Usando a lib OkHttp
    2. Usando a Java.net HTTPURLConnection
    Deixarei usando OkHttp
    */

    companion object {
        private const val URL = "https://xd5zl5kk2yltomvw5fb37y3bm40vsyrx.lambda-url.sa-east-1.on.aws"
        private const val CONNECT_TIMEOUT = 5L  // Timeout de conexão em segundos
        private const val READ_TIMEOUT = 5L     // Timeout de leitura em segundos
        private const val WRITE_TIMEOUT = 5L    // Timeout de gravação em segundos
    }

    fun consultaViagemNativo(customerId: String, origem: String, destino: String): String? {

        // Configuração do OkHttpClient com timeouts
        val client = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS) // Timeout de conexão
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS) // Timeout de leitura
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS) // Timeout de escrita
            .build()

        // Corpo da requisição em formato JSON
        val requestBody = """
        {
            "customer_id": "$customerId",
            "origin": "$origem",
            "destination": "$destino"
        }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        // Construindo a requisição
        val request = Request.Builder()
            .url("$URL/ride/estimate")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        return try {
            // Executar a requisição
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    throw IOException("Erro na requisição: ${response.code}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun registraViagemNativo(motorista: Motorista, viagem: Viagem, cliente: Cliente): String? {

        val client = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS) // Timeout de conexão
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS) // Timeout de leitura
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS) // Timeout de escrita
            .build()

        // Corpo da requisição em formato JSON
        val requestBody = """
            {
                "customer_id": "${cliente.idCliente}",
                "origin": "${viagem.origin}",
                "destination": "${viagem.destination}",
                "distance": {"text" : "${viagem.distance}"},
                "duration": {"text" : "${viagem.durantion}"},
                "driver": {
                    "id": ${motorista.idMotorista},
                    "name": "${motorista.name}"
                },
                "value": ${motorista.value}
            }
            """.trimIndent().toRequestBody("application/json".toMediaType())

        // Construção da requisição
        val request = Request.Builder()
            .url("$URL/ride/confirm")
            .patch(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        return try {
            // Executar a requisição
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    throw IOException("Erro na requisição: ${response.code}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun consultaHistoricoViagemNativo(cliente: String, motorista: Motorista): String? {

        val client = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS) // Timeout de conexão
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS) // Timeout de leitura
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS) // Timeout de escrita
            .build()

        // Constrói a URL com os parâmetros
        val url = "$URL/ride/$cliente?driver_id=${motorista.idMotorista}"

        // Constrói a requisição
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            // Executar a requisição
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    throw IOException("Erro na requisição: ${response.code}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun consultaViagemNativo_HTTP(customerId: String, origem: String, destino: String): String? {
        val url = URL("$URL/ride/estimate")
        val requestBody = """
        {
            "customer_id": "$customerId",
            "origin": "$origem",
            "destination": "$destino"
        }
    """.trimIndent()

        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
    }

    fun registraViagemNativo_HTTP(motorista: Motorista, viagem: Viagem, cliente: Cliente): String? {

        val url = URL("$URL/ride/confirm")
        val requestBody = """
        {
        "customer_id": "${cliente.idCliente}",
        "origin": "${viagem.origin}",
        "destination": "${viagem.destination}",
        "distance": {"text" : "${viagem.distance}"},
        "duration": {"text" : "${viagem.durantion}"},
        "driver": {
                "id": ${motorista.idMotorista},
                "name": "${motorista.name}"
            },
        "value": ${motorista.value}
        }
    """.trimIndent()

        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.apply {
                requestMethod = "PATCH"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            return response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }

        return null
    }

    fun consultaHistoricoViagemNativo_HTTP(cliente: String, motorista: Motorista): String? {

        val url = "$URL/ride/${cliente}?driver_id=${motorista.idMotorista}"

        try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
            }

            try {
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    return response
                } else {
                    Log.e("API_ERROR", "Erro na requisição: ${connection.responseCode}")
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e("API_EXCEPTION", "Exceção ao consumir API: ${e.message}")
        }
        return null
    }
}