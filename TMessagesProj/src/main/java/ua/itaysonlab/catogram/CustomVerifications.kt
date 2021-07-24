package ua.itaysonlab.catogram

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.URL

object CustomVerifications: CoroutineScope by MainScope() {

    var IDS = listOf<String>()

    init {
        launch(Dispatchers.IO) {
            try {
                IDS = URL("https://raw.githubusercontent.com/ctwoon/galo4ki/main/verif.txt").readText().lines()
            }
            catch (e: Exception) { }
        }
    }

    @JvmStatic
    fun isPremium(id: Int): Boolean {
        return IDS.contains(id.toString())
    }
}