package me.lenglet.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

class SensitiveStringCheckTest {

  @Test
  void test() {
    CheckVerifier.newVerifier()
      .onFile("src/test/files/SensitiveStringCheck.java")
      .withCheck(new SensitiveStringCheck())
      .verifyIssues();
  }

}
