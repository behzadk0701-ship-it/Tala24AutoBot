
package com.tala24.autobot

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.text.DecimalFormat
import java.util.*

class AutoSendWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {

    // TODO: اینجا توکن ربات خودت را بگذار
    private val TOKEN = "8447024292:AAG3HbCGFNIkNfMfWwoFy50mAn-YQstLqNM"
    private val CHANNEL = "@Tala24_B"
    private val client = OkHttpClient()

    override fun doWork(): Result {
        return try {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            // فقط روی ساعت‌های فرد (۱،۳،۵،...،۲۳)
            if (hour % 2 == 1) {
                val msg = buildMessage()
                sendMessage(msg)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun fetchFromTable(url: String, key: String): Double? {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get()

            val rows = doc.select("tr")
            for (row in rows) {
                val th = row.selectFirst("th")?.text()?.trim()
                val td = row.selectFirst("td.nf")?.text()?.trim()

                if (th != null && td != null && th.contains(key)) {
                    val cleaned = td.replace(",", "").replace("٬", "")
                    return cleaned.toDoubleOrNull()
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun fmtRial(v: Double?): String {
        if (v == null) return " "
        return DecimalFormat("#,###").format(v)
    }

    private fun fmtUsd(v: Double?): String {
        if (v == null) return " "
        return DecimalFormat("#,###.##").format(v)
    }

    private fun buildMessage(): String {
        val usd = fetchFromTable("https://www.tgju.org/currency", "دلار")
        val ounce = fetchFromTable("https://www.tgju.org/gold-global", "انس طلا")
        val brent = fetchFromTable("https://www.tgju.org/energy", "نفت برنت")

        val gold18 = fetchFromTable("https://www.tgju.org/gold-chart", "طلای 18 عیار")
        val gold24 = fetchFromTable("https://www.tgju.org/gold-chart", "طلای ۲۴ عیار")
        val goldUsed = fetchFromTable("https://www.tgju.org/gold-chart", "طلای دست دوم")

        val coinImami = fetchFromTable("https://www.tgju.org/coin", "سکه امامی")
        val coinBahar = fetchFromTable("https://www.tgju.org/coin", "سکه بهار آزادی")
        val coinHalf = fetchFromTable("https://www.tgju.org/coin", "نیم سکه")
        val coinQuarter = fetchFromTable("https://www.tgju.org/coin", "ربع سکه")

        val silver = fetchFromTable("https://www.tgju.org/gold-chart", "نقره")

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        val date = "%04d/%02d/%02d".format(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        val time = "%02d:%02d".format(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        return """
🗓 $date
⏰ $time
━━━━━━━━━━━━━━━━━━━━━
💵 دلار تهران : <b>${fmtRial(usd)}</b> ریال
━━━━━━━━━━━━━━━━━━━━━
💰 انس : <b>${fmtUsd(ounce)}</b> $

🛢 نفت برنت : <b>${fmtUsd(brent)}</b> $
━━━━━━━━━━━━━━━━━━━━━
🟡 طلای ۱۸ عیار : <b>${fmtRial(gold18)}</b> ریال

🟡 طلای ۲۴ عیار : <b>${fmtRial(gold24)}</b> ریال

🟡 طلای دست دوم : <b>${fmtRial(goldUsed)}</b> ریال
━━━━━━━━━━━━━━━━━━━━━
🔶 سکه امامی : <b>${fmtRial(coinImami)}</b> ریال

🔶 سکه بهار آزادی : <b>${fmtRial(coinBahar)}</b> ریال

🔶 نیم سکه : <b>${fmtRial(coinHalf)}</b> ریال

🔶 ربع سکه : <b>${fmtRial(coinQuarter)}</b> ریال
━━━━━━━━━━━━━━━━━━━━━
⚪️ نقره : <b>${fmtRial(silver)}</b> ریال

📊 @Tala24_B
        """.trimIndent()
    }

    private fun sendMessage(text: String) {
        val url = "https://api.telegram.org/bot$TOKEN/sendMessage"

        val form = FormBody.Builder()
            .add("chat_id", CHANNEL)
            .add("text", text)
            .add("parse_mode", "HTML")
            .build()

        val req = Request.Builder()
            .url(url)
            .post(form)
            .build()

        client.newCall(req).execute()
    }
}
