package app.simple.inure.updatehistory.model

enum class DeltaSeverity(val rank: Int) {
    INFO(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    companion object {
        fun highest(values: Iterable<DeltaSeverity>): DeltaSeverity {
            return values.maxByOrNull { it.rank } ?: INFO
        }
    }
}
