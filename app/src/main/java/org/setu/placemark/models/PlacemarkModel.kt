package org.setu.placemark.models

data class PlacedMark(
    var id:  Long = 0L,
    var title: String = "",
    var desc: String = "",
    var x: Double = 0.0,
    var y: Double = 0.0,
    var imageUri: String? = null
){
    fun validate() {
        require(title.isNotBlank()) {
            "Name must not be empty."
        }

        require(x.isFinite() && x in -90.0..90.0) {
            "Latitude must be between -90 and 90."
        }

        require(y.isFinite() && y in -180.0..180.0) {
            "Longitude must be between -180 and 180."
        }
    }
}