package com.akhnaton.foodvisits.domin.location

import android.util.Log
import com.akhnaton.foodvisits.shared.ConstantLinks
import com.akhnaton.foodvisits.shared.RouteRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import retrofit2.Response

class RouteRepository {

    suspend fun fetchRoute(waypoints: List<GeoPoint>): Result<List<GeoPoint>> = withContext(Dispatchers.IO) {

        val coordinates = JSONArray()
        for (point in waypoints) {
            coordinates.put(JSONArray().apply {
                put(point.longitude)
                put(point.latitude)
            })
        }

        val jsonBody = JSONObject().put("coordinates", coordinates)
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)


        val keys = listOf(ConstantLinks.ROUTE_KEY2, ConstantLinks.ROUTE_KEY)

        for (key in keys) {
            val api = RouteRetrofitClient.create(key)

            try {
                val response: Response<okhttp3.ResponseBody> = api.getRoute(requestBody)

                Log.d("ROUTE_API", "Request URL: ${response.raw().request.url}")
                Log.d("ROUTE_API", "Response Code: ${response.code()}")
                Log.d("ROUTE_API", "Is Successful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val bodyString = response.body()!!.string()
                    Log.d("ROUTE_API", "Response Body: $bodyString")

                    val responseJson = JSONObject(bodyString)
                    val coords = responseJson
                        .getJSONArray("features")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")

                    val geoPoints = ArrayList<GeoPoint>()
                    for (i in 0 until coords.length()) {
                        val point = coords.getJSONArray(i)
                        geoPoints.add(GeoPoint(point.getDouble(1), point.getDouble(0)))
                    }

                    return@withContext Result.success(geoPoints)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ROUTE_API_ERROR", "Error Body: $errorBody")
                }

            } catch (e: Exception) {
                Log.e("ROUTE_API_EXCEPTION", "Exception: ${e.localizedMessage}", e)
                continue
            }
        }

        return@withContext Result.failure(Exception("Failed to fetch route"))
    }
}
