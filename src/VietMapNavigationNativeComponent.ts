import type { HostComponent, ViewProps } from 'react-native';
import type {
  DirectEventHandler,
  Double,
} from 'react-native/Libraries/Types/CodegenTypes';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

type LatLngZoom = Readonly<{ lat: Double; lng: Double; zoom: Double }>;
type Padding = Readonly<{
  left: Double;
  top: Double;
  right: Double;
  bottom: Double;
}>;

type GenericEvent = Readonly<{}>;

export interface NativeProps extends ViewProps {
  apiKey: string;
  baseUrl?: string;
  apiKeyAlert?: string;
  apiIDAlert?: string;
  shouldSimulateRoute?: boolean;
  initialLatLngZoom?: LatLngZoom;
  navigationZoomLevel?: Double;
  navigationTiltLevel?: Double;
  navigationPadding?: Padding;
  onRouteProgressChange?: DirectEventHandler<GenericEvent>;
  onCancelNavigation?: DirectEventHandler<GenericEvent>;
  onRouteBuilt?: DirectEventHandler<GenericEvent>;
  onMapLongClick?: DirectEventHandler<GenericEvent>;
  onMapMove?: DirectEventHandler<GenericEvent>;
  onMapMoveEnd?: DirectEventHandler<GenericEvent>;
  onNavigationFinished?: DirectEventHandler<GenericEvent>;
  onNavigationCancelled?: DirectEventHandler<GenericEvent>;
  onNavigationRunning?: DirectEventHandler<GenericEvent>;
  onRouteBuildFailed?: DirectEventHandler<GenericEvent>;
  onRouteBuilding?: DirectEventHandler<GenericEvent>;
  onMapReady?: DirectEventHandler<GenericEvent>;
  onMilestoneEvent?: DirectEventHandler<GenericEvent>;
  onUserOffRoute?: DirectEventHandler<GenericEvent>;
  onArrival?: DirectEventHandler<GenericEvent>;
  onNewRouteSelected?: DirectEventHandler<GenericEvent>;
  onMapClick?: DirectEventHandler<GenericEvent>;
}

export default codegenNativeComponent<NativeProps>(
  'VietMapNavigation'
) as HostComponent<NativeProps>;
