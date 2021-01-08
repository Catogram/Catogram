package ua.itaysonlab.catogram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.URL

object CustomVerifications: CoroutineScope by MainScope() {
    // all guys from those list asked to mark them as scam or verified so don't think we're marking guys we don't like like this xd

    var SCAM_IDS = listOf<String>()
    var VERIF_IDS = listOf<String>()

    init {
        launch(Dispatchers.IO) {
            SCAM_IDS = URL("https://raw.githubusercontent.com/Catogram/Catogram/verification/scam.txt").readText().lines()
            VERIF_IDS = URL("https://raw.githubusercontent.com/Catogram/Catogram/verification/verif.txt").readText().lines()
        }
    }

    @JvmStatic
    fun isScam(id: Int) = SCAM_IDS.contains(id.toString())

    @JvmStatic
    fun isVerified(id: Int) = VERIF_IDS.contains(id.toString())
}