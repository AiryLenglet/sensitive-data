package me.lenglet.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

class SensitiveStringValueCheckTest {

  @Test
  void test() {
    CheckVerifier.newVerifier()
      .onFile("src/test/files/SensitiveStringValueCheck.java")
      .withCheck(new SensitiveStringValueCheck())
      .verifyIssues();
  }

}
