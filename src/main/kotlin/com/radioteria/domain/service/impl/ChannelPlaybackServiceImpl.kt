package com.radioteria.domain.service.impl

import com.radioteria.domain.entity.Channel
import com.radioteria.domain.repository.ChannelRepository
import com.radioteria.domain.service.ChannelPlaybackService
import com.radioteria.domain.service.NowPlayingService
import com.radioteria.service.core.TimeService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class ChannelPlaybackServiceImpl(
        val timeService: TimeService,
        val channelRepository: ChannelRepository,
        val nowPlayingService: NowPlayingService
) : ChannelPlaybackService {

    override fun startChannel(channel: Channel) {
        startChannelFromTimePosition(channel, 0)
    }

    override fun startChannelFromTimePosition(channel: Channel, timePosition: Long) {
        if (channel.isStarted()) {
            return
        }
        channel.startedAt = timeService.getTime() - timePosition
        channelRepository.save(channel)
    }

    override fun stopChannel(channel: Channel) {
        if (!channel.isStarted()) {
            return
        }
        channel.startedAt = null
        channelRepository.save(channel)
    }

    override fun seekChannel(channel: Channel, amount: Long) {
        if (!channel.isStarted()) {
            return
        }
        channelRepository.increaseStartedAt(channel.id, amount)
        refreshChannelStartedAt(channel)
    }

    override fun skipTrackOnChannel(channel: Channel) {
        val nowPlaying = nowPlayingService.getNowPlaying(channel)
        val seekAmount = nowPlaying.track.duration - nowPlaying.timePosition
        seekChannel(channel, seekAmount)
    }

    override fun rewindTrackOnChannel(channel: Channel) {
        val nowPlaying = nowPlayingService.getNowPlaying(channel)
        seekChannel(channel, -nowPlaying.timePosition)
    }

    private fun refreshChannelStartedAt(channel: Channel) {
        channelRepository.findOne(channel.id)
                ?.let { channel.startedAt = it.startedAt }
    }

}