package org.setu.placemark

import org.setu.placemark.models.PlacedMark

interface OnMarkListener {
    fun onMarkClick(mark: PlacedMark)
    fun onMarkLongClick(mark: PlacedMark): Boolean
}