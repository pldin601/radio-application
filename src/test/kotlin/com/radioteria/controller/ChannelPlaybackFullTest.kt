package com.radioteria.controller

import org.junit.Test

import org.hamcrest.CoreMatchers.*
import org.junit.Ignore
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.LinkedMultiValueMap

class ChannelPlaybackFullTest : AbstractControllerTest() {

    val channelId = 7
    val channelOwner = "user@mail.com"

    fun getNowPlayingUrl() = "/api/public/channel/$channelId/now"
    fun getChannelActionUrl(action: String) = "/api/channel/$channelId/control/$action"

    @Test
    fun testEverything() {
        assertThatChannelIsStopped()
        performAction("start")
        verifyNowPlaying(trackId = 3, timePosition = 0L)
        performAction("seek", mapOf("amount" to "1000"))
        verifyNowPlaying(trackId = 3, timePosition = 1000L)
        performAction("seek", mapOf("amount" to "3000"))
        verifyNowPlaying(trackId = 3, timePosition = 4000L)
        performAction("skip")
        verifyNowPlaying(trackId = 4, timePosition = 0L)
        performAction("seek", mapOf("amount" to "1000"))
        verifyNowPlaying(trackId = 4, timePosition = 1000L)
        performAction("rewind")
        verifyNowPlaying(trackId = 4, timePosition = 0L)
        performAction("skip")
        verifyNowPlaying(trackId = 5, timePosition = 0L)
        performAction("skip")
        verifyNowPlaying(trackId = 3, timePosition = 0L)
        performAction("stop")
        assertThatChannelIsStopped()
    }

    private fun verifyNowPlaying(trackId: Long, timePosition: Long) {
        getNowPlaying()
                .andExpect(jsonPath("$.timePosition").value(timePosition))
                .andExpect(jsonPath("$.track.id").value(trackId))
    }

    private fun performAction(action: String, params: Map<String, String> = mapOf()) {
        val multivaluedMap = LinkedMultiValueMap(params.mapValues { listOf(it.value) })

        mvc.perform(post("/api/channel/$channelId/control/$action")
                .with(user(getUserDetails(channelOwner)))
                .params(multivaluedMap))

                .andExpect(status().isOk)
    }

    private fun assertThatChannelIsStopped() {
        mvc.perform(get(getNowPlayingUrl()))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(containsString("is not started")))
    }

    private fun getNowPlaying(): ResultActions {
        return mvc.perform(get(getNowPlayingUrl()))
                .andExpect(status().isOk)
    }

}