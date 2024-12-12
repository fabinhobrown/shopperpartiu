package com.fabio.shopperpartiu.Controller

import com.fabio.shopperpartiu.Model.Motorista
import com.fabio.shopperpartiu.Model.Viagem
import com.fabio.shopperpartiu.Model.ViagemHistorico
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class Utils {

    /**
     Classe com funções úteis para a aplicação para fazer o tratamento de dados
     */

    internal fun parsePolyline(jsonString: String): List<LatLng> {
        val jsonObject = JSONObject(jsonString)
        val routesArray = jsonObject.optJSONObject("routeResponse")?.optJSONArray("routes")

        if (routesArray == null) {
            return emptyList()
        }

        val polylineList = mutableListOf<LatLng>()

        for (i in 0 until routesArray.length()) {
            val route = routesArray.getJSONObject(i)
            val legsArray = route.optJSONArray("legs") ?: continue

            for (j in 0 until legsArray.length()) {
                val leg = legsArray.getJSONObject(j)
                val polylineEncoded = leg.optJSONObject("polyline")?.optString("encodedPolyline")
                polylineEncoded?.let {
                    polylineList.addAll(decodePolyline(it))
                }
            }
        }

        return polylineList
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val polyline = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1F shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1F shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            polyline.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return polyline
    }

    internal fun parseJsonToMotorista(jsonString: String): List<Motorista> {
        val lista = mutableListOf<Motorista>()
        val jsonObject = JSONObject(jsonString)
        val opcoesArray = jsonObject.getJSONArray("options")

        for (i in 0 until opcoesArray.length()) {
            val opcaoJson = opcoesArray.getJSONObject(i)

            val mapReviewTemp = listOf(
                opcaoJson.getJSONObject("review").getInt("rating") to
                opcaoJson.getJSONObject("review").getString("comment")
            )

            val opcao = Motorista(
                idMotorista = opcaoJson.getInt("id"),
                name = opcaoJson.getString("name"),
                description = opcaoJson.getString("description"),
                vehicle = opcaoJson.getString("vehicle"),
                value = opcaoJson.getString("value"),
                review =  mapReviewTemp,
            )
            lista.add(opcao)
        }

        return lista
    }

    internal fun parseInfoViagem(jsonString: String, edtOrigem: TextInputEditText, edtDestino: TextInputEditText): Viagem {
        val jsonObject = JSONObject(jsonString)

        val routesArray = jsonObject.
        optJSONObject("routeResponse")?.
        optJSONArray("routes")?.
        getJSONObject(0)?.
        getJSONObject("localizedValues")

        val infoViagem = Viagem(
            origin = edtOrigem.text.toString(),
            destination = edtDestino.text.toString(),
            durantion = routesArray?.getJSONObject("duration")?.getString("text").toString(),
            distance = routesArray?.getJSONObject("distance")?.getString("text").toString()
        )

        return infoViagem
    }

    internal fun parseJsonToHistorico(jsonString: String, motorista: Motorista): List<ViagemHistorico>{
        val lista = mutableListOf<ViagemHistorico>()
        val jsonObject = JSONObject(jsonString)
        val opcoesArray = jsonObject.getJSONArray("rides")

        for (i in 0 until opcoesArray.length()) {
            val opcaoJson = opcoesArray.getJSONObject(i)
            val opcao = ViagemHistorico(
                id = opcaoJson.getInt("id"),
                date = opcaoJson.getString("date"),
                origin = opcaoJson.getString("origin"),
                destination = opcaoJson.getString("destination"),
                distance = opcaoJson.getDouble("distance"),
                duration = opcaoJson.getString("duration"),
                driver = opcaoJson.getJSONObject("driver")["name"].toString(),
                value = opcaoJson.getInt("value")
            )

            // Compara se o item de retorno da api é igual ao selecionado no spinner
            if(opcaoJson.getJSONObject("driver")["name"] == motorista.name){
                lista.add(opcao)
            }

            // Mas se o item selecionado for TODOS, ai lista todos os motoristas
            if(motorista.name == "TODOS"){
                lista.add(opcao)
            }
        }

        return lista
    }
}