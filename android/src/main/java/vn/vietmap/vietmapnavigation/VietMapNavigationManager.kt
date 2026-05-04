package vn.vietmap.vietmapnavigation

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.VietMapNavigationManagerDelegate
import com.facebook.react.viewmanagers.VietMapNavigationManagerInterface
import vn.vietmap.vietmapsdk.Vietmap
import javax.annotation.Nonnull

class VietMapNavigationManager(mCallerContext: ReactApplicationContext) :
    SimpleViewManager<VietMapNavigationView>(),
    VietMapNavigationManagerInterface<VietMapNavigationView> {

    private val delegate = VietMapNavigationManagerDelegate(this)

    override fun getDelegate(): ViewManagerDelegate<VietMapNavigationView> = delegate
    init {
        mCallerContext.runOnUiQueueThread {
        }
    }

    override fun getName(): String = "VietMapNavigation"

    public override fun createViewInstance(@Nonnull reactContext: ThemedReactContext): VietMapNavigationView {
        Vietmap.getInstance(reactContext)
        VietMapNavigationView.instance = VietMapNavigationView(reactContext)
        return VietMapNavigationView.instance!!
    }

    override fun onDropViewInstance(view: VietMapNavigationView) {
        view.onDropViewInstance()
        super.onDropViewInstance(view)
    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Map<String, String>>? {
        return mutableMapOf<String, Map<String, String>>(
            "onLocationChange" to MapBuilder.of("registrationName", "onLocationChange"),
            "onError" to MapBuilder.of("registrationName", "onError"),
            "onCancelNavigation" to MapBuilder.of("registrationName", "onCancelNavigation"),
            "onArrive" to MapBuilder.of("registrationName", "onArrive"),
            "onMapReady" to MapBuilder.of("registrationName", "onMapReady"),
            "onRouteBuilding" to MapBuilder.of("registrationName", "onRouteBuilding"),
            "onRouteBuilt" to MapBuilder.of("registrationName", "onRouteBuilt"),
            "onRouteBuildFailed" to MapBuilder.of("registrationName", "onRouteBuildFailed"),
            "routeBuildCancelled" to MapBuilder.of("registrationName", "routeBuildCancelled"),
            "routeBuildNoRoutesFound" to MapBuilder.of("registrationName", "routeBuildNoRoutesFound"),
            "onRouteProgressChange" to MapBuilder.of("registrationName", "onRouteProgressChange"),
            "onUserOffRoute" to MapBuilder.of("registrationName", "onUserOffRoute"),
            "onMilestoneEvent" to MapBuilder.of("registrationName", "onMilestoneEvent"),
            "onNavigationRunning" to MapBuilder.of("registrationName", "onNavigationRunning"),
            "onNavigationCancelled" to MapBuilder.of("registrationName", "onNavigationCancelled"),
            "onNavigationFinished" to MapBuilder.of("registrationName", "onNavigationFinished"),
            "fasterRouteFound" to MapBuilder.of("registrationName", "fasterRouteFound"),
            "speechAnnouncement" to MapBuilder.of("registrationName", "speechAnnouncement"),
            "bannerInstruction" to MapBuilder.of("registrationName", "bannerInstruction"),
            "onArrival" to MapBuilder.of("registrationName", "onArrival"),
            "failedToReroute" to MapBuilder.of("registrationName", "failedToReroute"),
            "rerouteAlong" to MapBuilder.of("registrationName", "rerouteAlong"),
            "onMapMove" to MapBuilder.of("registrationName", "onMapMove"),
            "onMapMoveEnd" to MapBuilder.of("registrationName", "onMapMoveEnd"),
            "onMapLongClick" to MapBuilder.of("registrationName", "onMapLongClick"),
            "onMapClick" to MapBuilder.of("registrationName", "onMapClick"),
            "onMapRendered" to MapBuilder.of("registrationName", "onMapRendered"),
            "onNewRouteSelected" to MapBuilder.of("registrationName", "onNewRouteSelected"),
            "cameraOnMove" to MapBuilder.of("registrationName", "cameraOnMove"),
            "markerClicked" to MapBuilder.of("registrationName", "markerClicked"),
            "startAlert" to MapBuilder.of("registrationName", "startAlert"),
            "stopAlert" to MapBuilder.of("registrationName", "stopAlert")
        )
    }

    @ReactProp(name = "apiKey")
    override fun setApiKey(view: VietMapNavigationView, sources: String?) {
        view.setApiKey(sources ?: "")
    }

    @ReactProp(name = "baseUrl")
    override fun setBaseUrl(view: VietMapNavigationView, sources: String?) {
        if (sources != null) {
            view.setBaseUrl(sources)
        }
    }

    @ReactProp(name = "navigationZoomLevel")
    override fun setNavigationZoomLevel(view: VietMapNavigationView, zoomLevel: Double) {
        view.setNavigationZoomLevel(zoomLevel)
    }

    @ReactProp(name = "initialLatLngZoom")
    override fun setInitialLatLngZoom(view: VietMapNavigationView, initialLatLng: ReadableMap?) {
        if (initialLatLng != null) {
            view.setInitialLatLngZoom(initialLatLng)
        }
    }

    @ReactProp(name = "navigationTiltLevel")
    override fun setNavigationTiltLevel(view: VietMapNavigationView, value: Double) {
        view.setNavigationTiltAnchor(value)
    }

    @ReactProp(name = "navigationPadding")
    override fun setNavigationPadding(view: VietMapNavigationView, padding: ReadableMap?) {
        if (padding != null) {
            view.setNavigationPadding(padding)
        }
    }

    @ReactProp(name = "shouldSimulateRoute")
    override fun setShouldSimulateRoute(view: VietMapNavigationView, shouldSimulateRoute: Boolean) {
        view.setShouldSimulateRoute(shouldSimulateRoute)
    }

    @ReactProp(name = "apiKeyAlert")
    override fun setApiKeyAlert(view: VietMapNavigationView, apiKeyAlert: String?) {
        if (apiKeyAlert != null) {
            view.setApiKeyAlert(apiKeyAlert)
        }
    }

    @ReactProp(name = "apiIDAlert")
    override fun setApiIDAlert(view: VietMapNavigationView, apiIDAlert: String?) {
        if (apiIDAlert != null) {
            view.setApiIDAlert(apiIDAlert)
        }
    }


}