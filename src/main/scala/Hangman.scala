import HangmanStrings._
import domain._
import zio._

import java.io.IOException

object Hangman extends ZIOAppDefault {
  private val MaxAttempts = 6
  private val FilePath = "src/main/resources/words.txt"

  def getUserInput(message: String): IO[IOException, String] =
    for {
      _     <- Console.printLine(message)
      input <- Console.readLine
    } yield input

  lazy val getName: IO[IOException, Name] =
    for {
      input <- getUserInput("What's your name? ")
      name  <- ZIO.from(Name.make(input)) <> (Console.printLine(tryAgainMessage) <*> getName)
    } yield name

  lazy val chooseWord: IO[IOException, Word] =
    for {
      words <- ZIO.readFile(FilePath).map(_.filter(_ != '\r')).map(_.split('\n'))
      index <- Random.nextIntBounded(words.length)
      word  <- ZIO.from(words.lift(index).flatMap(Word.make)).orDieWith(_ => new Error("!"))
    } yield word

  lazy val getGuess: IO[IOException, Guess] =
    for {
      input <- getUserInput("What's your next guess? ")
      guess <- ZIO.from(Guess.make(input)) <> (Console.printLine(tryAgainMessage) <*> getGuess)
    } yield guess

  def renderState(state: State): IO[IOException, Unit] = {
    val word = state.word.toList
      .map(c => if (state.guesses.map(_.char).contains(c)) s" $c " else "   ")
      .mkString
    val line = List.fill(state.word.length)(" - ").mkString
    val (goodG, badG) = splitGuesses(state.guesses.map(_.char), state.word.word)
    for {
      hangman <- ZIO.attempt(hangmanStages(state.failuresCount, MaxAttempts)).orDie
      _ <- Console.printLine(hangmanDisplay(hangman, word, line, goodG, badG))
    } yield ()
  }

  def analyzeNewGuess(oldState: State, newState: State, guess: Guess): GuessResult =
    if (oldState.guesses.contains(guess)) GuessResult.Unchanged
    else if (newState.playerWon) GuessResult.Won
    else if (newState.playerLost) GuessResult.Lost
    else if (oldState.word.contains(guess.char)) GuessResult.Correct
    else GuessResult.Incorrect

  def gameLoop(oldState: State): IO[IOException, Unit] =
    for {
      guess <- renderState(oldState) <*> getGuess
      newState = oldState.addGuess(guess)
      guessResult = analyzeNewGuess(oldState, newState, guess)
      (name, word) = (newState.name.name, newState.word.word)
      _ <- guessResult match {
        case GuessResult.Won =>
          Console.printLine(s"Congratulations, $name! You won!") <*> renderState(newState)
        case GuessResult.Lost =>
          Console.printLine(s"You Lost, $name! Word was: $word") <*> renderState(newState)
        case GuessResult.Correct =>
          Console.printLine(s"Good guess, $name!") <*> gameLoop(newState)
        case GuessResult.Incorrect =>
          Console.printLine(s"Bad guess, $name!") <*> gameLoop(newState)
        case GuessResult.Unchanged =>
          Console.printLine(s"$name, you've already tried this letter!") <*> gameLoop(newState)
      }
    } yield ()

  override val run: IO[IOException, Unit] =
    for {
      name <- Console.printLine("Welcome to ZIO Hangman!") <*> getName
      word <- chooseWord
      _    <- gameLoop(State.initial(name, word))
    } yield ()
}
