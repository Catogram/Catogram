package ua.itaysonlab.catogram

object CustomVerifications {
    // that man requested to have a scam verification mark xddddddd
    val SCAM_IDS = listOf("1114839809")

    @JvmStatic
    fun isScam(id: Int) = SCAM_IDS.contains(id.toString())
}