import org.gradle.api.GradleException
import java.util.regex.Pattern

object PluginVersions {
    /**
     * Increments the last integer within the given string.
     */
    @JvmStatic
    fun increment(previous: String): String {
        val matcher = Pattern.compile("(.*\\D)(\\d+)(\\D*)").matcher(previous)
        if (matcher.matches()) {
            val incrementedNumber = Integer.parseInt(matcher.group(2)) + 1
            return matcher.group(1) + incrementedNumber + matcher.group(3)
        } else {
            throw GradleException("Unsupported version: $previous")
        }
    }
}
