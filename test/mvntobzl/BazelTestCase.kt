package mvntobzl

import org.junit.Test
import kotlin.test.assertEquals

class BazelTestCase {

    @Test
    fun `toBazelPath maps artifact special characters to underscores`() {
        assertEquals("org_junit_junit_5", toBazelPath("org.junit:junit-5"))
    }
}
