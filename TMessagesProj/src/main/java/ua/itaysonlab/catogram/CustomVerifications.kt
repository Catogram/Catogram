package ua.itaysonlab.catogram
import com.google.android.exoplayer2.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.URL
import ua.itaysonlab.catogram.CGControversive;

object CustomVerifications: CoroutineScope by MainScope() {
    // all guys from those list asked to mark them as scam or verified so don't think we're marking guys we don't like like this xd

    var SCAM_IDS = listOf<String>()
    var VERIF_IDS = listOf<String>()
    var FAKE_IDS = listOf<String>()

    init {
        launch(Dispatchers.IO) {
            try {
                SCAM_IDS = URL("https://raw.githubusercontent.com/Catogram/Catogram/verification/scam.txt").readText().lines()
                VERIF_IDS = URL("https://raw.githubusercontent.com/Catogram/Catogram/verification/verif.txt").readText().lines()
                FAKE_IDS = URL("https://raw.githubusercontent.com/Catogram/Catogram/verification/fake.txt").readText().lines()
            }
            catch (e: Exception) { }
        }
    }

    @JvmStatic
    fun isScam(id: Int): Boolean {
        if (!CGControversive.isControversiveFeaturesEnabled()) return false
        return SCAM_IDS.contains(id.toString())
    }

    @JvmStatic
    fun isVerified(id: Int): Boolean {
        if (!CGControversive.isControversiveFeaturesEnabled()) return false
        return VERIF_IDS.contains(id.toString())
    }

    @JvmStatic
    fun isFake(id: Int): Boolean {
        if (!CGControversive.isControversiveFeaturesEnabled()) return false
        return FAKE_IDS.contains(id.toString())
    }
}