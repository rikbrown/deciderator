package codes.rik.deciderator.cmd
import codes.rik.deciderator.types.CoinStyle
import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.OptionName
import codes.rik.deciderator.types.Round
import codes.rik.deciderator.types.SessionId
import codes.rik.deciderator.types.UncertaintyId
import codes.rik.deciderator.types.Username
import me.ntrrgc.tsGenerator.TypeScriptGenerator
import java.time.Duration

fun main() {
  println(
    TypeScriptGenerator(
      rootClasses = Messages::class.nestedClasses.toSet() + Round.RoundData::class.nestedClasses.toSet(),
      mappings = mapOf(
        UncertaintyId::class to "string",
        SessionId::class to "string",
        Username::class to "string",
        OptionName::class to "string",
        CoinStyle::class to "string",
        Duration::class to "number",
      )
  ).definitionsText)
}
