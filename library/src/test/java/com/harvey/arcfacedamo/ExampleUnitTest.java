package com.harvey.arcfacedamo;

import org.junit.Test;

import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
        byte[] nv21 = new byte[]{1, 2, 3, 4, 5};
        byte[] temp = nv21.clone();
//        byte[] temp = nv21;
        temp[0] = 3;
        temp[1] = 3;
        temp[2] = 3;
        temp[3] = 3;
        temp[4] = 3;
        System.out.println(Arrays.toString(nv21));
        System.out.println(Arrays.toString(temp));
    }
}