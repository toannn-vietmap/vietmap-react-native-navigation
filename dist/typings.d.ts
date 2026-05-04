/** @type {[number, number]}
 * Provide an array with longitude and latitude [$longitude, $latitude]
 */
import { NavigationProgressData } from "./models/route_progress_data";
import { RouteData } from "./models/route_data";
type LocationData = {
    nativeEvent: {
        data: {
            latitude: number;
            longitude: number;
            x?: string | null;
            y?: string | null;
        };
    };
};
type NavigationPadding = {
    left: number;
    top: number;
    right: number;
    bottom: number;
};
type InitialLatLngZoom = {
    lat: number;
    lng: number;
    zoom: number;
};
interface MilestoneData {
    nativeEvent: {
        data: {
            instruction: string;
        };
        eventType: string;
    };
}
export interface IVietMapNavigationProps {
    baseUrl?: string;
    apiKey: string;
    apiKeyAlert?: string;
    apiIDAlert?: string;
    shouldSimulateRoute?: boolean;
    initialLatLngZoom?: InitialLatLngZoom;
    navigationZoomLevel?: number;
    navigationTiltAnchor?: number;
    navigationPadding?: NavigationPadding;
    style?: object;
    onRouteProgressChange?: (event: NavigationProgressData) => void;
    onCancelNavigation?: () => void;
    onRouteBuilt?: (event: RouteData) => void;
    onMapLongClick?: (event: LocationData) => void;
    onMapMove?: () => void;
    onMapMoveEnd?: () => void;
    onNavigationFinished?: () => void;
    onNavigationCancelled?: () => void;
    onNavigationRunning?: () => void;
    onRouteBuildFailed?: () => void;
    onRouteBuilding?: () => void;
    onMapReady?: () => void;
    onMilestoneEvent?: (event: MilestoneData) => void;
    onUserOffRoute?: (event: LocationData) => void;
    onArrival?: (event: LocationData) => void;
    onNewRouteSelected?: (event: RouteData) => void;
    onMapClick?: (event: LocationData) => void;
}
export {};
