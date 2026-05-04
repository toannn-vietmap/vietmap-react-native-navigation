import * as React from 'react';
import { StyleSheet } from 'react-native';

import { IVietMapNavigationProps } from './typings';
import RNVietMapNavigation from './VietMapNavigationNativeComponent';

const VietMapNavigation = (props: IVietMapNavigationProps) => {
  return <RNVietMapNavigation style={styles.container} {...(props as any)} />;
};

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
});
// export * from './models/route_progress_model'
export * from './enums/vietmap_event_type';
export * from './models/route_progress_data';
export {VietMapNavigationModule} from './native_modules';
export * from './models/coordinates';
export * from './typings';
export {VietMapNavigationController, VehicleType} from './controller';
export default VietMapNavigation;
