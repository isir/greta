/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation.rbdl;

import greta.core.animation.math.Matrix4d;
import greta.core.animation.math.Vector4d;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jing Huang
 */
public class Matrix4dTest {

    public Matrix4dTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of set method, of class Matrix4d.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 11.0;
        double v4 = 0.0;
        double v5 = 0.0;
        double v6 = 0.0;
        double v7 = 8.0;
        double v8 = 0.0;
        double v9 = 0.0;
        double v10 = 0.0;
        double v11 = 0.0;
        double v12 = 0.0;
        double v13 = 10.0;
        double v14 = 0.0;
        double v15 = 0.0;
        double v16 = 0.0;
        Matrix4d instance = new Matrix4d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("set test: " + instance);
    }

    /**
     * Test of toIdentity method, of class Matrix4d.
     */
    @Test
    public void testToIdentity() {
        System.out.println("toIdentity");
        Matrix4d instance = new Matrix4d();
        instance.toIdentity();
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("toIdentity test: " + instance);
    }

    /**
     * Test of toMIdentity method, of class Matrix4d.
     */
    @Test
    public void testToMIdentity() {
        System.out.println("toMIdentity");
        double m = 11.0;
        Matrix4d instance = new Matrix4d();
        instance.toMIdentity(m);
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("toMIdentity test: " + instance);
    }

    /**
     * Test of toZero method, of class Matrix4d.
     */
    @Test
    public void testToZero() {
        System.out.println("toZero");
        Matrix4d instance = new Matrix4d();
        instance.toZero();
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("toZero test: " + instance);
    }

    /**
     * Test of copyData method, of class Matrix4d.
     */
    @Test
    public void testCopyData() {
        System.out.println("copyData");
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 11.0;
        double v4 = 0.0;
        double v5 = 0.0;
        double v6 = 0.0;
        double v7 = 8.0;
        double v8 = 0.0;
        double v9 = 0.0;
        double v10 = 0.0;
        double v11 = 0.0;
        double v12 = 0.0;
        double v13 = 10.0;
        double v14 = 0.0;
        double v15 = 0.0;
        double v16 = 0.0;
        Matrix4d instance = new Matrix4d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);

        Matrix4d result = instance.copyData(instance);

        // TODO review the generated test code and remove the default call to fail.
        System.out.println("copyData test: " + instance +" "+ result);
    }

    /**
     * Test of multiple method, of class Matrix4d.
     */
    @Test
    public void testMultiple() {
        System.out.println("multiple");
        Vector4d v = new Vector4d(1,3,5.1,9.3);
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 11.0;
        double v4 = 0.0;
        double v5 = 0.0;
        double v6 = 0.0;
        double v7 = 8.0;
        double v8 = 0.0;
        double v9 = 0.0;
        double v10 = 0.0;
        double v11 = 0.0;
        double v12 = 0.0;
        double v13 = 10.0;
        double v14 = 0.0;
        double v15 = 0.0;
        double v16 = 0.0;
        Matrix4d instance = new Matrix4d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
        Vector4d result = instance.multiple(v);

        // TODO review the generated test code and remove the default call to fail.
        System.out.println("multiple test: " +  result);
    }

    /**
     * Test of zero method, of class Matrix4d.
     */
    @Test
    public void testZero() {
        System.out.println("zero");

        Matrix4d result = Matrix4d.zero();
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("zero test: " +  result);
    }

    /**
     * Test of identity method, of class Matrix4d.
     */
    @Test
    public void testIdentity() {
        System.out.println("identity");

        Matrix4d result = Matrix4d.identity();

        // TODO review the generated test code and remove the default call to fail.
        System.out.println("identity test: " +  result);
    }

    /**
     * Test of transpose method, of class Matrix4d.
     */
    @Test
    public void testTranspose() {
        System.out.println("transpose");
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 11.0;
        double v4 = 0.0;
        double v5 = 0.0;
        double v6 = 0.0;
        double v7 = 8.0;
        double v8 = 0.0;
        double v9 = 0.0;
        double v10 = 0.0;
        double v11 = 0.0;
        double v12 = 0.0;
        double v13 = 10.0;
        double v14 = 0.0;
        double v15 = 0.0;
        double v16 = 0.0;
        Matrix4d instance = new Matrix4d();
        instance.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
        Matrix4d result = instance.transpose();

        // TODO review the generated test code and remove the default call to fail.
        System.out.println("transpose test: " +  instance + " "+result);
    }

}
