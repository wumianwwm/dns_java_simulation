// Class for testing javac and Makefile.

import java.util.*;

public class TestAddClass2 {

    private int randomNumber;

    TestAddClass2()
    {
        Random randomGen = new Random();
        this.randomNumber = randomGen.nextInt(100);
    }

    public int getRandomNumber()
    {
        return this.randomNumber;
    }
}
