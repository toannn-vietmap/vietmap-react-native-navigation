import * as React from 'react';
import { StyleSheet } from 'react-native';
import RNVietMapNavigation from './VietMapNavigationNativeComponent';
const VietMapNavigation = (props) => {
    return <RNVietMapNavigation style={styles.container} {...props}/>;
};
const styles = StyleSheet.create({
    container: {
        flex: 1
    },
});
// export * from './models/route_progress_model'
export * from './enums/vietmap_event_type';
export * from './models/route_progress_data';
export { VietMapNavigationModule } from './native_modules';
export * from './models/coordinates';
export * from './typings';
export { VietMapNavigationController, VehicleType } from './controller';
export default VietMapNavigation;
