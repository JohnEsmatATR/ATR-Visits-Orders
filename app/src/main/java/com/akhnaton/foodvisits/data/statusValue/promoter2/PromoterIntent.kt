package com.akhnaton.foodvisits.data.statusValue.promoter2

import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSReq
import com.akhnaton.foodvisits.data.model.checkInPhone.CheckInPhoneReq
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import okhttp3.MultipartBody
import okhttp3.RequestBody


sealed class PromoterIntent {

    //Competitors
    data class GetItemSizes(
        val size_name: String
    )

    data class GetCompetitorList(
        val appVersion: Double,
    ) : PromoterIntent()

    data class CheckIn(val checkInGPSReq: CheckInGPSReq) :
        PromoterIntent()

    //Inventory
    data class PromoterGetItemData(
        val customerCode: String,
        val partySiteId: String
    ) : PromoterIntent()

    data class PromoterSaveStock(
        val promoterSaveStockReq: PromoterSaveStockReq
    ) : PromoterIntent()

    data class SendCompetitors(
        val appVersion: RequestBody,
        val apiToken: RequestBody,
        val image: MultipartBody.Part,
        val created_by: RequestBody,
        val creation_date: RequestBody,
        val party_site_id: RequestBody,
        val customer_code: RequestBody,
        val product_id: RequestBody,
        val price: RequestBody,
        val price_after_disc: RequestBody,
        val product_name: RequestBody,
        val weight: RequestBody,
        val discount_rate: RequestBody,
        val prom_type: RequestBody,
        val prom_date: RequestBody,
        val user_type: RequestBody,
        val PromoterCompetitorCompress: RequestBody,
        val competitor_id: RequestBody,
        val type_id: RequestBody,
    ) : PromoterIntent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SendCompetitors

            if (appVersion != other.appVersion) return false
            if (apiToken != other.apiToken) return false
            if (image != other.image) return false
            if (created_by != other.created_by) return false
            if (creation_date != other.creation_date) return false
            if (party_site_id != other.party_site_id) return false
            if (customer_code != other.customer_code) return false
            if (product_id != other.product_id) return false
            if (price != other.price) return false
            if (price_after_disc != other.price_after_disc) return false
            if (product_name != other.product_name) return false
            if (weight != other.weight) return false
            if (discount_rate != other.discount_rate) return false
            if (prom_type != other.prom_type) return false
            if (prom_date != other.prom_date) return false
            if (user_type != other.user_type) return false
            if (PromoterCompetitorCompress != other.PromoterCompetitorCompress) return false
            if (competitor_id != other.competitor_id) return false
            if (type_id != other.type_id) return false

            return true
        }

        override fun hashCode(): Int {
            var result = appVersion.hashCode()
            result = 31 * result + apiToken.hashCode()
            result = 31 * result + image.hashCode()
            result = 31 * result + created_by.hashCode()
            result = 31 * result + creation_date.hashCode()
            result = 31 * result + party_site_id.hashCode()
            result = 31 * result + customer_code.hashCode()
            result = 31 * result + product_id.hashCode()
            result = 31 * result + price.hashCode()
            result = 31 * result + price_after_disc.hashCode()
            result = 31 * result + product_name.hashCode()
            result = 31 * result + weight.hashCode()
            result = 31 * result + discount_rate.hashCode()
            result = 31 * result + prom_type.hashCode()
            result = 31 * result + prom_date.hashCode()
            result = 31 * result + user_type.hashCode()
            result = 31 * result + PromoterCompetitorCompress.hashCode()
            result = 31 * result + competitor_id.hashCode()
            result = 31 * result + type_id.hashCode()
            return result
        }
    }

    //Images
    data class UploadImages(
        val appVersion: RequestBody?,
        val apiToken: RequestBody?,
        val image: Array<MultipartBody.Part?>,
        val created_by: RequestBody?,
        val creation_date: RequestBody?,
        val customer_code: RequestBody?,
        val party_site_id: RequestBody?,
        val user_type: RequestBody?,
        val funNum: RequestBody?,
    ) : PromoterIntent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UploadImages

            if (!image.contentEquals(other.image)) return false
            if (created_by != other.created_by) return false
            if (creation_date != other.creation_date) return false
            if (customer_code != other.customer_code) return false
            if (party_site_id != other.party_site_id) return false
            if (user_type != other.user_type) return false
            if (funNum != other.funNum) return false

            return true
        }

        override fun hashCode(): Int {
            var result = image.contentHashCode()
            result = 31 * result + created_by.hashCode()
            result = 31 * result + creation_date.hashCode()
            result = 31 * result + customer_code.hashCode()
            result = 31 * result + party_site_id.hashCode()
            result = 31 * result + user_type.hashCode()
            result = 31 * result + funNum.hashCode()
            return result
        }
    }

    data class RefreshToken(val userId: String, val token: String) : PromoterIntent()

}