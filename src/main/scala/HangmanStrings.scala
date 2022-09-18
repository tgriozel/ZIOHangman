object HangmanStrings {

  val tryAgainMessage = "Invalid input. Please try again..."

  def hangmanStages(failuresCount: Int, maxAttempts: Int): String =
    if (failuresCount < 0 || failuresCount > maxAttempts) throw new IllegalArgumentException()
    else s"Remaining attempts: >>> ${maxAttempts - failuresCount} <<<"

  def splitGuesses(guesses: Set[Char], word: String): (String, String) = {
    val (goodChars, badChars) = guesses.partition(word.contains(_))
    val goodG = s"Good guesses: ${goodChars.mkString(", ")}"
    val badG = s"Bad guesses: ${badChars.mkString(", ")}"
    (goodG, badG)
  }

  def hangmanDisplay(hangman: String, word: String, line: String, goodG: String, badG: String): String =
    s"""
       #$hangman
       #
       #$word
       #$line
       #
       #$goodG
       #$badG
       #
       #""".stripMargin('#')

}
