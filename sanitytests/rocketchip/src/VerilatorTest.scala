package sanitytests.rocketchip

import utest._

/** software dependencies:
  * clang -> bootrom cross compiling / veriltor C compiling
  * verilator -> emulator generation
  * cmake -> simulation
  * ninja -> fast verilator build tool
  * spike -> isa behavior model linking in emulator
  */
object VerilatorTest extends TestSuite {
  val outputDirectory = os.pwd / "out" / "VerilatorTest"
  os.remove.all(outputDirectory)
  os.makeDir(outputDirectory)
  val tests = Tests {
    test("build TestHarness emulator") {
      val testHarness = classOf[openrigil.OpenRigilTestHarness]
      val configs = Seq(
        classOf[openrigil.OpenRigilConfig])
      val emulator = TestHarness(testHarness, configs, Some(outputDirectory)).emulator
    }
  }
}
