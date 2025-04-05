package com.woosung.widgetpractice

object GridConverter {
    fun convertGRID_GPS(lat_X: Double, lng_Y: Double): Pair<Int, Int> {
        val RE = 6371.00877
        val GRID = 5.0
        val SLAT1 = 30.0
        val SLAT2 = 60.0
        val OLON = 126.0
        val OLAT = 38.0
        val XO = 43
        val YO = 136

        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)
        val rs = DoubleArray(2)

        var ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        var theta = lng_Y * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn
        rs[0] = (ra * Math.sin(theta) + XO + 0.5).toInt().toDouble()
        rs[1] = (ro - ra * Math.cos(theta) + YO + 0.5).toInt().toDouble()

        return Pair(rs[0].toInt(), rs[1].toInt())
    }
}
