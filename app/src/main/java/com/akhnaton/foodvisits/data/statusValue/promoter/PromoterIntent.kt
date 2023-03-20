package com.akhnaton.foodvisits.data.statusValue.promoter

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part


sealed class PromoterIntent {


    data class GetItems(
        val items: String,
        val createdBy: String,
        val customerCode: String,
        val partySiteId: String,
    ) : PromoterIntent()

    data class SubmitStock(
        val createdBy: String,
        val partySite: String,
        val customerCode: String,
        val date: String,
        val itemId: String,
        val quantity: String,
        val returnQuantity: String,
        val price: String,
        val funNum: String,
    ) : PromoterIntent()

    data class GetCurrentStockItems(
        val employeeId: String,
        val partySite: String,
        val code: String,
        val creationDate: String,
        val userType: String,
        val funNum: String
    ) : PromoterIntent()

    data class GetPromotersTargetNotes(
        val employeeId: String,
        val userType: String,
        val funNum: String,
    ) : PromoterIntent()

    data class SendDetails(
        val employeeId: String,
        val date: String,
        val partySite: String,
        val code: String,
        val customerAvg: String,
        val customerCall: String,
        val customerPositiveCall: String,
        val customerPurchase: String,
        val userType: String,
        val funNum: String,
    ) : PromoterIntent()

    data class UploadImages(
        val image: Array<MultipartBody.Part?>,
        val created_by: RequestBody,
        val creation_date: RequestBody,
        val customer_code: RequestBody,
        val party_site_id: RequestBody,
        val user_type: RequestBody,
        val funNum: RequestBody,
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
}