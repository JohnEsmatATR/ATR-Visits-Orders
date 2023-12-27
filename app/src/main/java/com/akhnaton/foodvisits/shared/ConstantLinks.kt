package com.akhnaton.foodvisits.shared

object ConstantLinks {

    const val BASE_URL = "https://sales.atr-eg.com/"
    private const val ROUTS_API = "web_food_visit/routes.php"
    const val APP_SETTING = "$ROUTS_API?path=get_app_settings"
    const val LOGIN_PATH = "$ROUTS_API?path=login"
    const val CHART_PATH = "$ROUTS_API?path=user_chart_info"
    const val TICKET_SYSTEM = "$ROUTS_API?path=create_new_ticket"

    const val FOOD_ORDER = "$ROUTS_API?path=get_food_invoices_list"
    const val FOOD_ORDER_DETAILS = "$ROUTS_API?path=get_food_invoice_details"

    const val VISITS_PATH = "$ROUTS_API?path=get_user_order_type"
    const val VISIT_PLAN = "$ROUTS_API?path=get_visit_plan"
    const val CUSTOMER_TYPE = "$ROUTS_API?path=get_user_customer_type"
    const val LINES = "$ROUTS_API?path=get_user_lines"
    const val CUSTOMER_LINE = "$ROUTS_API?path=get_main_customers_line"
    const val CUSTOMERS_SITE = "$ROUTS_API?path=get_customers_sites_by_customer_code"
    const val SAVE_VISIT = "$ROUTS_API?path=save_visit"
    const val CUSTOMER_PAYMENT = "$ROUTS_API?path=get_customer_payment_terms"
    const val GENERATE_ORDER_NUMBER =
        "$ROUTS_API?path=generate_and_validate_user_customer_before_crate_order"
    const val GET_CATEGORIES = "$ROUTS_API?path=get_categories_based_on_order_type"
        const val GET_PRODUCT = "$ROUTS_API?path=get_products_based_on_sub_categories"
    const val SEND_ORDER = "$ROUTS_API?path=create_new_order"
    const val SAVE_ORDER_PENDING = "$ROUTS_API?path=save_order"
    const val SAVED_ORDER = "$ROUTS_API?path=get_saved_order_products"

    // Add New Customer
    const val ADD_CUSTOMER = "$ROUTS_API?path=create_new_customer"

    //Order History
    const val ORDER_HISTORY = "$ROUTS_API?path=get_orders_history"
    const val ORDER_HISTORY_DETAILS = "$ROUTS_API?path=get_orders_history_details"
    const val DELIVERY_PRINT = "$ROUTS_API?path=print_food_invoice"

    // Promoters
    const val PROMOTER_GET_ITEMS = "$ROUTS_API?path=get_item_data"
    const val PROMOTER_SUBMIT_ITEMS = "$ROUTS_API?path=stock_item"
    const val PROMOTER_INSERT_DETAILS = "$ROUTS_API?path=insertDayDetailsPerClient"
    const val PROMOTER_SEND_COMPETITORS = "$ROUTS_API?path=send_competitors"
    const val PROMOTER_UPLOAD_IMAGE = "$ROUTS_API?path=upload_image"
    const val PROMOTER_COMPETITOR_LIST = "$ROUTS_API?path=get_competitor_list"

    // supervisor
    const val SUPER_ORDER_LIST = "$ROUTS_API?path=super_order_list"
    const val SUPER_ORDER_REJECT = "$ROUTS_API?path=super_order_reject"
    const val SUPER_ORDER_DETAILS = "$ROUTS_API?path=super_order_details"
    const val SUPER_CHECK_CREDIT_LIMIT = "$ROUTS_API?path=super_check_credit_limit"
    const val SUPER_GET_CREDIT_LIMIT_DETAILS = "$ROUTS_API?path=super_get_credit_limit_details"
    const val SUPER_SEND_CREDIT_LIMIT_DETAILS = "$ROUTS_API?path=super_send_credit_limit_details"
    const val SUPER_CHECK_QOUTA = "$ROUTS_API?path=super_approve_qouta"
    const val SUPER_APPROVE_ORDER = "$ROUTS_API?path=super_approve_order"

}