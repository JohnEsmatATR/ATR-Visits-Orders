package com.akhnaton.foodvisits.data.model.visits

import java.io.Serializable


data class CustomerSite(
    val status: Int,
    val data: CustomerSiteData
)

data class CustomerSiteData(
    val customer_site: List<SitesData>
)

@kotlinx.serialization.Serializable
class SitesData(
    val customer_party_site_id: String,
    val customer_name: String,
    val customer_latitude: String,
    val customer_longitude: String,
    val customer_addresses: String
) : Serializable