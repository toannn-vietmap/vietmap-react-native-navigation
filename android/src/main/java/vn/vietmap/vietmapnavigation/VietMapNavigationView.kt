package vn.vietmap.vietmapnavigation

//import vn.vietmap.bindgen.Expected
//import vn.vietmap.vietmapsdk.maps.EdgeInsets
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.component1
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import vn.vietmap.utilities.VietMapEvent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMisc
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import vn.vietmap.android.gestures.MoveGestureDetector
import vn.vietmap.model.CurrentCenterPoint
import vn.vietmap.model.VietMapEvents
import vn.vietmap.model.VietMapLocation
import vn.vietmap.model.VietMapRouteProgressEvent
import vn.vietmap.services.android.navigation.ui.v5.ThemeSwitcher
import vn.vietmap.services.android.navigation.ui.v5.camera.CameraOverviewCancelableCallback
import vn.vietmap.services.android.navigation.ui.v5.listeners.BannerInstructionsListener
import vn.vietmap.services.android.navigation.ui.v5.listeners.NavigationListener
import vn.vietmap.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener
import vn.vietmap.services.android.navigation.ui.v5.route.NavigationMapRoute
import vn.vietmap.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechAnnouncement
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayer
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayerProvider
import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider
import vn.vietmap.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine
import vn.vietmap.services.android.navigation.v5.milestone.Milestone
import vn.vietmap.services.android.navigation.v5.milestone.MilestoneEventListener
import vn.vietmap.services.android.navigation.v5.milestone.VoiceInstructionMilestone
import vn.vietmap.services.android.navigation.v5.navigation.NavigationConstants
import vn.vietmap.services.android.navigation.v5.navigation.NavigationEventListener
import vn.vietmap.services.android.navigation.v5.navigation.NavigationRoute
import vn.vietmap.services.android.navigation.v5.navigation.NavigationTimeFormat
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigation
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigationOptions
import vn.vietmap.services.android.navigation.v5.navigation.camera.RouteInformation
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteListener
import vn.vietmap.services.android.navigation.v5.route.FasterRouteListener
import vn.vietmap.services.android.navigation.v5.route.RouteListener
import vn.vietmap.services.android.navigation.v5.routeprogress.ProgressChangeListener
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress
import vn.vietmap.services.android.navigation.v5.snap.SnapToRoute
import vn.vietmap.services.android.navigation.v5.utils.RouteUtils
import vn.vietmap.utilities.JSONConverter
import vn.vietmap.vietmapnavigation.databinding.NavigationViewBinding
import vn.vietmap.vietmapsdk.Vietmap
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.camera.CameraUpdate
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.newLatLngBounds
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.geometry.LatLngBounds
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions
import vn.vietmap.vietmapsdk.location.LocationComponentOptions
import vn.vietmap.vietmapsdk.location.engine.LocationEngine
import vn.vietmap.vietmapsdk.location.modes.CameraMode
import vn.vietmap.vietmapsdk.location.modes.RenderMode
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.style.layers.LineLayer
import vn.vietmap.vietmapsdk.style.layers.Property.LINE_CAP_ROUND
import vn.vietmap.vietmapsdk.style.layers.Property.LINE_JOIN_ROUND
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory.iconAllowOverlap
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory.iconIgnorePlacement
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory.iconImage
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory.lineCap
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory.lineColor
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory.lineJoin
import vn.vietmap.vietmapsdk.style.layers.PropertyFactory.lineWidth
import vn.vietmap.vietmapsdk.style.layers.SymbolLayer
import vn.vietmap.vietmapsdk.style.sources.GeoJsonSource
import com.vietmap.trackingsdk.VietmapTrackingSDK
import com.vietmap.trackingsdk.VietmapSpeedAlertManager
import com.vietmap.trackingsdk.TrackingConfig
import com.vietmap.trackingsdk.VMLocation
import com.vietmap.trackingsdk.RouteData
import com.vietmap.trackingsdk.VehicleType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class VietMapNavigationView(
    private val context: ThemedReactContext,
) :
    FrameLayout(context), ProgressChangeListener,
    OffRouteListener, MilestoneEventListener, NavigationEventListener, NavigationListener,
    FasterRouteListener, SpeechAnnouncementListener, BannerInstructionsListener, RouteListener,
    VietMapGL.OnMapClickListener,
    MapView.OnDidFinishRenderingMapListener, VietMapGL.OnMapLongClickListener {
    private var currentRoute: DirectionsRoute? = null
    private var routeClicked: Boolean = false
    private var locationEngine: LocationEngine? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var directionsRoutes: List<DirectionsRoute>? = null
    private var distanceToOffRoute = 30 //distance in meter
    private val navigationOptions =
        VietmapNavigationOptions.builder().maxTurnCompletionOffset(30.0).maneuverZoneRadius(40.0)
            .maximumDistanceOffRoute(50.0).deadReckoningTimeInterval(5.0)
            .maxManipulatedCourseAngle(25.0).userLocationSnapDistance(20.0).secondsBeforeReroute(3)
            .enableOffRouteDetection(true).enableFasterRouteDetection(false).snapToRoute(false)
            .manuallyEndNavigationUponCompletion(false).defaultMilestonesEnabled(true)
            .minimumDistanceBeforeRerouting(10.0).metersRemainingTillArrival(20.0)
            .isFromNavigationUi(false).isDebugLoggingEnabled(false)
            .roundingIncrement(NavigationConstants.ROUNDING_INCREMENT_FIFTY)
            .timeFormatType(NavigationTimeFormat.NONE_SPECIFIED)
            .locationAcceptableAccuracyInMetersThreshold(100).build()
    private var navigation: VietmapNavigation? = null
    private var onMapReady = false
    private var isDisposed = false
    private var isRefreshing = false
    private var isBuildingRoute = false
    private var isNavigationInProgress = false
    private var isNavigationCanceled = false
    private var isOverviewing = false
    private var isNextTurnHandling = false
    private var routeUtils = RouteUtils()
    private val snapEngine = SnapToRoute()
    private var apikey: String? = null
    private var apiKeyAlert: String? = null
    private var apiIDAlert: String? = null
    
    // Vehicle configuration variables
    private var pendingVehicleId: String? = null
    private var pendingVehicleType: Int? = null
    private var pendingVehicleSeats: Int? = null
    private var pendingVehicleWeight: Double? = null
    
    private var baseUrl: String = "https://maps.vietmap.vn/api/navigations/route/"
    private var speechPlayer: SpeechPlayer? = null
    private var routeProgress: RouteProgress? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var primaryRouteIndex = 0
    private var padding: IntArray = intArrayOf(50, 50, 50, 50)
    private var profile: String = "driving-traffic"

    private var navigationZoomLevel = 18.0
    private var bearing = 0.0
    private var tilt = 0.0
    private var currentCenterPoint: CurrentCenterPoint? = null

    private var shouldSimulateRoute = false
    private var arrivalIndex = 0

    /**
     * Bindings to the example layout.
     */
    private var binding: NavigationViewBinding =
        NavigationViewBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var vietMapGL: VietMapGL
    private lateinit var vietmapSDK: VietmapTrackingSDK

    private var restartAlert = false


    override fun onAttachedToWindow() {
        Vietmap.getInstance(context)
        super.onAttachedToWindow()
        onCreate()
    }

    override fun requestLayout() {
        super.requestLayout()

        post(measureAndLayout)
    }

    private val measureAndLayout = Runnable {
        measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        layout(left, top, right, bottom)
    }

    companion object {
        private const val TAG = "VietMapNavigationView"
        var instance: VietMapNavigationView? = null

        //Config
        var initialLatitude: Double? = null
        var initialLongitude: Double? = null
        var mapZoomLevel: Double = 15.0
        var mapStyleURL: String? =
            "https://maps.vietmap.vn/api/maps/light/styles.json?apikey=YOUR_API_KEY_HERE"

        var distanceRemaining: Double? = null
        var durationRemaining: Double? = null
        var voiceInstructionsEnabled = true
        var bannerInstructionsEnabled = true
        var longPressDestinationEnabled = true
        var animateBuildRoute = true

        //        var originPoint: Point? = null
//        var destinationPoint: Point? = null
        var listNavigationRemainingPoints: MutableList<Point> = mutableListOf()
        var isRunning: Boolean = false
    }

    @SuppressLint("MissingPermission")
    fun onCreate() {

        fusedLocationClient = context.currentActivity?.let {
            LocationServices.getFusedLocationProviderClient(
                it
            )
        }
        locationEngine = if (shouldSimulateRoute) {
            ReplayRouteLocationEngine()

        } else {
            LocationEngineProvider.getBestLocationEngine(context)
        }

        context.currentActivity?.let {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState =
                    ContextCompat.checkSelfPermission(it, Manifest.permission.POST_NOTIFICATIONS)

                if (permissionState == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                }
            }
        }

        binding.mapView.getMapAsync {
            this.onMapReady = true
            this.vietMapGL = it
//            context.currentActivity?.runOnUiThread {
//                this.vietMapGL.locationComponent.cameraMode = CameraMode.TRACKING_GPS
//
//            }
            if (shouldSimulateRoute) {
                locationEngine = ReplayRouteLocationEngine()
            }

            vietMapGL.setStyle(mapStyleURL) { style ->
                addDestinationIconSymbolLayer(style)
                val routeLineLayer = LineLayer("line-layer-id", "source-id")
                routeLineLayer.setProperties(
                    lineWidth(9f),
                    lineColor(Color.RED),
                    lineCap(LINE_CAP_ROUND),
                    lineJoin(LINE_JOIN_ROUND)
                )
                style.addLayer(routeLineLayer)
                vietMapGL.addOnMoveListener(object : VietMapGL.OnMoveListener {
                    override fun onMoveBegin(moveGestureDetector: MoveGestureDetector) {
                        isOverviewing = true
                        sendEvent(VietMapEvents.ON_MAP_MOVE)
                    }

                    override fun onMove(moveGestureDetector: MoveGestureDetector) {}
                    override fun onMoveEnd(moveGestureDetector: MoveGestureDetector) {
                        sendEvent(VietMapEvents.ON_MAP_MOVE_END)
                    }
                })
                enableLocationComponent(style)
                initMapRoute()
                configSpeechPlayer()
            }

            if (longPressDestinationEnabled) vietMapGL.addOnMapLongClickListener(this)



            if (initialLatitude != null && initialLongitude != null) {
                moveCamera(
                    LatLng(initialLatitude!!, initialLongitude!!),
                    null,
                    zoomLevel = mapZoomLevel
                )

            }
        }
        
        // Initialize SDK sau khi map ready
        initializeSDK()
    }

    private fun initializeSDK() {
        // Initialize SDK
        vietmapSDK = VietmapTrackingSDK.getInstance(context)
        vietmapSDK.initialize("VIETMAP_ALERT")
        
        // Gọi configureAlertAPI nếu apiKeyAlert và apiIDAlert đã được set trước đó
        configureAlertAPI()
        
        // Gọi configureVehicle nếu vehicle info đã được set trước đó
        configureVehicle()
    }

    private fun startRoute(points: ReadableArray, profile: String) {
        // Create a list of coordinates that includes origin, destination, and waypoints
        val coordinatesList = mutableListOf<Point>()
//        coordinatesList.add(
//            Point.fromLngLat(
//                points.getMap(0).getDouble("long"),
//                points.getMap(0).getDouble("lat")
//            )
//        )
//        coordinatesList.add(
//            Point.fromLngLat(
//                points.getMap(1).getDouble("long"),
//                points.getMap(1).getDouble("lat")
//            )
//        )
        for (i in 0 until points.size()) {
            coordinatesList.add(
                Point.fromLngLat(
                    points.getMap(i)?.getDouble("long") ?: 0.0,
                    points.getMap(i)?.getDouble("lat") ?: 0.0
                )
            )
        }

        listNavigationRemainingPoints = coordinatesList
        findRoute(coordinatesList, profile)
    }

    private fun onDestroy() {
//        VietMapNavigationProvider.destroy()
//        mapboxReplayer.finish()
//        maneuverApi.cancel()
//        routeLineApi.cancel()
//        routeLineView.cancel()
//        speechApi.cancel()
//        voiceInstructionsPlayer.shutdown()
    }

    private fun findRoute(coordinates: List<Point>, profile: String?) {
        try {
            val br = bearing
            Log.d("VietMapNavigation", "getroute")
            sendEvent(VietMapEvents.ROUTE_BUILDING)
            val build = NavigationRoute.builder(context)
                .baseUrl(baseUrl)
                .apikey(apikey ?: "")

                .origin(coordinates.first(), 60.0, br)

                .destination(coordinates.last())
                .alternatives(true)
                ///driving-traffic
                ///cycling
                ///walking
                ///motorcycle
                .profile(profile ?: "driving-traffic")
            
            for (i in 1 until coordinates.size - 1) {
                build.addWaypoint(coordinates[i])
            }

            val builder = build.build()

            listNavigationRemainingPoints.removeAt(0)
            builder.getRoute(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse?>, response: Response<DirectionsResponse?>,
                ) {
                    if (response.body() == null || response.body()!!.routes().size < 1) {
                        sendEvent(VietMapEvents.ROUTE_BUILD_FAILED)
                        return
                    }
                    directionsRoutes = response.body()!!.routes()
                    currentRoute = if (directionsRoutes!!.size <= primaryRouteIndex) {
                        directionsRoutes!![0]
                    } else {
                        directionsRoutes!![primaryRouteIndex]
                    }

                    sendEvent(
                        VietMapEvents.ROUTE_BUILT,
                        currentRoute?.toJson()?.let { JSONObject(it) })

                    // Draw the route on the map
                    if (navigationMapRoute != null) {
                        navigationMapRoute?.removeRoute()
                    } else {
                        val routeStyleRes = ThemeSwitcher.retrieveNavigationViewStyle(
                            binding.mapView!!.context,
                            vn.vietmap.services.android.navigation.R.style.NavigationMapRoute
                        )
                        
                        navigationMapRoute =
                            NavigationMapRoute(
                                navigation,
                                binding.mapView!!,
                                vietMapGL!!,
                                routeStyleRes,
                                "vmadmin_province"
                            )
                    }

                    //show multiple route to map
                    if (response.body()!!.routes().size > 1) {
                        navigationMapRoute?.addRoutes(directionsRoutes!!)
                    } else {
                        navigationMapRoute?.addRoute(currentRoute)
                    }


                    isBuildingRoute = false
                    // get route point from current route
                    val routePoints: List<Point> =
                        currentRoute?.routeOptions()?.coordinates() as List<Point>
                    animateVietmapGLForRouteOverview(padding, routePoints)
                    //Start Navigation again from new Point, if it was already in Progress
//                    if (isNavigationInProgress || isStartNavigation) {
//                        startNavigation()
//                    }
                }

                override fun onFailure(call: Call<DirectionsResponse?>, throwable: Throwable) {
                    isBuildingRoute = false
                    sendEvent(
                        VietMapEvents.ROUTE_BUILD_FAILED
                    )

                    //TODO: Handle send event here
                }
            })
        } catch (ex: Exception) {
            sendErrorToReact(ex.toString())
        }

    }

    private fun sendErrorToReact(error: String?) {
        val event = Arguments.createMap()
        event.putString("error", error)
        val surfaceId = UIManagerHelper.getSurfaceId(this)
        UIManagerHelper.getEventDispatcherForReactTag(context, id)
            ?.dispatchEvent(VietMapEvent(surfaceId, id, "onError", event))
    }

    fun onDropViewInstance() {
        this.onDestroy()
    }

    fun setApiKey(apiKey: String) {
        this.apikey = apiKey
        mapStyleURL = "https://maps.vietmap.vn/api/maps/light/styles.json?apikey=$apiKey"
    }

    fun setApiKeyAlert(apiKeyAlert: String) {
        this.apiKeyAlert = apiKeyAlert
        configureAlertAPI()
    }

    fun setApiIDAlert(apiIDAlert: String) {
        this.apiIDAlert = apiIDAlert
        configureAlertAPI()
    }

    private fun configureAlertAPI() {        
        if (::vietmapSDK.isInitialized && apiKeyAlert != null && apiIDAlert != null) {
            try {
                vietmapSDK.configureAlertAPI(apiKeyAlert!!, apiIDAlert!!)
            } catch (e: Exception) {
                Log.e("VietmapSpeedAlert", "Error configuring Alert API", e)
            }
        } else {
            Log.d("VietmapSpeedAlert", "Cannot configure Alert API - SDK not initialized or missing credentials")
        }
    }

    private fun configureVehicle() {
        if (::vietmapSDK.isInitialized && 
            pendingVehicleId != null && 
            pendingVehicleType != null && 
            pendingVehicleSeats != null && 
            pendingVehicleWeight != null) {
            try {
                val vehicleTypeEnum = VehicleType.fromValue(pendingVehicleType!!)
                vietmapSDK.configureVehicle(pendingVehicleId!!, vehicleTypeEnum, pendingVehicleSeats!!, pendingVehicleWeight!!)
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring vehicle", e)
            }
        } else {
            Log.d(TAG, "Cannot configure vehicle - SDK not initialized or missing vehicle data")
        }
    }

    fun setMute(mute: Boolean) {
//        this.padding = padding
    }

    fun setInitialLatLngZoom(initialLatLng: ReadableMap) {
        initialLatitude = initialLatLng.getDouble("lat")
        initialLongitude = initialLatLng.getDouble("lng")
        mapZoomLevel = initialLatLng.getDouble("zoom")

    }

    fun setNavigationZoomLevel(zoomLevel: Double) {
        this.navigationZoomLevel = zoomLevel
    }

    fun setNavigationTiltAnchor(tiltAnchor: Double) {
        this.tilt = tiltAnchor
    }

    fun setNavigationPadding(padding: ReadableMap) {
        this.padding =
            intArrayOf(
                padding.getInt("left"),
                padding.getInt("top"),
                padding.getInt("right"),
                padding.getInt("bottom")
            )
    }

    fun setShouldSimulateRoute(shouldSimulateRoute: Boolean) {
        this.shouldSimulateRoute = shouldSimulateRoute
    }


//    override fun onMapReady(p0: VietMapGL) {
//        this.onMapReady = true
//        this.vietMapGL = p0
//        if (shouldSimulateRoute) {
//            locationEngine = ReplayRouteLocationEngine()
//        }
//
//        vietMapGL.setStyle(mapStyleURL) { style ->
//            addDestinationIconSymbolLayer(style)
//            val routeLineLayer = LineLayer("line-layer-id", "source-id")
//            routeLineLayer.setProperties(
//                lineWidth(9f),
//                lineColor(Color.RED),
//                lineCap(LINE_CAP_ROUND),
//                lineJoin(LINE_JOIN_ROUND)
//            )
//            style.addLayer(routeLineLayer)
//            vietMapGL.addOnMoveListener(object : VietMapGL.OnMoveListener {
//                override fun onMoveBegin(moveGestureDetector: MoveGestureDetector) {
//                    isOverviewing = true
//                    sendEvent(VietMapEvents.ON_MAP_MOVE)
//                    // TODO: handle send event here
//                }
//
//                override fun onMove(moveGestureDetector: MoveGestureDetector) {}
//                override fun onMoveEnd(moveGestureDetector: MoveGestureDetector) {
//                    sendEvent(VietMapEvents.ON_MAP_MOVE_END)
//                    // TODO: handle send event here
//                }
//            })
//
//
//
//            enableLocationComponent(style)
//            initMapRoute()
//        }
//
//        if (longPressDestinationEnabled) vietMapGL.addOnMapLongClickListener(this)
//
//
//
//        if (initialLatitude != null && initialLongitude != null) {
//            // println("MoveCamera5")
//
//            moveCamera(
//
//                LatLng(
//                    initialLatitude!!,
//                    initialLongitude!!
//                ), null, 3.0
//            )
//        }
//        sendEvent(VietMapEvents.MAP_READY)
//    }

    private fun initMapRoute() {
        val routeStyleRes = ThemeSwitcher.retrieveNavigationViewStyle(
            binding.mapView!!.context,
            vn.vietmap.services.android.navigation.R.style.NavigationMapRoute
        )
        
        navigationMapRoute =
            NavigationMapRoute(
                navigation,
                binding.mapView!!,
                vietMapGL!!,
                routeStyleRes,
                "vmadmin_province"
            )

        navigationMapRoute?.setOnRouteSelectionChangeListener {
            routeClicked = true

            currentRoute = it

            val routePoints: List<Point> =
                currentRoute?.routeOptions()?.coordinates() as List<Point>
            animateVietmapGLForRouteOverview(padding, routePoints)
            primaryRouteIndex = try {
                it.routeIndex()?.toInt() ?: 0
            } catch (e: Exception) {
                0
            }
            if (isRunning) {
                finishNavigation(isOffRouted = true)
                startNavigation()
            }
            sendEvent(
                VietMapEvents.ON_NEW_ROUTE_SELECTED, JSONObject(it.toJson())
            )
            //TODO: Handle send event here
        }

        vietMapGL.addOnMapClickListener(this)

    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        val customLocationComponentOptions =
            LocationComponentOptions.builder(context).pulseEnabled(true)
//                .backgroundDrawable()
                .build()
        vietMapGL.locationComponent.let { locationComponent ->
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(context, loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .locationEngine(locationEngine).build()
            )

            locationComponent.locationEngine = locationEngine
            if (locationEngine is ReplayRouteLocationEngine) {

                if (initialLatitude != null && initialLongitude != null) {
                    (locationEngine as ReplayRouteLocationEngine).assignLastLocation(
                        Point.fromLngLat(
                            initialLongitude!!, initialLatitude!!
                        )
                    )
                }
                locationComponent.setCameraMode(
                    CameraMode.TRACKING_GPS_NORTH,
                    750L,
                    navigationZoomLevel,
                    0.0,
                    tilt,
                    null
                )
            } else {
                locationComponent.setCameraMode(
                    CameraMode.TRACKING_GPS_NORTH,
                    750L,
                    mapZoomLevel,
                    locationComponent.lastKnownLocation?.bearing?.toDouble() ?: 0.0,
                    tilt,
                    null
                )
            }
            locationComponent.zoomWhileTracking(mapZoomLevel)
            locationComponent.renderMode = RenderMode.GPS

            locationComponent.isLocationComponentEnabled = true
        }

    }

    private fun addDestinationIconSymbolLayer(loadedMapStyle: Style) {
        val geoJsonSource = GeoJsonSource("destination-source-id")
        loadedMapStyle.addSource(geoJsonSource)

        val destinationSymbolLayer =
            SymbolLayer("destination-symbol-layer-id", "destination-source-id")
        destinationSymbolLayer.withProperties(
            iconImage("destination-icon-id"), iconAllowOverlap(true), iconIgnorePlacement(true)
        )
        loadedMapStyle.addLayer(destinationSymbolLayer)
    }

    private fun moveCamera(
        location: LatLng,
        bearing: Float?,
        zoomLevel: Double = navigationZoomLevel,
    ) {

        val cameraPosition = CameraPosition.Builder().target(location).zoom(zoomLevel).tilt(tilt)

        if (bearing != null) {
            cameraPosition.bearing(bearing.toDouble())
        }

        var duration = 1000
        if (!animateBuildRoute) duration = 1
        vietMapGL.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition.build()), duration
        )
    }


    private fun getRoute(
        context: Context, isStartNavigation: Boolean, bearing: Float?, profile: String,
    ) {
//        if (!PluginUtilities.isNetworkAvailable(context)) {
//            sendEvent(
//                VietMapEvents.ROUTE_BUILD_FAILED,
//            )
//            return
//        } 
        sendEvent(VietMapEvents.ROUTE_BUILDING)
        val br = bearing ?: 0.0
        val routeOptions = RouteOptions.builder()
            .coordinates(listNavigationRemainingPoints)
            .bearings(br.toString())
            .alternatives(true)
            .baseUrl("")
            .accessToken(apikey ?: "")
            .requestUuid("")
            .profile(profile)
            .user("")
            .build()
        val build = NavigationRoute.builder(context)
            .baseUrl(baseUrl)
            .apikey(apikey ?: "")
            .routeOptions(routeOptions)
            .origin(listNavigationRemainingPoints.first(), 60.0, br.toDouble())
            .destination(listNavigationRemainingPoints.last())
            .alternatives(true)
        ///driving-traffic
        ///cycling
        ///walking
        ///motorcycle
//            .profile(profile)

        for (i in 1 until listNavigationRemainingPoints.size - 1) {
            build.addWaypoint(listNavigationRemainingPoints[i])
        }
        val builder = build.build()
        listNavigationRemainingPoints.removeAt(0)
        builder.getRoute(object : Callback<DirectionsResponse?> {
            override fun onResponse(
                call: Call<DirectionsResponse?>, response: Response<DirectionsResponse?>,
            ) {
                if (response.body() == null || response.body()!!.routes().size < 1) {
                    sendEvent(VietMapEvents.ROUTE_BUILD_FAILED)
                    return
                }
                directionsRoutes = response.body()!!.routes()
                currentRoute = if (directionsRoutes!!.size <= primaryRouteIndex) {
                    directionsRoutes!![0]
                } else {
                    directionsRoutes!![primaryRouteIndex]
                }

                sendEvent(VietMapEvents.ROUTE_BUILT, currentRoute?.toJson()?.let { JSONObject(it) })

                // Draw the route on the map
                if (navigationMapRoute != null) {
                    navigationMapRoute?.removeRoute()
                } else {
                    val routeStyleRes = ThemeSwitcher.retrieveNavigationViewStyle(
                        binding.mapView!!.context,
                        vn.vietmap.services.android.navigation.R.style.NavigationMapRoute
                    )
                    
                    navigationMapRoute =
                        NavigationMapRoute(
                            navigation,
                            binding.mapView!!,
                            vietMapGL!!,
                            routeStyleRes,
                            "vmadmin_province"
                        )
                }

                //show multiple route to map
                if (response.body()!!.routes().size > 1) {
                    navigationMapRoute?.addRoutes(directionsRoutes!!)
                } else {
                    navigationMapRoute?.addRoute(currentRoute)
                }


                isBuildingRoute = false
                // get route point from current route
                val routePoints: List<Point> =
                    currentRoute?.routeOptions()?.coordinates() as List<Point>
                animateVietmapGLForRouteOverview(padding, routePoints)
                //Start Navigation again from new Point, if it was already in Progress 
                if (isNavigationInProgress || isStartNavigation) {
                    startNavigation()
                }
            }

            override fun onFailure(call: Call<DirectionsResponse?>, throwable: Throwable) {
                isBuildingRoute = false
                sendEvent(
                    VietMapEvents.ROUTE_BUILD_FAILED
                )
            }
        })
    }

    fun buildRoute(data: Any?) {
        if (data != null) {

            Log.d("points", (data as Map<*, *>)["points"].toString())
            profile = data["vehicle"].toString()
            val points = data["points"] as ReadableArray

            startRoute(
                points,
                profile
            )
        }

    }

    fun demoVMFunc() {
        startNavigation()
    }

    fun startNavigation() {
        context.currentActivity?.runOnUiThread {

        tilt = 10000.0
        isOverviewing = false
        isNavigationCanceled = false

        navigation = VietmapNavigation(
            context, navigationOptions, locationEngine!!
        )
        if (currentRoute != null) {
            if (shouldSimulateRoute) {
                val mockLocationEngine = ReplayRouteLocationEngine()

                mockLocationEngine.assign(currentRoute)
                navigation?.locationEngine = mockLocationEngine
            } else {
                locationEngine?.let {
                    navigation?.locationEngine = it
                }
            }
            isRunning = true
            vietMapGL.locationComponent.locationEngine = null
            navigation?.addNavigationEventListener(this)
            navigation?.addFasterRouteListener(this)
            navigation?.addMilestoneEventListener(this)
            navigation?.addOffRouteListener(this)
            navigation?.addProgressChangeListener(this)

            
            navigation?.snapEngine = snapEngine

            navigationMapRoute!!.updateRouteArrowVisibilityTo(true)
            navigationMapRoute!!.showAlternativeRoutes(true)
            navigationMapRoute!!.updateRouteVisibilityTo(true)
            navigationMapRoute!!.showAlternativeRoutes(true)
            currentRoute?.let {
                isNavigationInProgress = true
                navigation?.startNavigation(currentRoute!!)
                sendEvent(VietMapEvents.NAVIGATION_RUNNING)
                recenter()

            }
        }
            }
    }

    fun finishNavigation(isOffRouted: Boolean = false) {
        context.currentActivity?.runOnUiThread {

//            navigationZoomLevel = mapZoomLevel
            bearing = 0.0
            tilt = 0.0
            isNavigationCanceled = true

            if (!isOffRouted) {
                isNavigationInProgress = false
//                moveCameraToOriginOfRoute()
                overViewRoute()
            }

            if (currentRoute != null) {
                isRunning = false
                navigation?.stopNavigation()
                navigation?.removeFasterRouteListener(this)
                navigation?.removeMilestoneEventListener(this)
                navigation?.removeNavigationEventListener(this)
                navigation?.removeOffRouteListener(this)
                navigation?.removeProgressChangeListener(this)
                
                if (restartAlert) {
                    // Nếu có cảnh báo tốc độ đang tạm dừng thì khởi động lại
                    val speedAlertManager = vietmapSDK.speedAlertManager
                    speedAlertManager.startSpeedAlerts()
                    restartAlert = false
                }
            }

        }
    }

    private fun moveCameraToOriginOfRoute() {
        currentRoute?.let {
            try {
                val originCoordinate = it.routeOptions()?.coordinates()?.get(0)
                originCoordinate?.let {
                    val location = LatLng(originCoordinate.latitude(), originCoordinate.longitude())
                    // println("MoveCamera1")
                    moveCamera(location, null)
                }
            } catch (e: java.lang.Exception) {
                Timber.i(String.format("moveCameraToOriginOfRoute, %s", "Error: ${e.message}"))
            }
        }
    }

    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {

        var currentSpeed = location.speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentSpeed = location.speedAccuracyMetersPerSecond
        }
        if (!isNavigationCanceled) {
            try {
                val noRoutes: Boolean = directionsRoutes?.isEmpty() ?: true

                val newCurrentRoute: Boolean = !routeProgress.directionsRoute()
                    .equals(directionsRoutes?.get(primaryRouteIndex))
                val isANewRoute: Boolean = noRoutes || newCurrentRoute
                if (isANewRoute) {
                } else {

                    distanceRemaining = routeProgress.distanceRemaining()
                    durationRemaining = routeProgress.durationRemaining()


                    if (!isDisposed && !isBuildingRoute) {
                        val snappedLocation: Location =
                            snapEngine.getSnappedLocation(location, routeProgress)
                        // Xử lý cảnh báo tốc độ
                        val speedAlertManager = vietmapSDK.speedAlertManager
                        if (speedAlertManager.isSpeedAlertActive()) {
                            // Stop alert trước khi xử lý
                            speedAlertManager.stopSpeedAlerts()
                            restartAlert = true
                        }
                        // Log.d("VietmapSpeedAlert", "Current location: ${snappedLocation.latitude}, ${snappedLocation.longitude}, ${snappedLocation.speed}, ${snappedLocation.bearing.toDouble()}")
                        // Xử lý location
                        speedAlertManager.processExternalLocation(snappedLocation.latitude, snappedLocation.longitude, snappedLocation.speed.toDouble(), snappedLocation.bearing.toDouble())

                        val progressEvent =
                            VietMapRouteProgressEvent(routeProgress, location, snappedLocation)
                        sendRouteProgressEvent(progressEvent)
                        currentCenterPoint =
                            CurrentCenterPoint(
                                snappedLocation.latitude,
                                snappedLocation.longitude,
                                snappedLocation.bearing
                            )
                        if (!isOverviewing) {
                            this.routeProgress = routeProgress
                            if (currentSpeed > 0) {
                                moveCamera(
                                    LatLng(snappedLocation.latitude, snappedLocation.longitude),
                                    snappedLocation.bearing,
                                    zoomLevel = navigationZoomLevel
                                )
                            }
                        }

                        vietMapGL.locationComponent.forceLocationUpdate(snappedLocation)
                    }

//                    if (shouldSimulateRoute && !isDisposed && !isBuildingRoute) {
//                        vietmapGL?.locationComponent?.forceLocationUpdate(location)
//                    }

                    if (!isRefreshing) {
                        isRefreshing = true
                    }
                }
                handleProgressChange(routeProgress, location)
            } catch (_: java.lang.Exception) {
            }
        }
    }

    override fun userOffRoute(location: Location) {
        if (checkIfUserOffRoute(location)) {
            speechPlayer!!.onOffRoute()
            sendEvent(
                VietMapEvents.USER_OFF_ROUTE,
                JSONObject("{\"latitude\":${location.latitude},\"longitude\":${location.longitude}}")
            )
            doOnNewRoute(Point.fromLngLat(location.longitude, location.latitude))
        }
    }

    private fun doOnNewRoute(offRoutePoint: Point?) {
        if (!isBuildingRoute) {
            isBuildingRoute = true

            offRoutePoint?.let {

                finishNavigation(isOffRouted = true)
                // println("MoveCamera3")

                moveCamera(LatLng(it.latitude(), it.longitude()), null)

                sendEvent(
                    VietMapEvents.USER_OFF_ROUTE, JSONObject(
                        VietMapLocation(
                            latitude = it.latitude(), longitude = it.longitude()
                        ).toString()
                    )
                )

            }

//            originPoint = offRoutePoint

            if (listNavigationRemainingPoints.isNotEmpty()) {
                listNavigationRemainingPoints.add(0, offRoutePoint!!)
                isNavigationInProgress = true

                fetchRouteWithBearing(false, profile)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchRouteWithBearing(isStartNavigation: Boolean, profile: String) {
        context.currentActivity?.let {
            fusedLocationClient?.lastLocation?.addOnSuccessListener(
                it
            ) { location: Location? ->
                if (location != null) {
                    getRoute(context, isStartNavigation, location.bearing, profile)
                } else {

                    getRoute(context, isStartNavigation, null, profile)
                }
            }
        }

    }

    private fun checkIfUserOffRoute(location: Location): Boolean {
        if (routeProgress?.currentStepPoints() != null) {
            val snapLocation: Location = snapEngine.getSnappedLocation(location, routeProgress)
            val distance: Double = calculateDistanceBetween2Point(location, snapLocation)
            return distance > this.distanceToOffRoute && checkIfUserIsDrivingToOtherRoute(location)
//                && areBearingsClose(
//            location.bearing.toDouble(), snapLocation.bearing.toDouble()
//        )
        }
        return false
    }

    private fun checkIfUserIsDrivingToOtherRoute(location: Location): Boolean {
        directionsRoutes?.forEach {
            //get list point
            snapLocationLatLng(
                location,
                it.routeOptions()?.coordinates() as List<Point>
            ).let { snapLocation ->
                val distance: Double = calculateDistanceBetween2Point(location, snapLocation)
                if (distance < 30) {
                    if (it != currentRoute) {
                        currentRoute = it
                        currentRoute?.toJson()?.let { it1 ->
                            sendEvent(
                                VietMapEvents.ON_NEW_ROUTE_SELECTED,
                                JSONObject(it1)
                            )
                            return false
                        }
                    }

                }
            }
        }
        return true
    }

    private fun snapLocationLatLng(location: Location, stepCoordinates: List<Point>): Location {
        val snappedLocation = Location(location)
        val locationToPoint = Point.fromLngLat(location.longitude, location.latitude)
        if (stepCoordinates.size > 1) {
            val feature = TurfMisc.nearestPointOnLine(locationToPoint, stepCoordinates)
            val point = feature.geometry() as Point?
            snappedLocation.longitude = point!!.longitude()
            snappedLocation.latitude = point.latitude()
        }
        return snappedLocation
    }

    private fun calculateDistanceBetween2Point(location1: Location, location2: Location): Double {
        val radius = 6371000.0 // meters

        val dLat = (location2.latitude - location1.latitude) * PI / 180.0
        val dLon = (location2.longitude - location1.longitude) * PI / 180.0

        val a =
            sin(dLat / 2.0) * sin(dLat / 2.0) + cos(location1.latitude * PI / 180.0) * cos(location2.latitude * PI / 180.0) * sin(
                dLon / 2.0
            ) * sin(dLon / 2.0)
        val c = 2.0 * kotlin.math.atan2(sqrt(a), sqrt(1.0 - a))

        return radius * c
    }

    override fun onMilestoneEvent(
        routeProgress: RouteProgress,
        instruction: String,
        milestone: Milestone,
    ) {
        if (voiceInstructionsEnabled) {
            playVoiceAnnouncement(milestone)
        }
        if (routeUtils.isArrivalEvent(routeProgress, milestone) && isNavigationInProgress) {
            val arrivalLatLng = routeProgress.currentLegProgress()?.currentStep()?.maneuver()?.location()
            val data:JSONObject = JSONObject()
            arrivalLatLng?.let {
                data.put("latitude", arrivalLatLng.latitude())
                data.put("longitude", arrivalLatLng.longitude())
            }
            sendEvent(VietMapEvents.ON_ARRIVAL,data = data)

            if (listNavigationRemainingPoints.size == 1) {


                finishNavigation()
                try {
                    vietMapGL.locationComponent.locationEngine = locationEngine
                } catch (_: Exception) {
                }
            }
            listNavigationRemainingPoints.removeAt(0)
            Log.d("DataList---------------", listNavigationRemainingPoints.size.toString())
            Log.d("Datalist", listNavigationRemainingPoints.toList().toString())

        }
        if (!isNavigationCanceled) {
            sendEvent(VietMapEvents.MILESTONE_EVENT, JSONObject().put("instruction", instruction))
        }
    }

    private fun playVoiceAnnouncement(milestone: Milestone?) {
        if (milestone is VoiceInstructionMilestone) {
            val announcement = SpeechAnnouncement.builder()
                .voiceInstructionMilestone(milestone as VoiceInstructionMilestone?).build()
            speechPlayer!!.play(announcement)
        }
    }

    private fun configSpeechPlayer() {
        val speechPlayerProvider = SpeechPlayerProvider(context, "vi", true)
        this.speechPlayer = NavigationSpeechPlayer(speechPlayerProvider)
    }

    override fun onRunning(p0: Boolean) {
        if (!isNavigationCanceled) {
            sendEvent(VietMapEvents.NAVIGATION_RUNNING)

        }
    }

    override fun onCancelNavigation() {
        sendEvent(VietMapEvents.NAVIGATION_CANCELLED)
        navigation?.stopNavigation()
        isRunning = false
        
        if (restartAlert) {
            // Nếu trước đó có cảnh báo tốc độ đang hoạt động thì restart lại
            val speedAlertManager = vietmapSDK.speedAlertManager
            speedAlertManager.startSpeedAlerts()
            restartAlert = false
        }
    }

    override fun onNavigationFinished() {
        vietMapGL.locationComponent.locationEngine = locationEngine
        sendEvent(VietMapEvents.NAVIGATION_FINISHED)
        
        if (restartAlert) {
            // Nếu trước đó có cảnh báo tốc độ đang hoạt động thì restart lại
            val speedAlertManager = vietmapSDK.speedAlertManager
            speedAlertManager.startSpeedAlerts()
            restartAlert = false
        }
    }

    override fun onNavigationRunning() {
        if (!isNavigationCanceled) {
            sendEvent(VietMapEvents.NAVIGATION_RUNNING)

        }
    }

    override fun fasterRouteFound(directionsRoute: DirectionsRoute) {

        refreshNavigation(directionsRoute)
    }

    private fun refreshNavigation(directionsRoute: DirectionsRoute?, shouldCancel: Boolean = true) {
        directionsRoute?.let {

            if (shouldCancel) {

                currentRoute = directionsRoute
                finishNavigation()
                startNavigation()
            }
        }
    }

    override fun willVoice(announcement: SpeechAnnouncement?): SpeechAnnouncement? {
        return if (voiceInstructionsEnabled) {

            announcement
        } else {
            null
        }
    }

    override fun willDisplay(instructions: BannerInstructions?): BannerInstructions? {
        return if (bannerInstructionsEnabled) {

            return instructions
        } else {
            null
        }
    }

    override fun onResponseReceived(p0: DirectionsResponse?, p1: RouteProgress?) {
    }

    override fun onErrorReceived(p0: Throwable?) {
    }

    override fun onDidFinishRenderingMap(p0: Boolean) {
    }

    private fun animateVietmapGLForRouteOverview(padding: IntArray, routePoints: List<Point>) {
        if (routePoints.size <= 1) {
            return
        }
        val resetUpdate: CameraUpdate = buildResetCameraUpdate()
        val overviewUpdate: CameraUpdate = buildOverviewCameraUpdate(padding, routePoints)
        context.currentActivity?.runOnUiThread {
            vietMapGL.animateCamera(
                resetUpdate, 150, CameraOverviewCancelableCallback(overviewUpdate, vietMapGL)
            )
        }
    }

    private fun buildResetCameraUpdate(): CameraUpdate {
        val resetPosition: CameraPosition = CameraPosition.Builder().tilt(0.0).bearing(0.0).build()
        return CameraUpdateFactory.newCameraPosition(resetPosition)
    }

    private fun buildOverviewCameraUpdate(
        padding: IntArray, routePoints: List<Point>,
    ): CameraUpdate {
        val routeBounds = convertRoutePointsToLatLngBounds(routePoints)
        return newLatLngBounds(
            routeBounds, padding[0], padding[1], padding[2], padding[3]
        )
    }

    private fun convertRoutePointsToLatLngBounds(routePoints: List<Point>): LatLngBounds {
        val latLngs: MutableList<LatLng> = ArrayList()
        for (routePoint in routePoints) {
            latLngs.add(LatLng(routePoint.latitude(), routePoint.longitude()))
        }
        return LatLngBounds.Builder().includes(latLngs).build()
    }


    private fun handleProgressChange(routeProgress: RouteProgress, location: Location) {
        // println("handleProgressChange")
        if (location.speed < 1) return
        // println("start handleProgressChange")

        val distanceRemainingToNextTurn =
            routeProgress.currentLegProgress()?.currentStepProgress()?.distanceRemaining()
        if (distanceRemainingToNextTurn != null && distanceRemainingToNextTurn < 30) {
            isNextTurnHandling = true
            val resetPosition: CameraPosition =
                CameraPosition.Builder().tilt(0.0).zoom(17.0).build()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(resetPosition)
            vietMapGL.animateCamera(
                cameraUpdate, 1000
            )
        } else {
            if (routeProgress.currentLegProgress().currentStepProgress()
                    .distanceTraveled() > 30 && !isOverviewing
            ) {
                isNextTurnHandling = false
                recenter()
            }
        }
    }


    fun recenter() {
        isOverviewing = false
        if (currentCenterPoint != null) {
            context.currentActivity?.runOnUiThread {
                moveCamera(
                    LatLng(currentCenterPoint!!.latitude, currentCenterPoint!!.longitude),
                    currentCenterPoint!!.bearing,
                    zoomLevel = navigationZoomLevel
                )
            }
        }

    }

    fun overViewRoute() {
        isOverviewing = true
        routeProgress?.let { showRouteOverview(padding, it) }
    }

    fun clearRoute() {
        context.currentActivity?.runOnUiThread {
            if (navigationMapRoute != null) {
                navigationMapRoute?.removeRoute()
            }
            currentRoute = null
        }
    }


    private fun showRouteOverview(padding: IntArray?, currentRouteProgress: RouteProgress) {

        val routeInformation: RouteInformation =
            buildRouteInformationFromProgress(currentRouteProgress)
        animateCameraForRouteOverview(routeInformation, padding!!)
    }

    private fun buildRouteInformationFromProgress(routeProgress: RouteProgress?): RouteInformation {
        return if (routeProgress == null) {
            RouteInformation.create(null, null, null)
        } else RouteInformation.create(routeProgress.directionsRoute(), null, null)
    }

    private fun animateCameraForRouteOverview(
        routeInformation: RouteInformation, padding: IntArray,
    ) {
        val cameraEngine = navigation?.cameraEngine
        val routePoints = cameraEngine?.overview(routeInformation)
        if (routePoints?.isNotEmpty() == true) {
            animateVietmapGLForRouteOverview(padding, routePoints)
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        if (routeClicked) {
            routeClicked = false
            return true
        }

        val pointF = vietMapGL.projection.toScreenLocation(point)
        val map = JSONObject()
        map.put("latitude", point.latitude)
        map.put("longitude", point.longitude)
        map.put("x", pointF.x)
        map.put("y", pointF.y)
        sendEvent(
            VietMapEvents.ON_MAP_CLICK,
            map
        )
        return true
    }


    override fun onMapLongClick(point: LatLng): Boolean {
        val pointf = vietMapGL.projection.toScreenLocation(point)

        val map = JSONObject()
        map.put("latitude", point.latitude)
        map.put("longitude", point.longitude)
        map.put("x", pointf.x)
        map.put("y", pointf.y)
        sendEvent(
            VietMapEvents.ON_MAP_LONG_CLICK,
            map
        )
        return false

    }


    private fun sendRouteProgressEvent(event: VietMapRouteProgressEvent) {
        val dataString = JSONObject(event.toJson())
        val writableMap = Arguments.createMap()
        writableMap.putString("eventType", VietMapEvents.PROGRESS_CHANGE.value)

        val convertedData = JSONConverter().convertJsonToReadableMap(dataString)
        writableMap.putMap("data", convertedData)
        val surfaceId = UIManagerHelper.getSurfaceId(this)
        UIManagerHelper.getEventDispatcherForReactTag(context, id)
            ?.dispatchEvent(VietMapEvent(surfaceId, id, "onRouteProgressChange", writableMap))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startSpeedAlert() {
        // Check permissions for GPS mode (always starts with GPS)
        if (!vietmapSDK.hasLocationPermissions()) {
            checkPermissions()
            return
        }

        // Get speed alert manager and start with GPS mode (simplified)
        val speedAlertManager = vietmapSDK.speedAlertManager
        speedAlertManager.startSpeedAlerts() // Always starts with GPS
        sendEvent(VietMapEvents.START_ALERT)
    }

    fun stopSpeedAlert() {
        val speedAlertManager = vietmapSDK.speedAlertManager
        speedAlertManager.stopSpeedAlerts()
        restartAlert = false
        sendEvent(VietMapEvents.STOP_ALERT)
    }

    fun isSpeedAlertActive(): Boolean {
        return try {
            val speedAlertManager = vietmapSDK.speedAlertManager
            speedAlertManager.isSpeedAlertActive()
        } catch (e: Exception) {
            false
        }
    }

    fun configVehicleSpeedAlert(vehicleId: String, vehicleType: Int, seats: Int, weight: Double) {
        // Store vehicle configuration parameters
        pendingVehicleId = vehicleId
        pendingVehicleType = vehicleType
        pendingVehicleSeats = seats
        pendingVehicleWeight = weight
        
        Log.d(TAG, "Storing vehicle config: ID=$vehicleId, Type=$vehicleType, Seats=$seats, Weight=$weight")
        
        // Try to configure immediately if SDK is initialized
        configureVehicle()
    }

    private fun sendEvent(eventName: VietMapEvents, data: JSONObject? = null) {
        val writableMap = Arguments.createMap()
        writableMap.putString("eventType", eventName.value)
        if (data != null) {
            val convertedData = JSONConverter().convertJsonToReadableMap(data)
            writableMap.putMap("data", convertedData)
        }
        val surfaceId = UIManagerHelper.getSurfaceId(this)
        UIManagerHelper.getEventDispatcherForReactTag(context, id)
            ?.dispatchEvent(VietMapEvent(surfaceId, id, eventName.value, writableMap))
    }

    fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    private fun checkPermissions() {
        context.currentActivity?.let { activity ->
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )
            }
        }
    }
}