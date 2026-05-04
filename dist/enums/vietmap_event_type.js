export var VietMapEvents;
(function (VietMapEvents) {
    VietMapEvents["MAP_READY"] = "onMapReady";
    VietMapEvents["ROUTE_BUILDING"] = "onRouteBuilding";
    VietMapEvents["ROUTE_BUILT"] = "onRouteBuilt";
    VietMapEvents["ROUTE_BUILD_FAILED"] = "onRouteBuildFailed";
    VietMapEvents["ROUTE_BUILD_CANCELLED"] = "routeBuildCancelled";
    VietMapEvents["ROUTE_BUILD_NO_ROUTES_FOUND"] = "routeBuildNoRoutesFound";
    VietMapEvents["PROGRESS_CHANGE"] = "progressChange";
    VietMapEvents["USER_OFF_ROUTE"] = "onUserOffRoute";
    VietMapEvents["MILESTONE_EVENT"] = "onMilestoneEvent";
    VietMapEvents["NAVIGATION_RUNNING"] = "onNavigationRunning";
    VietMapEvents["NAVIGATION_CANCELLED"] = "onNavigationCancelled";
    VietMapEvents["NAVIGATION_FINISHED"] = "onNavigationFinished";
    VietMapEvents["FASTER_ROUTE_FOUND"] = "fasterRouteFound";
    VietMapEvents["SPEECH_ANNOUNCEMENT"] = "speechAnnouncement";
    VietMapEvents["BANNER_INSTRUCTION"] = "bannerInstruction";
    VietMapEvents["ON_ARRIVAL"] = "onArrival";
    VietMapEvents["FAILED_TO_REROUTE"] = "failedToReroute";
    VietMapEvents["REROUTE_ALONG"] = "rerouteAlong";
    VietMapEvents["ON_MAP_MOVE"] = "onMapMove";
    VietMapEvents["ON_MAP_LONG_CLICK"] = "onMapLongClick";
    VietMapEvents["ON_MAP_MOVE_END"] = "onMapMoveEnd";
    VietMapEvents["ON_MAP_CLICK"] = "onMapClick";
    VietMapEvents["ON_MAP_RENDERED"] = "onMapRendered";
    VietMapEvents["ON_NEW_ROUTE_SELECTED"] = "onNewRouteSelected";
    VietMapEvents["CAMERA_ON_MOVE"] = "cameraOnMove";
    VietMapEvents["MARKER_CLICKED"] = "markerClicked";
})(VietMapEvents || (VietMapEvents = {}));
// interface String {
//     toVietMapEvent(): VietMapEvents | null;
// }
String.prototype.toVietMapEvent = function () {
    const enumValue = VietMapEvents[this];
    return enumValue !== undefined ? enumValue : null;
};
// Usage
// const event = VietMapEvents.fromString('onMapReady'); // event will be VietMapEvents.MAP_READY
// const invalidEvent = VietMapEvents.fromString('invalidEvent'); // invalidEvent will be null
