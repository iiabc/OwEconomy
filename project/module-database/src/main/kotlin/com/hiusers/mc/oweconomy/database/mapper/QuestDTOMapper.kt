package com.hiusers.questengine.database.mapper

import com.hiusers.questengine.api.dto.QuestDTO
import com.hiusers.questengine.database.entity.QuestEntity

/**
 * @author iiabc
 * @since 2025/9/3 00:40
 */
object QuestDTOMapper {

    fun toDTOList(questEntities: List<QuestEntity>): List<QuestDTO> {
        return questEntities.map { it.toDTO() }
    }

}


