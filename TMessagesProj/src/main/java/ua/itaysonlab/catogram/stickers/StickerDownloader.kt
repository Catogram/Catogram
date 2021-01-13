package ua.itaysonlab.catogram.stickers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.telegram.messenger.FileLoader
import org.telegram.messenger.MediaController
import org.telegram.messenger.MessageObject
import java.io.File
import java.lang.Exception


class StickerDownloader(val message: MessageObject)  {
    fun download(): Pair<String, String> {
        val stickerPath = FileLoader.getPathToMessage(message.messageOwner).toString()
        val stickerName = message.fileName.replace(".webp", ".png")
        val stickerPngPath = stickerPath.replace(".webp", ".png")
        val stickerBitmap = BitmapFactory.decodeFile(stickerPath);

        File(stickerPngPath).outputStream().use {out ->
            stickerBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        stickerBitmap.recycle()

        return Pair(stickerName, stickerPngPath)
    }
}