package co.kr.emgram.mobilpackfieldtest;


public class Outer {
    public void sampleMethod() {
        SampleThread st = new SampleThread();
        SampleThread2 st2 = new SampleThread2(new Runnable(){
            @Override
            public void run() {
            }

            public void test() {

            }
        });
        st.start();
        st2.start();
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private static class SampleThread extends Thread {
        public void print() {
        }

        public void run() {
            for(int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            new Runnable(){
                @Override
                public void run() {

                }
            };
            print();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }

    private class SampleThread2 extends Thread {
        SampleThread2(Runnable runnable) {
            runnable.run();
        }

        public void print() {
        }

        public void run() {
            print();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }
}
