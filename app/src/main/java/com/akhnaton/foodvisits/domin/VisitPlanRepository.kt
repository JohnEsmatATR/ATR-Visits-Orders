package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IVisitPlan
import com.akhnaton.foodvisits.data.model.pendingVisits.ApproveVisitsReq
import com.akhnaton.foodvisits.data.model.pendingVisits.PendingVisitsRes
import com.akhnaton.foodvisits.data.model.visitPlan.VisitData
import com.akhnaton.foodvisits.data.model.visitPlan.VisitListModel
import com.akhnaton.foodvisits.data.offline.OfflineDatabase
import com.akhnaton.foodvisits.data.offline.dao.PendingVisitsCacheDao
import com.akhnaton.foodvisits.data.offline.dao.VisitCacheDao
import com.akhnaton.foodvisits.data.offline.entity.PendingVisitsCacheEntity
import com.akhnaton.foodvisits.data.offline.entity.toCacheEntity
import com.akhnaton.foodvisits.data.offline.entity.toVisitItem
import com.akhnaton.foodvisits.shared.RetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.IOException

class VisitPlanRepository(
    private val cacheDao: VisitCacheDao? = null,
    private val pendingCacheDao: PendingVisitsCacheDao? = null
) {

    private val retrofit = RetrofitClient.getInstance(IVisitPlan::class.java)

    suspend fun getMonthlyVisits(from: String, to: String): VisitListModel {
        return try {
            val response = retrofit.getMonthlyVisits(from, to)
            if (response.status == 200) saveToCache(response)
            response
        } catch (e: IOException) {
            loadFromCache() ?: throw e
        }
    }

    private suspend fun saveToCache(response: VisitListModel) {
        val dao = cacheDao ?: return
        val userId = OfflineDatabase.currentUserId()
        try {
            val visitData = Gson().fromJson(response.data, VisitData::class.java)
            dao.replaceAll(userId, visitData.visits.map { it.toCacheEntity(userId) })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadFromCache(): VisitListModel? {
        val dao = cacheDao ?: return null
        val cached = dao.getAll(OfflineDatabase.currentUserId())
        if (cached.isEmpty()) return null

        val visitData = VisitData(cached.map { it.toVisitItem() })
        return VisitListModel(
            status = 200,
            message = "",
            type = "offline",
            data = Gson().toJsonTree(visitData)
        )
    }

    suspend fun getPendingVisitsForApproval(
        page: Int,
        pageSize: Int,
        dateFrom: String?,
        dateTo: String?
    ): PendingVisitsRes {
        val cacheKey = pendingCacheKey(page, pageSize, dateFrom, dateTo)
        return try {
            val response = retrofit.getPendingVisitsForApproval(page, pageSize, dateFrom, dateTo)
            if (response.status == 200) savePendingToCache(cacheKey, response)
            response
        } catch (e: IOException) {
            loadPendingFromCache(cacheKey) ?: throw e
        }
    }

    private fun pendingCacheKey(page: Int, pageSize: Int, dateFrom: String?, dateTo: String?) =
        "${OfflineDatabase.currentUserId()}|$page|$pageSize|${dateFrom.orEmpty()}|${dateTo.orEmpty()}"

    private suspend fun savePendingToCache(cacheKey: String, response: PendingVisitsRes) {
        val dao = pendingCacheDao ?: return
        val data = response.data ?: return
        try {
            dao.save(PendingVisitsCacheEntity(cacheKey, Gson().toJson(data)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadPendingFromCache(cacheKey: String): PendingVisitsRes? {
        val dao = pendingCacheDao ?: return null
        return try {
            val json = dao.get(cacheKey) ?: return null
            PendingVisitsRes(
                status = 200,
                message = "",
                type = "offline",
                data = JsonParser.parseString(json)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun approveVisits(ids: List<String>, decision: Int) =
        retrofit.approveVisits(ApproveVisitsReq(ids, decision))

    suspend fun updateVisitDate(id: String, newDate: String) = retrofit.updateVisitDate(id, newDate)

    suspend fun deleteVisitPlan(ids: List<Int>) =
        retrofit.deleteVisitPlan(com.akhnaton.foodvisits.data.model.visitPlan.DeleteVisitReq(ids))

    suspend fun copyPlan(sourceDate: String, targetDate: String) =
        retrofit.copyPlan(com.akhnaton.foodvisits.data.model.visitPlan.CopyPlanReq(sourceDate, targetDate))
}