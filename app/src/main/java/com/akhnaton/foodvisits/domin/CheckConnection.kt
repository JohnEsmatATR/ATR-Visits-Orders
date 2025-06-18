package com.akhnaton.foodvisits.domin

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.akhnaton.foodvisits.data.db.VisitDatabase
import com.akhnaton.foodvisits.data.db.model.SaveVisitDB
import com.akhnaton.foodvisits.data.interfaces.apis.IVisits
import com.akhnaton.foodvisits.data.model.VisitsPlan
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit
import com.akhnaton.foodvisits.shared.RetrofitClient

class CheckConnection(val context: Context) {
    private val retrofit = RetrofitClient.getInstance(IVisits::class.java)
    private val database = VisitDatabase

    fun checkConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    suspend fun getPlan(version: String, token: String): VisitsPlan {
        if (checkConnection()) {
            val visitPlan = retrofit.getPlan(version, token)
            deletePlanFromDB()
            insertPlanToDB(visitPlan)
        }
        return database.getDatabase(context).visitPlanDao().getPlan()
    }


    suspend fun saveVisit(
        version: String,
        token: String,
        customerPartySiteId: String,
        visitType: String,
        visitTarget: String,
        visitActualTarget: String,
        latitude: String,
        longitude: String,
        deviceType: String,
        zoneFlag: String,
        checkInDate: String,
        dateVisit: String,
        customerType: String,
        orderType: String,
    ) : SaveVisit {
        val saveVisitDB = SaveVisitDB(
            version = version,
            token = token,
            customerPartySiteId = customerPartySiteId,
            visitType = visitType,
            visitarget = visitTarget,
            visitActualTarget = visitActualTarget,
            latitude = latitude,
            longitude = longitude,
            deviceType = deviceType,
            zoneFlag = zoneFlag,
            checkInDate = checkInDate,
            dateVisit = dateVisit,
            customerType = customerType,
            orderType = orderType,
        )
        insertSaveVisitToDB(saveVisitDB)
        return saveVisitOnline()
    }


    suspend fun saveVisitOnline() : SaveVisit {
        var saveVisit: SaveVisit? = null
        if (checkConnection()) {

            val saveVisitDBList: List<SaveVisitDB> = database.getDatabase(context).saveVisitDao().getVisits()

            Log.d("jnjndjnjndjnjnd", "saveVisitOnline: $saveVisitDBList")
            for (saveVisitDB in saveVisitDBList) {
                Log.d("SaveVisitDebug", """
    Sending visit with:
    app_version: ${saveVisitDB.version}
    api_token: ${saveVisitDB.token}
    customer_party_site_id: ${saveVisitDB.customerPartySiteId}
    visit_type: ${saveVisitDB.visitType}
    visit_target: ${saveVisitDB.visitarget}
    visit_actual_target: ${saveVisitDB.visitActualTarget}
    latitude: ${saveVisitDB.latitude}
    longitude: ${saveVisitDB.longitude}
    device_type: ${saveVisitDB.deviceType}
    zone_flag: ${saveVisitDB.zoneFlag}
    check_in_date: ${saveVisitDB.checkInDate}
    date_visit: ${saveVisitDB.dateVisit}
    customer_type: ${saveVisitDB.customerType}
    order_type: ${saveVisitDB.orderType}
""".trimIndent())

                saveVisit = retrofit.saveVisits(
                    saveVisitDB.version,
                    saveVisitDB.token,
                    saveVisitDB.customerPartySiteId,
                    saveVisitDB.visitType,
                    saveVisitDB.visitarget,
                    saveVisitDB.visitActualTarget,
                    saveVisitDB.latitude,
                    saveVisitDB.longitude,
                    saveVisitDB.deviceType,
                    saveVisitDB.zoneFlag,
                    saveVisitDB.checkInDate,
                    saveVisitDB.dateVisit,
                    saveVisitDB.customerType,
                    saveVisitDB.orderType,

                )
            }
        }
        return saveVisit!!

    }

    suspend fun getAppSetting(version: String) = if (checkConnection()) {
        retrofit.getAppSetting(version)
    } else {
        null
    }

    suspend fun deletePlanFromDB() {
        database.getDatabase(context).visitPlanDao().deletePlan()
    }

    suspend fun insertPlanToDB(visitsPlan: VisitsPlan) {
        database.getDatabase(context).visitPlanDao().insert(visitsPlan)
    }

    suspend fun deleteSaveVisitFromDB() {
        database.getDatabase(context).saveVisitDao().deleteVisit()
    }

    suspend fun insertSaveVisitToDB(visitsPlanDB: SaveVisitDB) {
        database.getDatabase(context).saveVisitDao().insert(visitsPlanDB)
    }
}