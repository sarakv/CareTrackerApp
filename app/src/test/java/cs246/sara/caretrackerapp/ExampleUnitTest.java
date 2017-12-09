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
    public void buttonLabelSetterAndGetterWork() {
        ButtonInfo ButtIn = new ButtonInfo();
        ButtIn.setLabel("my but-ton brings all the boys to the yard");
        assert( ButtIn.getLabel() == "my but-ton brings all the boys to the yard");
    }

    @Test
    public void buttonIdSetterAndGetterWork() {
        ButtonInfo ButtIn = new ButtonInfo();
        ButtIn.setId(42);
        assert( ButtIn.getId() == 42);
    }

    @Test
    public void buttonColorSetterAndGetterWork() {
        ButtonInfo ButtIn = new ButtonInfo();
        ButtIn.setColor(246246);
        assert( ButtIn.getColor() == 246246);
    }

    @Test
    public void sheetDataConstructorSetsGetValuesRetrieves() {
        SheetData test = new SheetData("Tom Bombadil", "18:00:59",
                "Don't label me", "This is a test",
                "Let's get ice cream",
                "I'm lactose intolarant fool!", "image.png");
        String[] testResults = test.getValues();
        for (int i = 0; i < testResults.length; ++i){
            System.out.println(testResults[i]);
        }
    }
}