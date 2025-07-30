package com.akhnaton.foodvisits.ui.map

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.model.VisitsPlan
import com.akhnaton.foodvisits.data.statusValue.route.RouteIntent
import com.akhnaton.foodvisits.data.statusValue.route.RouteState
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.visits.VisitsDetailsActivity
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModel
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MapActivity : AppCompatActivity() {
    private lateinit var map: MapView
    private lateinit var btnMyLocation: ImageButton
    private var userMarker: Marker? = null
    private var lastGeoPoint: GeoPoint? = null
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var customerList: List<CustomerVisitPlan> = emptyList()
    private var visitsPlan: VisitsPlan? = null
    private val versionName = BuildConfig.VERSION_NAME
    private val routeViewModel : RouteViewModel  by  lazy {
        RouteViewModelFactory(applicationContext).create(RouteViewModel::class.java)
    }
    private val viewModel: VisitsViewModel by lazy {
        VisitsViewModelFactory(applicationContext).create(VisitsViewModel::class.java)
    }
    private var currentMarker: Marker? = null
    private var currentBottomSheet: BottomSheetDialog? = null

    private var sortedCustomerList: List<CustomerVisitPlan> = emptyList()
    private var nextCustomerIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance()
            .load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        setContentView(R.layout.activity_map)
        val findNextButton = findViewById<Button>(R.id.find_next_customer)
        map = findViewById(R.id.osm_map)
        btnMyLocation = findViewById(R.id.btn_my_location)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        observeRoute()
        btnMyLocation.setOnClickListener {
            userLatitude?.let { lat ->
                userLongitude?.let { lng ->
                    val point = GeoPoint(lat, lng)
                    map.controller.animateTo(point)
                }
            }
        }

        findNextButton.setOnClickListener {
            focusNextCustomer()
        }
        checkLocationPermissionAndStart()



    }
    private fun observeRoute(){
        lifecycleScope.launch {
            routeViewModel.state.collect { state ->
                when (state) {
                    is RouteState.Idle -> {

                    }
                    is RouteState.Loading -> {
                        Toast.makeText(this@MapActivity, "جاري تحميل المسار...", Toast.LENGTH_SHORT).show()
                    }
                    is RouteState.Success -> {
                        drawRoutePolyline(state.geoPoints)
                    }
                    is RouteState.Error -> {
                        Toast.makeText(this@MapActivity, "فشل في تحميل المسار: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    private fun drawRoutePolyline(geoPoints: List<GeoPoint>) {
        val roadOverlay = Polyline().apply {
            setPoints(geoPoints)
            color = ContextCompat.getColor(this@MapActivity, R.color.blue)
            width = 15f
            isGeodesic = true
            outlinePaint.strokeJoin = Paint.Join.ROUND
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.isAntiAlias = true
        }

        map.overlays.add(roadOverlay)
        map.invalidate()
    }

    private fun focusNextCustomer() {
        if (sortedCustomerList.isEmpty()) {
            Toast.makeText(this, "لم يتم تحميل العملاء بعد", Toast.LENGTH_SHORT).show()
            return
        }

        if (nextCustomerIndex >= sortedCustomerList.size) {
            Toast.makeText(this, "تم عرض كل العملاء", Toast.LENGTH_SHORT).show()
            nextCustomerIndex = 0
            return
        }

        val customer = sortedCustomerList[nextCustomerIndex]
        val lat = customer.customer_latitude.toDoubleOrNull()
        val lng = customer.customer_longitude.toDoubleOrNull()

        if (lat != null && lng != null) {
            val point = GeoPoint(lat, lng)
            map.controller.animateTo(point)
            map.controller.setZoom(20.0)
            nextCustomerIndex++
        }
    }

    private fun checkLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            startLocationTracking()
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            startLocationTracking()
        } else {
            Log.e("MapActivity", "Location permissions not granted")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationTracking() {
        viewModel.getCurrentLocation()
        lifecycleScope.launch {
            viewModel.locationState.collect { location ->
                if (location != null) {
                    val newLat = location.latitude
                    val newLng = location.longitude
                    val newPoint = GeoPoint(newLat, newLng)
                    userLatitude = newLat
                    userLongitude = newLng
                    Log.d("MapActivity", "Location updated: $newLat, $newLng")

                    if (userMarker == null) {
                        userMarker = Marker(map).apply {
                            position = newPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon =
                                getBitmapFromVectorDrawable(R.drawable.transport_van).toDrawable(
                                    resources
                                )
                            title = "موقعك الحالي"
                        }
                        map.overlays.add(userMarker)
                        map.controller.setZoom(18.0)
                        map.controller.setCenter(newPoint)
                        fetchPlanAndDrawCustomers()
                    } else {
                        animateMarkerSmoothly(lastGeoPoint ?: userMarker!!.position, newPoint)
                    }

                    lastGeoPoint = newPoint
                }
            }
        }
    }

    private fun animateMarkerSmoothly(from: GeoPoint, to: GeoPoint) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000L
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val lat = from.latitude + (to.latitude - from.latitude) * fraction
                val lng = from.longitude + (to.longitude - from.longitude) * fraction
                userMarker?.position = GeoPoint(lat, lng)
                map.invalidate()
            }
        }
        animator.start()
    }


    private fun fetchPlanAndDrawCustomers() {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
            viewModel.statusVisit.collect { status ->
                when (status) {
                    is VisitsStatus.Plan -> {
                        visitsPlan = status.data
                        customerList = status.data.data.customer_visit_plan
                        drawRouteFromUserToCustomers()
                    }

                    is VisitsStatus.Error -> {
                        Log.e("MapActivity", "Error getting visits: ${status.error}")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun drawRouteFromUserToCustomers() {
        val routePoints = mutableListOf<GeoPoint>()

        lastGeoPoint?.let { userPoint ->
            val sortedCustomers = customerList
                .mapNotNull { customer ->
                    val lat = customer.customer_latitude.toDoubleOrNull()
                    val lng = customer.customer_longitude.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        val distance = calculateDistanceInKm(userPoint.latitude, userPoint.longitude, lat, lng)
                        Pair(customer, distance)
                    } else {
                        null
                    }
                }
                .sortedBy { it.second }
                .map { it.first }
            sortedCustomerList = sortedCustomers

            routePoints.add(userPoint)

            var routeIndex = 1
            for (customer in sortedCustomers) {
                val lat = customer.customer_latitude.toDouble()
                val lng = customer.customer_longitude.toDouble()
                val point = GeoPoint(lat, lng)


                val markerIcon = createCustomMarker(customer.customer_name, routeIndex, customer.is_visited_today)


                if (customer.is_visited_today != true) {
                    routePoints.add(point)
                    routeIndex++
                }


                val marker = Marker(map).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = customer.customer_name
                    subDescription = customer.customer_address ?: ""
                    icon = markerIcon

                    setOnMarkerClickListener { m, _ ->
                        if (currentMarker == m && currentBottomSheet?.isShowing == true) {

                            currentBottomSheet?.dismiss()
                            currentMarker = null
                            currentBottomSheet = null
                        } else {

                            showCustomerBottomSheet(customer, m)
                        }
                        true
                    }
                }


                map.overlays.add(marker)
            }


            if (routePoints.size in 2..40) {
                routeViewModel.onIntent(RouteIntent.FetchRoute(routePoints))

            } else {
                Log.d("TAG", "drawRouteFromUserToCustomers: ${routePoints.size} ")
                Toast.makeText(
                    this@MapActivity,
                    "عدد النقاط غير صالح للرسم، يجب أن يكون من 2 إلى 40",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createCustomMarker(name: String, routeNumber: Int, isVisited: Boolean): Drawable {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.marker_customer, null)

        val container = view.findViewById<LinearLayout>(R.id.container)
        val tvRouteNumber = view.findViewById<TextView>(R.id.tvRouteNumber)
        val tvCustomerName = view.findViewById<TextView>(R.id.tvCustomerName)

        tvRouteNumber.text = routeNumber.toString()
        tvCustomerName.text = name


        val backgroundColor = if (isVisited) "#4CAF50" else "#F44336"
        container.background.setTint(Color.parseColor(backgroundColor))


        tvRouteNumber.visibility = if (isVisited) View.INVISIBLE else View.VISIBLE

        tvRouteNumber.setTextColor(Color.WHITE)
        tvCustomerName.setTextColor(Color.WHITE)

        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = createBitmap(view.measuredWidth, view.measuredHeight)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap.toDrawable(resources)
    }

    private fun calculateDistanceInKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }


    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
        val drawable = AppCompatResources.getDrawable(this, drawableId)!!
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun showCustomerBottomSheet(customer: CustomerVisitPlan, marker: Marker) {
        currentBottomSheet?.dismiss()

        val dialogView = layoutInflater.inflate(R.layout.layout_customer_visit_bottom_sheet, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val tvName = dialogView.findViewById<TextView>(R.id.tv_customer_name)
        val tvAddress = dialogView.findViewById<TextView>(R.id.tv_customer_address)
        val btnVisit = dialogView.findViewById<Button>(R.id.btn_visit)

        tvName.text = customer.customer_name
        tvAddress.text = customer.customer_address ?: "بدون عنوان"

        btnVisit.setOnClickListener {
            startVisitDetails(customer)
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            currentMarker = null
            currentBottomSheet = null
        }

        dialog.show()
        currentMarker = marker
        currentBottomSheet = dialog
    }


    private fun startVisitDetails(customer: CustomerVisitPlan) {
        val tsLong = System.currentTimeMillis() / 1000
        val intent = Intent(this@MapActivity, VisitsDetailsActivity::class.java).apply {
            putExtra("customerPartySiteId", customer.customer_party_site_id)
            putExtra("time", tsLong.toString())
            putExtra("customerSiteData", customer)
            putExtra("orderType", customer.customer_order_type)
            putExtra("customerTypePosition", customer.customer_type)
            putExtra("customer_name", customer.customer_name)
            putExtra("customerAddress", customer.customer_address)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
    }
}