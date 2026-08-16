import java.text.SimpleDateFormat
import java.util.Locale

data class TimeDifference(
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

fun calculateTimeDifference(
    checkIn: String,
    currentTime: String
): TimeDifference {

    val format = SimpleDateFormat(
        "dd-MM-yyyy HH:mm:ss",
        Locale.getDefault()
    )

    val checkInDate = format.parse(checkIn)
        ?: return TimeDifference(0, 0, 0)

    val currentDate = format.parse(currentTime)
        ?: return TimeDifference(0, 0, 0)

    val difference = currentDate.time - checkInDate.time

    val totalSeconds = difference / 1000

    return TimeDifference(
        hours = totalSeconds / 3600,
        minutes = (totalSeconds % 3600) / 60,
        seconds = totalSeconds % 60
    )
}