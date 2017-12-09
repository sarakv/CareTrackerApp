package cs246.sara.caretrackerapp;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        assertEquals(5, 2 + 3);
    }

    @Test
    public void buttonSettersWork() {
        ButtonInfo ButtIn = new ButtonInfo();
        ButtIn.setLabel("my but-ton brings all the boys to the yard");
        assert(ButtIn.getLabel() == "my but-ton brings all the boys to the yard");
    }
}