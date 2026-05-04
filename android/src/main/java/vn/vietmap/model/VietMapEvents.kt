package vn.vietmap.model



enum class VietMapEvents(val value: String) {
    MAP_READY("onMapReady"),
    ROUTE_BUILDING("onRouteBuilding"),
    ROUTE_BUILT("onRouteBuilt"),
    ROUTE_BUILD_FAILED("onRouteBuildFailed"),
    ROUTE_BUILD_CANCELLED("routeBuildCancelled"),
    ROUTE_BUILD_NO_ROUTES_FOUND("routeBuildNoRoutesFound"),
    PROGRESS_CHANGE("progressChange"),
    USER_OFF_ROUTE("onUserOffRoute"),
    MILESTONE_EVENT("onMilestoneEvent"),
    NAVIGATION_RUNNING("onNavigationRunning"),
    NAVIGATION_CANCELLED("onNavigationCancelled"),
    NAVIGATION_FINISHED("onNavigationFinished"),
    FASTER_ROUTE_FOUND("fasterRouteFound"),
    SPEECH_ANNOUNCEMENT("speechAnnouncement"),
    BANNER_INSTRUCTION("bannerInstruction"),
    ON_ARRIVAL("onArrival"),
    FAILED_TO_REROUTE("failedToReroute"),
    REROUTE_ALONG("rerouteAlong"),
    ON_MAP_MOVE("onMapMove"),
    ON_MAP_LONG_CLICK("onMapLongClick"),
    ON_MAP_MOVE_END("onMapMoveEnd"),
    ON_MAP_CLICK("onMapClick"),
    ON_MAP_RENDERED("onMapRendered"),
    ON_NEW_ROUTE_SELECTED("onNewRouteSelected"),
    CAMERA_ON_MOVE("cameraOnMove"),
    MARKER_CLICKED("markerClicked"),
    START_ALERT("startAlert"),
    STOP_ALERT("stopAlert"),

}
