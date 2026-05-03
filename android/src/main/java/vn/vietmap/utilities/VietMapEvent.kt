package vn.vietmap.utilities

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class VietMapEvent(
    surfaceId: Int,
    viewId: Int,
    private val eventNameStr: String,
    private val eventDataMap: WritableMap?
) : Event<VietMapEvent>(surfaceId, viewId) {
    override fun getEventName(): String = eventNameStr
    override fun getEventData(): WritableMap? = eventDataMap
}
