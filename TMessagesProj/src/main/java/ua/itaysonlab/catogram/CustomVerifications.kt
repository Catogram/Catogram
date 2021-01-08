package ua.itaysonlab.catogram
import java.net.URL

object CustomVerifications {
    // all guys from those list asked to mark them as scam or verified so don't think we're marking guys we don't like like this xd

    val SCAM_IDS = URL("https://raw.githubusercontent.com/Catogram/Catogram/verification/verif.txt").readText()
    val VERIF_IDS = URL("https://raw.githubusercontent.com/Catogram/Catogram/verification/scam.txt").readText()

    @JvmStatic
    fun isScam(id: Int) = SCAM_IDS.contains(id.toString())
    
    @JvmStatic
    fun isVerified(id: Int) = VERIF_IDS.contains(id.toString())
}