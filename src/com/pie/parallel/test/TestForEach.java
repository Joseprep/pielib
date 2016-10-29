/*
* Copyright 2016 Brandon Ripley
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.pie.parallel.test;

import com.pie.parallel.Parallel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;

public class TestForEach {
    @Test
    public void TestForEachNullArray() throws ExecutionException, InterruptedException {
        try {
            Object[] nullArr = null;
            Parallel.forEach(nullArr, null);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

//    @Test
//    public void TestForEachNullCollection() throws ExecutionException, InterruptedException {
//        try {
//            Collection<Object> nullColl = null;
//            Parallel.forEach(nullColl, null);
//        } catch (Exception e) {
//            Assert.assertTrue(e instanceof IllegalArgumentException);
//        }
//    }

    @Test
    public void TestForEachSingleElementArray() {
        try {
            String[] elements = { "Foo"};
            Collection<String> collection = new ArrayList<String>(elements.length);
            Parallel.forEach(elements, (x) -> collection.add(x));
            Assert.assertTrue("Collection size was: " + collection.size(), collection.size() == elements.length);
            for (String x : collection) {
                Assert.assertTrue(x.compareTo("Foo") == 0);
            }
        } catch (Exception e) {
           Assert.assertTrue(false);
        }
    }

    @Test
    public void TestForEachSmallArray() {
        try {
            String[] elements = { "Foo", "Foo", "Foo" };
            Collection<String> collection = new ArrayList<String>(elements.length);
            Parallel.forEach(elements, (x) -> collection.add(x));
            Assert.assertTrue("Collection size was: " + collection.size(), collection.size() == elements.length);
            for (String x : collection) {
                Assert.assertTrue(x.compareTo("Foo") == 0);
            }
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void TestForEachMediumArray() {
        try {
            String[] elements = { "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo", "Foo" };
            Collection<String> collection = new ArrayBlockingQueue<String>(elements.length);
            Parallel.forEach(elements, (x) -> collection.add(x));
            Assert.assertTrue("Collection size was: " + collection.size(), collection.size() == elements.length);
            for (String x : collection) {
                Assert.assertTrue(x.compareTo("Foo") == 0);
            }
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void ComparePieLibParallelForEachAndParallelStreamWithArray() {
        long pieLibTime = 0;
        long langTime = 0;
        long start;
        int numElements = 10000000;
        String[] elements = new String[numElements];
        for(int i = 0; i < elements.length; i++){
            elements[i] = "Foo";
        }

        Collection<String> col = new ArrayBlockingQueue<String>(numElements);
        try {
            start = System.currentTimeMillis();
            Parallel.forEach(elements, (x) -> col.add(x));
            Assert.assertTrue(col.size() == elements.length);
            pieLibTime = System.currentTimeMillis() - start;
        } catch (InterruptedException e) {
            Assert.assertTrue(false);
        } catch (ExecutionException e) {
            Assert.assertTrue(false);
        }

        Collection<String> col2 = new ArrayBlockingQueue<String>(numElements);

        start = System.currentTimeMillis();
        col.parallelStream().forEach((x) -> col2.add(x));
        langTime = System.currentTimeMillis() - start;
        Assert.assertTrue(col2.size() == col.size());

        System.out.println("Parallel.forEach ran in: " + pieLibTime);
        System.out.println("ParallelStream.forEach ran in: " + langTime);
    }


    @Test
    public void ComparePieLibParallelForEachAndParallelStreamWithCollection() {
        long pieLibTime = 0;
        long langTime = 0;
        long start;
        int numElements = 10000000;
        Collection<String> elements = new ArrayBlockingQueue<String>(numElements);
        for(int i = 0; i < numElements; i++){
            elements.add("Foo");
        }

        Collection<String> col = new ArrayBlockingQueue<String>(numElements);
        try {
            start = System.currentTimeMillis();
            Parallel.forEach(elements, (x) -> col.add(x));
            pieLibTime = System.currentTimeMillis() - start;
            Assert.assertTrue(col.size() == elements.size());
        } catch (InterruptedException e) {
            Assert.assertTrue(false);
        } catch (ExecutionException e) {
            Assert.assertTrue(false);
        }

        Collection<String> col2 = new ArrayBlockingQueue<String>(numElements);

        start = System.currentTimeMillis();
        col.parallelStream().forEach((x) -> col2.add(x));
        langTime = System.currentTimeMillis() - start;
        Assert.assertTrue(col2.size() == col.size());

        System.out.println("Parallel.forEach ran in: " + pieLibTime);
        System.out.println("ParallelStream.forEach ran in: " + langTime);
    }

}
