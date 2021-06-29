package ua.itaysonlab.catogram.voicerec

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
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
        return 1
    }

    @JvmStatic
    fun getInstantAudioChannelType(): Int {
        return AudioFormat.CHANNEL_IN_MONO
    }

    // Voices AGC/NS

    private var agc: AutomaticGainControl? = null
    private var ns: NoiseSuppressor? = null
    private var aec: AcousticEchoCanceler? = null

    @JvmStatic
    fun initVoiceEnhancements(audioRecord: AudioRecord) {
        if (!CatogramConfig.voicesAgc) return

        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(audioRecord.audioSessionId)
            agc!!.enabled = true
        }

        if (NoiseSuppressor.isAvailable()) {
            ns = NoiseSuppressor.create(audioRecord.audioSessionId)
            ns!!.enabled = true
        }

        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(audioRecord.audioSessionId)
            aec!!.enabled = true
        }
    }

    @JvmStatic
    fun releaseVoiceEnhancements() {
        agc?.release()
        ns?.release()
        aec?.release()

        agc = null
        ns = null
        aec = null
    }
}