/*
 * (c) the authors Licensed under the Apache License, Version 2.0.
 */

package org.roaringbitmap;

import org.junit.Test;

import java.util.*;
import static org.junit.Assert.*;

public class TestBitmapContainer {
  private static BitmapContainer emptyContainer() {
    return new BitmapContainer(new long[1], 0);
  }
  
  @Test
  public void testToString() {
    BitmapContainer bc2 = new BitmapContainer();
    bc2.add((short)1);
    String s = bc2.toString();
    assertTrue(s.equals("{1}"));
  }
  
  @Test  
  public void testXOR() {
    BitmapContainer bc = new BitmapContainer(100,10000);
    BitmapContainer bc2 = new BitmapContainer();
    BitmapContainer bc3 = new BitmapContainer();

    for(int i = 100; i < 10000; ++i) {
      if((i%2 ) == 0)
        bc2 = (BitmapContainer) bc2.add((short) i);
        else bc3 = (BitmapContainer) bc3.add((short) i);
    }
    bc = (BitmapContainer) bc.ixor(bc2);
    assertTrue(bc.ixor(bc3).getCardinality() == 0);
  }
  
  @Test  
  public void testANDNOT() {
    BitmapContainer bc = new BitmapContainer(100,10000);
    BitmapContainer bc2 = new BitmapContainer();
    BitmapContainer bc3 = new BitmapContainer();

    for(int i = 100; i < 10000; ++i) {
      if((i%2 ) == 0)
        bc2 = (BitmapContainer) bc2.add((short) i);
        else bc3 = (BitmapContainer) bc3.add((short) i);
    }
    RunContainer rc = new RunContainer();
    rc.iadd(0, 1<<16);
    bc = (BitmapContainer) bc.iand(rc);
    bc = (BitmapContainer) bc.iandNot(bc2);
    assertTrue(bc.equals(bc3));
    assertTrue(bc.hashCode() == bc3.hashCode());
    assertTrue(bc.iandNot(bc3).getCardinality() == 0);
    bc3.clear();
    assertTrue(bc3.getCardinality() == 0);
  }
  

  @Test  
  public void testAND() {
    BitmapContainer bc = new BitmapContainer(100,10000);
    BitmapContainer bc2 = new BitmapContainer();
    BitmapContainer bc3 = new BitmapContainer();

    for(int i = 100; i < 10000; ++i) {
      if((i%2 ) == 0)
        bc2 = (BitmapContainer) bc2.add((short) i);
        else bc3 = (BitmapContainer) bc3.add((short) i);
    }
    bc = (BitmapContainer) bc.iand(bc2);
    assertTrue(bc.equals(bc2));    
    assertTrue(bc.iand(bc3).getCardinality() == 0);    
  }

  

  @Test  
  public void testOR() {
    BitmapContainer bc = new BitmapContainer(100,10000);
    BitmapContainer bc2 = new BitmapContainer();
    BitmapContainer bc3 = new BitmapContainer();

    for(int i = 100; i < 10000; ++i) {
      if((i%2 ) == 0)
        bc2 = (BitmapContainer) bc2.add((short) i);
        else bc3 = (BitmapContainer) bc3.add((short) i);
    }
    bc2 = (BitmapContainer) bc2.ior(bc3);
    assertTrue(bc.equals(bc2));        
    bc2 = (BitmapContainer) bc2.ior(bc);
    assertTrue(bc.equals(bc2));       
    RunContainer rc = new RunContainer();
    rc.iadd(0, 1<<16);
    assertTrue(bc.iandNot(rc).getCardinality() == 0);      
  }
  
  @Test
  public void runConstructorForBitmap() {
    System.out.println("runConstructorForBitmap");
    for(int start = 0; start <= (1<<16); start += 4096 ) {
      for(int end = start; end <= (1<<16); end += 4096 ) {
        BitmapContainer bc = new BitmapContainer(start,end);
        BitmapContainer bc2 = new BitmapContainer();
        BitmapContainer bc3 = (BitmapContainer) bc2.add(start,end);
        bc2 = (BitmapContainer) bc2.iadd(start,end);
        assertEquals(bc.getCardinality(), end-start);
        assertEquals(bc2.getCardinality(), end-start);
        assertEquals(bc, bc2);
        assertEquals(bc, bc3);
        assertEquals(0,bc2.remove(start, end).getCardinality());
        assertEquals(bc2.getCardinality(), end-start);
        assertEquals(0,bc2.not(start, end).getCardinality());
      }  
    }
  }

  @Test
  public void runConstructorForBitmap2() {
    System.out.println("runConstructorForBitmap2");
    for(int start = 0; start <= (1<<16); start += 63 ) {
      for(int end = start; end <= (1<<16); end += 63 ) {
        BitmapContainer bc = new BitmapContainer(start,end);
        BitmapContainer bc2 = new BitmapContainer();
        BitmapContainer bc3 = (BitmapContainer) bc2.add(start,end);
        bc2 = (BitmapContainer) bc2.iadd(start,end);
        assertEquals(bc.getCardinality(), end-start);
        assertEquals(bc2.getCardinality(), end-start);
        assertEquals(bc, bc2);
        assertEquals(bc, bc3);
        assertEquals(0,bc2.remove(start, end).getCardinality());
        assertEquals(bc2.getCardinality(), end-start);
        assertEquals(0,bc2.not(start, end).getCardinality());
      }  
    }
  }
  
  @Test
  public void numberOfRunsLowerBound1() {
    System.out.println("numberOfRunsLowerBound1");
    Random r = new Random(12345);

    for (double density = 0.001; density < 0.8; density *= 2) {

      ArrayList<Integer> values = new ArrayList<Integer>();
      for (int i = 0; i < 65536; ++i) {
        if (r.nextDouble() < density) {
          values.add(i);
        }
      }
      Integer[] positions = values.toArray(new Integer[0]);
      BitmapContainer bc = new BitmapContainer();

      for (int position : positions) {
        bc.add((short) position);
      }

      assertTrue(bc.numberOfRunsLowerBound(1) > 1);
      assertTrue(bc.numberOfRunsLowerBound(100) <= bc.numberOfRuns());

      // a big parameter like 100000 ensures that the full lower bound
      // is taken


      assertTrue(bc.numberOfRunsLowerBound(100000) <= bc.numberOfRuns());
      assertEquals(bc.numberOfRuns(),
          bc.numberOfRunsLowerBound(100000) + bc.numberOfRunsAdjustment());

      /*
       * the unrolled guys are commented out, did not help performance and slated for removal
       * soon...
       * 
       * assertTrue(bc.numberOfRunsLowerBoundUnrolled2(1) > 1);
       * assertTrue(bc.numberOfRunsLowerBoundUnrolled2(100) <= bc.numberOfRuns());
       * 
       * assertEquals(bc.numberOfRunsLowerBound(100000),
       * bc.numberOfRunsLowerBoundUnrolled2(100000));
       */
    }

  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testNextTooLarge() {
    emptyContainer().nextSetBit(Short.MAX_VALUE + 1);
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testNextTooSmall() {
    emptyContainer().nextSetBit(-1);
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testPreviousTooLarge() {
    emptyContainer().prevSetBit(Short.MAX_VALUE + 1);
  }


  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testPreviousTooSmall() {
    emptyContainer().prevSetBit(-1);
  }
}
