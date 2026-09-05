package paige.navic.exoplayer

import android.media.AudioFormat
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.StreamMetadata
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode
import paige.navic.util.core.decibelsToLinear
import paige.navic.util.core.effectiveGain
import java.nio.ByteBuffer
import java.nio.ByteOrder


@UnstableApi
class AudioGainProcessor : BaseAudioProcessor() {
	private companion object {
		const val DEFAULT_GAIN = 1f
	}

	private val finalVolume: Float
		get() = (volume + amplifierValue).decibelsToLinear()

	private var volume = DEFAULT_GAIN
		set(value) {
			field = value
			flush(StreamMetadata.DEFAULT)
		}

	var amplifierValue = 0f
		set(value) {
			field = value
			flush(StreamMetadata.DEFAULT)
		}

	fun applyGainMode(metadata: DomainReplayGain, mode: ReplayGainMode) {
		volume = metadata.effectiveGain(mode)
	}

	fun resetGain() {
		volume = DEFAULT_GAIN
	}

	override fun isActive(): Boolean {
		return super.isActive() && finalVolume != DEFAULT_GAIN
	}

	override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
		if (inputAudioFormat.encoding == AudioFormat.ENCODING_PCM_16BIT) {
			return inputAudioFormat
		}
		throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
	}

	override fun queueInput(inputBuffer: ByteBuffer) {
		val pos = inputBuffer.position()
		val limit = inputBuffer.limit()
		val outputBuffer = replaceOutputBuffer(limit - pos)

		inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
		outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

		val computedVolume = finalVolume

		if (computedVolume == 1f) {
			outputBuffer.put(inputBuffer)
		} else {
			val shortBufferInput = inputBuffer.asShortBuffer()
			val shortBufferOutput = outputBuffer.asShortBuffer()

			while (shortBufferInput.hasRemaining()) {
				// prevent popping
				val sample = shortBufferInput.get()
				val scaledSample = (sample * computedVolume)
					.toInt()
					.coerceAtLeast(Short.MIN_VALUE.toInt())
					.coerceAtMost(Short.MAX_VALUE.toInt())
					.toShort()

				shortBufferOutput.put(scaledSample)
			}
			inputBuffer.position(inputBuffer.position() + shortBufferInput.position() * 2)
			outputBuffer.position(outputBuffer.position() + shortBufferOutput.position() * 2)
		}
		outputBuffer.flip()
	}
}
