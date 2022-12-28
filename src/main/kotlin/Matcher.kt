import java.io.File
import kotlin.math.abs

class Matcher {

    val menteesFile = this::class.java.getResource("/mentees.tsv").path
    val mentorsFile = this::class.java.getResource("/mentors.tsv").path
    val minimumMatch = 3
    val maxAssignedMentorsCount = 1
    val maxAssignedMentees = 2
    fun match() {
        val mentees = File(menteesFile)
            .readLines()
            .filter { !it.contains("City/ Country") }
            .mapIndexed { index, it ->  it.split("\t").let { extractPerson(it, index) } }
        val mentors = File(mentorsFile)
            .readLines()
            .filter { !it.contains("City/ Country") }
            .mapIndexed { index, it -> it.split("\t").let { extractPerson(it, index) } }

        val matchers = listOf(
            ageMatcher,
            englishMatcher,
            interestMatcher
        )
        val association = mentors.associateWith { mentor ->
            var menteesCount = 0
            mentees.filter { mentee ->
                val canBeTaken = matchers.fold(0){acc, matchFilter ->
                    acc + matchFilter.match(mentee, mentor)
                } >= minimumMatch && mentee.taken < maxAssignedMentorsCount && menteesCount < maxAssignedMentees
                if(canBeTaken) {mentee.taken ++; menteesCount++; true}
                else false
            }
        }
        association.forEach {
            println("Mentor ${it.key.getInfo()} is associated with ${it.value.map { "${it.getInfo()}"}}")
        }
        val notAssigned = mentees - association.values.flatMap { it }.toSet()
        println("Not assigned mentees are:")
        notAssigned.forEach{
            println(it.getInfo())
        }
    }

    private fun extractPerson(it: List<String>, index: Int) =
        Person(it[1], it[2], it[3].toInt(), it[4], it[5].toInt(), it[6].split(","), index)
}

val ageMatcher = MatchFilter { mentee, mentor ->
    if(mentor.age < 22) if (abs(mentee.age - mentor.age) <= 1) 1 else 0
    else if (abs(mentee.age - mentor.age) <= 2) 1 else 0
}
val englishMatcher = MatchFilter { mentee, mentor -> if (mentor.englishLevel >= mentee.englishLevel) 1 else 0 }
val interestMatcher = MatchFilter { mentee, mentor -> if (mentor.interest.intersect(mentee.interest.toSet()).isNotEmpty()) 1 else 0 }

fun interface MatchFilter {
    fun match(mentee: Person, mentor: Person): Int
}

data class Person(
    val name: String,
    val surname: String,
    val age: Int,
    val from: String,
    val englishLevel: Int,
    val interest: List<String>,
    val index: Int,
    var taken: Int = 0
) {
    fun getInfo() = "$index - $name $surname"
}