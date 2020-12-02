//Class for testing javac and makefile.

import java.util.*;

public class TestAddClass {

    private int class_id;
    private int class_quatity;
    private TestAddClass2 addClass2;

    // constructor
    TestAddClass(int id, int quatity)
    {
        System.out.println("TestAddClass: new object created!");
        this.class_id = id;
        this.class_quatity = quatity;
        this.addClass2 = new TestAddClass2();
    }
    // default constructor
    TestAddClass(Random randomGenerator)
    {
        System.out.println("TestAddClass: new object created! " +
                "Using default constructor");
        this.class_id = randomGenerator.nextInt(65535);
        this.class_quatity = randomGenerator.nextInt(65535);
        this.addClass2 = new TestAddClass2();
    }



    public int getClass_id()
    {
        if (this.class_id <= 0)
        {
            System.out.println("Notice: id smaller than 0");
        }
        return this.class_id;
    }

    public int getClass_quatity()
    {
        if (this.class_quatity <= 0)
        {
            System.out.println("Notice: quatity smaller than 0");
        }
        return this.class_quatity;
    }

    public int getNumberfromTestAddClass2()
    {
        return this.addClass2.getRandomNumber();
    }


}
