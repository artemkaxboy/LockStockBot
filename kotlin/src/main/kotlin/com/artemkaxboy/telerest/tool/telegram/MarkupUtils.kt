package com.artemkaxboy.telerest.tool.telegram

import com.artemkaxboy.telerest.tool.Emoji
import com.artemkaxboy.telerest.tool.StringUtils.times
import com.elbekD.bot.types.InlineKeyboardButton as IKB
import com.elbekD.bot.types.InlineKeyboardMarkup as IKM
import org.springframework.data.domain.Page
import kotlin.math.max
import kotlin.math.min

object MarkupUtils {

    fun getListPageMarkup(page: Page<Pair<String, String>>, callbackDataPrefix: String): IKM {

        val list = page.map { listOf(IKB(it.second, callback_data = "$callbackDataPrefix:${it.first}")) }

        val pager = getPagerKeyboard(page, callbackDataPrefix)

        return IKM(list + pager)
    }

    private fun getPagerKeyboard(page: Page<Pair<String, String>>, callbackDataPrefix: String): List<List<IKB>> {

        val zeroBasedPage = page.number
        val oneBasedPage = zeroBasedPage + 1

        val zeroBasedFirstPage = 0
        val zeroBasedLastPage = page.totalPages - 1

        val pagesCount = page.totalPages

        val pageIndicator = listOf(IKB("$oneBasedPage / $pagesCount", callback_data = callbackDataPrefix))

        val left = page.takeUnless { it.isFirst }?.let {
            val previous = zeroBasedPage - 1
            val tenBack = max(zeroBasedFirstPage, zeroBasedPage - 10)
            listOf(
                IKB(Emoji.FINGER_LEFT * 2, callback_data = "$callbackDataPrefix:page=$tenBack"),
                IKB(Emoji.FINGER_LEFT, callback_data = "$callbackDataPrefix:page=$previous")
            )
        } ?: emptyList()

        val right = page.takeUnless { it.isLast }?.let {
            val next = zeroBasedPage + 1
            val tenForward = min(zeroBasedLastPage, zeroBasedPage + 10)
            listOf(
                IKB(Emoji.FINGER_RIGHT, callback_data = "$callbackDataPrefix:page=$next"),
                IKB(Emoji.FINGER_RIGHT * 2, callback_data = "$callbackDataPrefix:page=$tenForward")
            )
        } ?: emptyList()

        return listOf(left + pageIndicator + right)
    }
}
