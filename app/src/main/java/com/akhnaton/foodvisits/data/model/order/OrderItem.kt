package com.akhnaton.foodvisits.data.model.order

import android.os.Parcel
import android.os.Parcelable


data class ItemsList(
    var app_version: String,
    var api_token: String,
    var order_type: String,
    var order_number: String,
    var customer_type: String,
    var customer_party_site_id: String,
    var pay_term_id: String,
    var turn_over: Boolean,
    var order_items: MutableList<OrderItem>,
    var return_items: MutableList<ReturnItem>,
    var ordersource_id: Int,
    var priceListIdPosition: Int,
    var priceListDescriptionPosition: String,
)

data class OrderItem(
    var bonus: String,
    var item_id: Int,
    var item_quantity: String,
    var item_price_list: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bonus)
        parcel.writeInt(item_id)
        parcel.writeString(item_quantity)
        parcel.writeInt(item_price_list)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderItem> {
        override fun createFromParcel(parcel: Parcel): OrderItem {
            return OrderItem(parcel)
        }

        override fun newArray(size: Int): Array<OrderItem?> {
            return arrayOfNulls(size)
        }
    }
}

data class ReturnItem(
    var item_id: Int,
    var item_quantity: String
)

data class CardItem(
    var item_id: Int,
    var item_code: Int,
    var item_description: String,
    var item_price: Double,
    var item_tax: Float,
    var quantity: String,
    var total: Float,
    var bonus: String,
    var item_price_list: Int
)