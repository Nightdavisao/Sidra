package paige.navic.domain.manager

import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode

expect class AudioGainManager {
	fun setPreAmp(value: Float)
	fun applyGainMode(metadata: DomainReplayGain, mode: ReplayGainMode)
	fun resetGain()
}
