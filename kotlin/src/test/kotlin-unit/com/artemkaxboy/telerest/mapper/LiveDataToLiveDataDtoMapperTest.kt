package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.config.ModelMapperConfig
import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.entity.LiveData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.random.Random
import kotlin.reflect.full.memberProperties

class LiveDataToLiveDataDtoMapperTest {

    private val modelMapper = ModelMapperConfig().getModelMapper()

    private val liveDataToLiveDataDtoMapper = LiveDataToLiveDataDtoMapper(modelMapper).apply {
        setupMapper()
    }

    @Test
    fun `fail to convert map to dto`() {

        val liveDataDto = requireNotNull(
            liveDataToLiveDataDtoMapper.toDto(
                LiveData.random().copy(date = LocalDate.now().minusDays(Random.nextLong(365)))
            )
        )

        val map = LiveDataDto::class.memberProperties
            .mapNotNull { key ->
                key.get(liveDataDto)?.let { key.name to it.toString() }
            }
            .toMap()

        Assertions.assertThat(liveDataToLiveDataDtoMapper.toDto(map)).isEqualTo(liveDataDto)
    }
}
