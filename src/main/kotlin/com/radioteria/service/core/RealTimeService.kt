package com.radioteria.service.core

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@ConditionalOnProperty("radioteria.time.service", havingValue = "real")
@Service
class RealTimeService : TimeService {
    override fun getTime(): Long = System.currentTimeMillis()
}
