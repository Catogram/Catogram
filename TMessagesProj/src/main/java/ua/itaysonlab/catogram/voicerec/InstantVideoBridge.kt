package ua.itaysonlab.catogram.voicerec

import android.media.AudioFormat
import android.os.Build
import ua.itaysonlab.catogram.CatogramConfig

object InstantVideoBridge {
    @JvmStatic
    fun getInstantBitrate(): Int {
        return when {
            Build.MODEL.startsWith("zeroflte") || Build.MODEL.startsWith("zenlte") -> 600000
            CatogramConfig.hqRoundVideos -> 600000
            else -> 400000
        }
    }

    @JvmStatic
    fun getInstantResolution(): Int {
        return when {
            Build.MODEL.startsWith("zeroflte") || Build.MODEL.startsWith("zenlte") -> 320
            CatogramConfig.hqRoundVideos -> 480
            else -> 240
        }
    }

    @JvmStatic
    fun getInstantAudioBitrate(): Int {
        return if (CatogramConfig.hqRoundVideoAudio) 64000 else 32000
    }

    @JvmStatic
    fun getVoiceBitrate(): Int {
        return if (CatogramConfig.hqRoundVideoAudio) 32000 else 25000
    }

    @JvmStatic
    fun getInstantAudioChannelCount(): Int {
        return if (CatogramConfig.stereoVoices) 2 else 1
    }

    @JvmStatic
    fun getInstantAudioChannelType(): Int {
        return if (CatogramConfig.stereoVoices) AudioFormat.CHANNEL_IN_STEREO else AudioFormat.CHANNEL_IN_MONO
    }
}