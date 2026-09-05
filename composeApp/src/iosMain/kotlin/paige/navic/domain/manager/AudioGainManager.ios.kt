package paige.navic.domain.manager

import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode

actual class AudioGainManager {
    actual fun applyGainMode(
        metadata: DomainReplayGain,
        mode: ReplayGainMode
    ) {
    }

    actual fun resetGain() {
    }

	actual fun setPreAmp(value: Float) {
	}
}
