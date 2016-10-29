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
package com.pie.parallel;

import java.util.Collection;
import java.util.concurrent.*;


public class Parallel {

    private static int LOGICAL_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(LOGICAL_PROCESSORS);
    private static BlockingQueue<SlottedWorker> workers = new ArrayBlockingQueue<SlottedWorker>(LOGICAL_PROCESSORS);
    private static BlockingQueue<Future<SlottedWorker>> futures = new ArrayBlockingQueue<Future<SlottedWorker>>(LOGICAL_PROCESSORS);

    static {
        for (int i = 0; i < LOGICAL_PROCESSORS; i++){
            workers.add(new SlottedWorker());
        }
    }

    public interface ForEachAction<E> {
        void run(E e);
    }


    public static synchronized <E> void forEach(E[] elements, ForEachAction<E> action) throws InterruptedException, ExecutionException {
        if(elements == null || action == null) {
            throw new IllegalArgumentException((elements == null ? "Elements" : "ForEachAction") + " was null.");
        }
        long slotQuantity = Math.round((double)elements.length/(double)LOGICAL_PROCESSORS);
        long start, end;

        start = 0; end = slotQuantity;
        for(int i = 0; i < LOGICAL_PROCESSORS - 1 && slotQuantity > 0; i++, start = end, end = end + slotQuantity){
            futures.add(threadPool.submit(workers.take().reset(start, end, elements, action)));
        }
        end = elements.length;
        futures.add(threadPool.submit(workers.take().reset(start, end, elements, action)));


        while(futures.peek() != null) {
            workers.add(futures.take().get());
        }
    }

    public static synchronized <E> void forEach (Collection<E> col, ForEachAction<E> action) throws ExecutionException, InterruptedException {
        if(col == null || action == null) {
            throw new IllegalArgumentException((col == null ? "Elements" : "ForEachAction") + " was null.");
        }
        forEach((E[])col.toArray(), action);
    }

    private static class SlottedWorker<E> implements Callable<SlottedWorker<E>> {

        private long start;
        private long end;
        private E[] elements;
        private ForEachAction action;


        public Callable<SlottedWorker<E>> reset(long s, long e, E[] eles, ForEachAction<E> fea) {
            start = s;
            end = e;
            elements = eles;
            action = fea;
            return this;
        }

        @Override
        public SlottedWorker<E> call() {
            for(long i = start; i < end; i++){
                action.run(elements[(int)i]);
            }
            return this;
        }
    }

}
