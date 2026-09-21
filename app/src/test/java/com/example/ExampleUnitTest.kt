package com.example

import com.example.data.model.TargetRule
import com.example.data.model.TargetType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun macWildcard_5thOctet_matchesCorrectly() {
    val pattern = "**:**:**:**:94:**"

    // Matches when 5th octet is 94
    assertTrue(TargetRule.matchesMac(pattern, "AA:BB:CC:DD:94:EE"))
    assertTrue(TargetRule.matchesMac(pattern, "00:11:22:33:94:55"))
    assertTrue(TargetRule.matchesMac(pattern, "aa:bb:cc:dd:94:ee"))
    assertTrue(TargetRule.matchesMac(pattern, "AA-BB-CC-DD-94-EE"))

    // Does NOT match when 94 is in another octet
    assertFalse(TargetRule.matchesMac(pattern, "AA:BB:CC:94:DD:EE")) // 4th octet
    assertFalse(TargetRule.matchesMac(pattern, "94:BB:CC:DD:EE:FF")) // 1st octet
    assertFalse(TargetRule.matchesMac(pattern, "AA:BB:CC:DD:EE:94")) // 6th octet
    assertFalse(TargetRule.matchesMac(pattern, "AA:94:CC:DD:EE:FF")) // 2nd octet
  }

  @Test
  fun macWildcard_variations_matchCorrectly() {
    // Single asterisk per octet
    assertTrue(TargetRule.matchesMac("*:*:*:*:94:*", "11:22:33:44:94:55"))
    assertFalse(TargetRule.matchesMac("*:*:*:*:94:*", "11:22:33:94:44:55"))

    // Prefix match (OUI vendor)
    assertTrue(TargetRule.matchesMac("00:1A:2B:*:*:*", "00:1A:2B:11:22:33"))
    assertTrue(TargetRule.matchesMac("00:1A:2B:*", "00:1a:2b:ff:ee:dd"))
    assertFalse(TargetRule.matchesMac("00:1A:2B:*:*:*", "00:1A:3C:11:22:33"))

    // Suffix match (last octet)
    assertTrue(TargetRule.matchesMac("*:*:*:*:*:94", "AA:BB:CC:DD:EE:94"))
    assertFalse(TargetRule.matchesMac("*:*:*:*:*:94", "AA:BB:CC:DD:94:EE"))
  }

  @Test
  fun targetRule_matchesWifiAndBluetoothMac() {
    val rule = TargetRule(
      rawPattern = "**:**:**:**:94:**",
      targetType = TargetType.ANY
    )

    // Matches WiFi BSSID
    assertTrue(rule.matches(bssid = "00:11:22:33:94:55"))
    // Matches Bluetooth MAC
    assertTrue(rule.matches(btAddress = "AA:BB:CC:DD:94:FF"))
    // Does not match non-matching MAC
    assertFalse(rule.matches(bssid = "00:11:22:94:33:55"))
  }
}

